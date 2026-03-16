package com.ameltz.languagelearner.data.api

import com.ameltz.languagelearner.ui.model.VoiceCardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class VoiceCardRequest(
    val model: String,
    val max_tokens: Int,
    val messages: List<VoiceMessage>
)

@Serializable
data class VoiceMessage(
    val role: String,
    val content: String
)

@Serializable
data class VoiceCardResponse(
    val content: List<VoiceResponseContent>,
    val id: String,
    val model: String
)

@Serializable
data class VoiceResponseContent(
    val type: String,
    val text: String
)

class VoiceCardAnalysisService {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeVoiceCommand(
        transcript: String,
        apiKey: String
    ): Result<VoiceCardData> = withContext(Dispatchers.IO) {
        try {
            val requestBody = createRequest(transcript)
            val response = executeRequest(requestBody, apiKey)
            val cardData = parseResponse(response)
            Result.success(cardData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createRequest(transcript: String): String {
        val prompt = """Analyze this voice command for creating a flashcard and extract the information.

Voice command: "$transcript"

Parse this command and extract:
1. The FRONT of the card (the word/phrase to learn)
2. The BACK of the card (the translation/meaning)
3. The DECK name where it should be added

IMPORTANT RULES:
- If Japanese words are spelled phonetically (romanized), convert them to proper Japanese characters (hiragana/katakana/kanji)
- Example: "konnichiwa" → "こんにちは"
- Example: "arigatou" → "ありがとう"
- Example: "sayonara" → "さようなら"
- If the deck name is generic like "Japanese" or "my Japanese deck", extract just the key word (e.g., "Japanese")
- Handle variations like "add card", "create card", "new card", etc.

Return ONLY a JSON object in this exact format with no additional text:
{
  "front": "the front of the card",
  "back": "the back of the card",
  "deckName": "the deck name"
}

If the command is unclear or missing information, make reasonable assumptions but still return valid JSON."""

        val request = VoiceCardRequest(
            model = "claude-3-5-sonnet-20241022",
            max_tokens = 1024,
            messages = listOf(
                VoiceMessage(
                    role = "user",
                    content = prompt
                )
            )
        )
        return Json.encodeToString(VoiceCardRequest.serializer(), request)
    }

    private fun executeRequest(requestBody: String, apiKey: String): String {
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("API call failed: ${response.code} - ${response.message}")
            }
            return response.body?.string() ?: throw Exception("Empty response body")
        }
    }

    private fun parseResponse(responseBody: String): VoiceCardData {
        val response = json.decodeFromString<VoiceCardResponse>(responseBody)
        val textContent = response.content.firstOrNull { it.type == "text" }?.text
            ?: throw Exception("No text content in response")

        // Extract JSON object from text (handles both raw JSON and markdown-wrapped)
        val jsonMatch = Regex("""\{[\s\S]*\}""").find(textContent)
            ?: throw Exception("No JSON object found in response")

        val jsonObject = jsonMatch.value
        val parsedData = Json.decodeFromString<Map<String, String>>(jsonObject)

        return VoiceCardData(
            front = parsedData["front"] ?: throw Exception("Missing 'front' field"),
            back = parsedData["back"] ?: throw Exception("Missing 'back' field"),
            deckName = parsedData["deckName"] ?: throw Exception("Missing 'deckName' field")
        )
    }
}
