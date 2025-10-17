package com.acme.linkedinoptimizer.suggestion

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "linkedin-optimizer.llm")
data class LlmProperties(
    val provider: Provider = Provider.OLLAMA,
    val model: String = "llama3:instruct",
    @DefaultValue("fr")
    val defaultLanguage: String = "fr"
) {
    enum class Provider { OLLAMA, OPENROUTER }
}
