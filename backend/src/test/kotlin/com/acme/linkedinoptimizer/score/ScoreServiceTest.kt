package com.acme.linkedinoptimizer.score

import com.acme.linkedinoptimizer.model.ExperienceSnapshot
import com.acme.linkedinoptimizer.model.ProfileSnapshot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ScoreServiceTest {

    private val service = ScoreService()

    @Test
    fun `should reward complete profile`() {
        val profile = ProfileSnapshot(
            headline = "Senior Data Analyst | Product Analytics",
            about = "Passionné par la donnée avec 10 ans d'expérience. Je pilote des projets de bout en bout en mesurant l'impact.",
            skills = listOf("SQL", "Python", "Product Analytics", "Storytelling"),
            experiences = listOf(
                ExperienceSnapshot(
                    role = "Lead Data Analyst",
                    company = "ScaleUp",
                    achievements = "Piloté 5 dashboards et amélioré le NPS de 18%",
                    timeframe = "2021-2024"
                )
            ),
            location = "Paris",
            hasPhoto = true
        )

        val score = service.score(profile, listOf("analytics", "sql"))

        assertThat(score.total).isBetween(70, 100)
        assertThat(score.warnings).isEmpty()
        assertThat(score.breakdown).containsKeys("headline", "experience", "skills", "keywords")
    }

    @Test
    fun `should flag missing sections`() {
        val score = service.score(ProfileSnapshot(), emptyList())

        assertThat(score.total).isLessThan(30)
        assertThat(score.warnings).isNotEmpty()
    }
}
