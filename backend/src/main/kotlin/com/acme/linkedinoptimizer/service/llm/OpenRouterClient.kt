package com.acme.linkedinoptimizer.service.llm

import com.acme.linkedinoptimizer.domain.LlmProvider
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class OpenRouterClient(
    builder: WebClient.Builder,
    @Value("\${openrouter.base-url:https://openrouter.ai/api/v1/chat/completions}") private val baseUrl: String,
    @Value("\${openrouter.api-key:}") private val apiKey: String
) : LlmClient {
    override val provider: LlmProvider = LlmProvider.OPENROUTER
    private val client: WebClient = builder.baseUrl(baseUrl).build()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generate(model: String, prompt: String): String {
        require(apiKey.isNotBlank()) { "OpenRouter API key is required" }
        val response = client.post()
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .bodyValue(
                mapOf(
                    "model" to model,
                    "messages" to listOf(mapOf("role" to "user", "content" to prompt))
                )
            )
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle()
        val parsed = json.parseToJsonElement(response)
        return parsed.jsonObject["choices"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("message")
            ?.jsonObject
            ?.get("content")
            ?.jsonPrimitive
            ?.content
            ?: response
    }
}
