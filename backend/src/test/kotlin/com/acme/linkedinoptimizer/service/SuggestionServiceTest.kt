package com.acme.linkedinoptimizer.service

import com.acme.linkedinoptimizer.domain.LlmProvider
import com.acme.linkedinoptimizer.domain.dto.ProfileDto
import com.acme.linkedinoptimizer.service.llm.LlmClient
import com.acme.linkedinoptimizer.util.TextUtils
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SuggestionServiceTest {
    private class StubClient(private val payload: String) : LlmClient {
        override val provider: LlmProvider = LlmProvider.OLLAMA
        override suspend fun generate(model: String, prompt: String): String = payload
    }

    private val objectMapper = ObjectMapper()
    private val textUtils = TextUtils()

    @Test
    fun `should format suggestions within constraints`() {
        val stubResponse = """
            {
              "headline": "Growth Marketer | Acquisition & Automation",
              "about": "Paragraphe 1.\n\nParagraphe 2.\n\nParagraphe 3.",
              "skills": ["SEO", "SEA", "Automation", "Analytics", "Copywriting", "CRM", "HubSpot"],
              "experienceBullets": [
                {
                  "company": "Acme",
                  "title": "Growth Marketer",
                  "bullets": [
                    "Augmenté le pipeline MRR de 45% via campagnes multicanales.",
                    "Déployé 12 expériences A-B réduisant le CPA de 28%."
                  ]
                }
              ]
            }
        """.trimIndent()

        val service = SuggestionService(
            llmClients = listOf(StubClient(stubResponse)),
            textUtils = textUtils,
            objectMapper = objectMapper,
            cacheTtl = Duration.ofMinutes(5),
            defaultProvider = "ollama",
            defaultModel = "llama3:instruct",
            defaultLanguage = "fr"
        )

        val profile = ProfileDto(url = "https://linkedin.com/in/test", experiences = emptyList(), skills = emptyList())

        val suggestion = service.suggest(profile)

        assertThat(suggestion.headline.length).isLessThanOrEqualTo(220)
        assertThat(suggestion.skills).hasSizeBetween(1, 12)
        assertThat(suggestion.experienceBullets).isNotEmpty()
    }
}
