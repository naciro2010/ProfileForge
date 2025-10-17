package com.acme.linkedinoptimizer.service.llm

import com.acme.linkedinoptimizer.domain.LlmProvider
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class OllamaClient(
    builder: WebClient.Builder,
    @Value("\${ollama.base-url:http://localhost:11434}") private val baseUrl: String
) : LlmClient {
    override val provider: LlmProvider = LlmProvider.OLLAMA
    private val client: WebClient = builder.baseUrl(baseUrl).build()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generate(model: String, prompt: String): String {
        val response = client.post()
            .uri("/api/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("model" to model, "prompt" to prompt, "stream" to false))
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle()
        val result = json.parseToJsonElement(response)
        val content = result.jsonObject["response"]?.jsonPrimitive?.content
        return content ?: response
    }
}
