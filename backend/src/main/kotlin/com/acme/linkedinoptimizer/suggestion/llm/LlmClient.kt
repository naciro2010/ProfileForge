package com.acme.linkedinoptimizer.suggestion.llm

interface LlmClient {
    fun generateCompletion(prompt: String, temperature: Double = 0.4): String
}
