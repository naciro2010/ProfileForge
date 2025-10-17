package com.acme.linkedinoptimizer.util

import org.springframework.stereotype.Component

@Component
class TextUtils {
    fun sanitize(input: String): String = input
        .replace("\u00A0", " ")
        .replace("…", "...")
        .replace(Regex("\\s+"), " ")
        .trim()

    fun truncate(input: String, maxLength: Int): String {
        if (input.length <= maxLength) return input
        return input.take(maxLength - 1).trimEnd() + "…"
    }

    fun splitParagraphs(input: String): List<String> = input
        .split(Regex("\n+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    fun enforceParagraphCount(text: String, min: Int = 3, max: Int = 5): String {
        val paragraphs = splitParagraphs(text)
        if (paragraphs.isEmpty()) return text
        val limited = paragraphs.take(max)
        return limited.joinToString(separator = "\n\n")
    }
}
