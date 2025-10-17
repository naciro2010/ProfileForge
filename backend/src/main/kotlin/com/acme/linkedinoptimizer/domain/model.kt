package com.acme.linkedinoptimizer.domain

enum class LlmProvider {
    OLLAMA,
    OPENROUTER
}

data class SuggestionContext(
    val provider: LlmProvider,
    val model: String,
    val language: String
)
