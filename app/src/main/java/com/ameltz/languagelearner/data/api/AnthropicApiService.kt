package com.ameltz.languagelearner.data.api

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.ameltz.languagelearner.ui.model.ExtractedWordPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class AnthropicRequest(
    val model: String,
    val max_tokens: Int,
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String,
    val content: List<ContentBlock>
)

@Serializable
data class ContentBlock(
    val type: String,
    val source: ImageSource? = null,
    val text: String? = null
)

@Serializable
data class ImageSource(
    val type: String,
    val media_type: String,
    val data: String
)

@Serializable
data class AnthropicResponse(
    val content: List<ResponseContent>,
    val id: String,
    val model: String,
    val stop_reason: String?
)

@Serializable
data class ResponseContent(
    val type: String,
    val text: String
)

class AnthropicApiService(private val context: Context) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun extractWordsFromImage(
        imageUri: Uri,
        apiKey: String
    ): Result<List<ExtractedWordPair>> = withContext(Dispatchers.IO) {
        try {
            val base64Image = encodeImageToBase64(imageUri)
            val requestBody = createRequest(base64Image)
            val response = executeRequest(requestBody, apiKey)
            val wordPairs = parseResponse(response)
            Result.success(wordPairs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun encodeImageToBase64(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: throw Exception("Cannot read image")
        inputStream.close()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun createRequest(base64Image: String): String {
        val request = AnthropicRequest(
            model = "claude-3-5-sonnet-20241022",
            max_tokens = 2048,
            messages = listOf(
                Message(
                    role = "user",
                    content = listOf(
                        ContentBlock(
                            type = "image",
                            source = ImageSource(
                                type = "base64",
                                media_type = "image/jpeg",
                                data = base64Image
                            )
                        ),
                        ContentBlock(
                            type = "text",
                            text = """Analyze this Japanese study page image and extract all Japanese words with their English translations.

Return ONLY a JSON array in this exact format, with no additional text or explanation:
[
  {"japanese": "こんにちは", "english": "hello"},
  {"japanese": "ありがとう", "english": "thank you"}
]

Rules:
- Extract only word pairs that are clearly visible
- Keep Japanese text exactly as shown (hiragana, katakana, or kanji)
- English translations should be lowercase
- Return empty array [] if no words found
- Do not include any markdown, explanations, or text outside the JSON array"""
                        )
                    )
                )
            )
        )
        return Json.encodeToString(AnthropicRequest.serializer(), request)
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

    private fun parseResponse(responseBody: String): List<ExtractedWordPair> {
        val response = json.decodeFromString<AnthropicResponse>(responseBody)
        val textContent = response.content.firstOrNull { it.type == "text" }?.text
            ?: throw Exception("No text content in response")

        // Extract JSON array from text (handles both raw JSON and markdown-wrapped)
        val jsonMatch = Regex("""\[[\s\S]*\]""").find(textContent)
            ?: throw Exception("No JSON array found in response")

        val jsonArray = jsonMatch.value
        val wordPairs = Json.decodeFromString<List<Map<String, String>>>(jsonArray)

        return wordPairs.map {
            ExtractedWordPair(
                japanese = it["japanese"] ?: "",
                english = it["english"] ?: ""
            )
        }.filter { it.japanese.isNotBlank() && it.english.isNotBlank() }
    }
}
