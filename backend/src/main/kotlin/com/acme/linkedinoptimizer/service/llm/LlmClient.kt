package com.acme.linkedinoptimizer.service.llm

import com.acme.linkedinoptimizer.domain.LlmProvider

interface LlmClient {
    val provider: LlmProvider
    suspend fun generate(model: String, prompt: String): String
}
