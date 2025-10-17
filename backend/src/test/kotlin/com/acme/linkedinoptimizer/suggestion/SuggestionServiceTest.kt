package com.acme.linkedinoptimizer.suggestion

import com.acme.linkedinoptimizer.model.ExperienceSnapshot
import com.acme.linkedinoptimizer.model.ProfileSnapshot
import com.acme.linkedinoptimizer.model.SuggestionLanguage
import com.acme.linkedinoptimizer.suggestion.llm.OllamaClient
import com.acme.linkedinoptimizer.suggestion.llm.OpenRouterClient
import com.acme.linkedinoptimizer.util.JobMapping
import com.acme.linkedinoptimizer.util.TextUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SuggestionServiceTest {

    private val properties = LlmProperties()
    private val service = SuggestionService(
        properties = properties,
        ollamaClient = object : OllamaClient(mockkBuilder(), "http://localhost", properties) {
            override fun generateCompletion(prompt: String, temperature: Double): String =
                "ABOUT::LLM perspective||Je structure l'impact en 3 axes."
        },
        openRouterClient = object : OpenRouterClient(mockkBuilder(), "https://openrouter.ai", null, properties) {
            override fun generateCompletion(prompt: String, temperature: Double): String = ""
        },
        textUtils = TextUtils(),
        jobMapping = JobMapping()
    )

    @Test
    fun `should build suggestions with heuristics`() {
        val profile = ProfileSnapshot(
            headline = "Data Analyst",
            about = "",
            skills = listOf("SQL", "Python", "Power BI", "Storytelling"),
            experiences = listOf(
                ExperienceSnapshot(
                    role = "Data Analyst",
                    company = "Acme",
                    achievements = "Automatisé les reportings (-30% de temps)\nBoosté le NPS de 12%",
                    timeframe = "2022-2024"
                )
            ),
            location = "Lyon",
            targetRole = "Senior Data Analyst"
        )

        val suggestions = service.suggest(profile, null, SuggestionLanguage.FR)

        assertThat(suggestions.headline).hasSize(3)
        assertThat(suggestions.skills.core).isNotEmpty()
        assertThat(suggestions.experiences).hasSize(1)
        assertThat(suggestions.about).anyMatch { it.label.contains("LLM") }
    }

    companion object {
        private fun mockkBuilder(): org.springframework.web.reactive.function.client.WebClient.Builder =
            org.springframework.web.reactive.function.client.WebClient.builder()
    }
}
