package com.ameltz.languagelearner.ui.composable

import java.util.Locale

/**
 * Infers a TTS locale from text by scanning Unicode script ranges.
 * Hiragana/Katakana → Japanese (takes priority over bare CJK kanji).
 * Hangul → Korean. Arabic → Arabic. Bare CJK → Chinese. Otherwise device default.
 */
fun detectLocale(text: String): Locale {
    for (ch in text) {
        val cp = ch.code
        when {
            cp in 0x3040..0x309F -> return Locale.JAPANESE // hiragana
            cp in 0x30A0..0x30FF -> return Locale.JAPANESE // katakana
            cp in 0x4E00..0x9FFF -> return Locale.JAPANESE // kanji (CJK)
            cp in 0xAC00..0xD7AF -> return Locale.KOREAN
            cp in 0x0600..0x06FF -> return Locale("ar")
        }
    }
    return Locale.getDefault()
}
