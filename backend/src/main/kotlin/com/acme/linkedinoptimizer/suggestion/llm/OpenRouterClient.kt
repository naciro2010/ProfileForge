package com.acme.linkedinoptimizer.suggestion.llm

import com.acme.linkedinoptimizer.suggestion.LlmProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
open class OpenRouterClient(
    builder: WebClient.Builder,
    @Value("\${openrouter.base-url:https://openrouter.ai/api/v1/chat/completions}") private val baseUrl: String,
    @Value("\${openrouter.api-key:}") private val apiKey: String?,
    private val properties: LlmProperties
) : LlmClient {

    private val logger = LoggerFactory.getLogger(OpenRouterClient::class.java)
    private val client: WebClient = builder.baseUrl(baseUrl).build()

    override fun generateCompletion(prompt: String, temperature: Double): String {
        if (apiKey.isNullOrBlank()) {
            logger.warn("OpenRouter API key missing; returning empty completion")
            return ""
        }

        return runCatching {
            client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $apiKey")
                .bodyValue(
                    mapOf(
                        "model" to properties.model,
                        "temperature" to temperature,
                        "messages" to listOf(
                            mapOf("role" to "system", "content" to SYSTEM_PROMPT),
                            mapOf("role" to "user", "content" to prompt)
                        )
                    )
                )
                .retrieve()
                .bodyToMono(OpenRouterResponse::class.java)
                .block(Duration.ofSeconds(40))
                ?.choices
                ?.firstOrNull()
                ?.message
                ?.content
                .orEmpty()
        }.onFailure { error ->
            logger.warn("OpenRouter generation failed: {}", error.message)
        }.getOrDefault("")
    }

    private data class OpenRouterResponse(val choices: List<Choice> = emptyList())
    private data class Choice(val message: Message)
    private data class Message(val content: String = "")

    companion object {
        private const val SYSTEM_PROMPT = "You generate actionable LinkedIn copy compliant with platform rules."
    }
}
