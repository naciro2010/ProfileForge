package com.acme.linkedinoptimizer.suggestion.llm

import com.acme.linkedinoptimizer.suggestion.LlmProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
open class OllamaClient(
    builder: WebClient.Builder,
    @Value("\${ollama.base-url:http://localhost:11434}") baseUrl: String,
    private val properties: LlmProperties
) : LlmClient {

    private val logger = LoggerFactory.getLogger(OllamaClient::class.java)
    private val client: WebClient = builder.baseUrl(baseUrl).build()

    override fun generateCompletion(prompt: String, temperature: Double): String {
        return runCatching {
            client.post()
                .uri("/api/generate")
                .bodyValue(
                    mapOf(
                        "model" to properties.model,
                        "prompt" to prompt,
                        "stream" to false,
                        "options" to mapOf("temperature" to temperature)
                    )
                )
                .retrieve()
                .bodyToMono(OllamaResponse::class.java)
                .block(Duration.ofSeconds(40))
                ?.response
                .orEmpty()
        }.onFailure { error ->
            logger.warn("Ollama generation failed: {}", error.message)
        }.getOrDefault("")
    }

    private data class OllamaResponse(val response: String? = null)
}
