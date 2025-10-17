package com.acme.linkedinoptimizer.service

import com.acme.linkedinoptimizer.domain.dto.ExperienceDto
import com.acme.linkedinoptimizer.domain.dto.ProfileDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ScoringServiceTest {
    private val service = ScoringService()

    @Test
    fun `should reward complete profile`() {
        val profile = ProfileDto(
            url = "https://linkedin.com/in/jane",
            fullName = "Jane Doe",
            headline = "Product Manager | SaaS",
            about = "Passionate product leader with 8 years of experience delivering SaaS products that scale.",
            location = "Paris",
            hasPhoto = true,
            experiences = listOf(
                ExperienceDto(
                    title = "Senior PM",
                    company = "Acme",
                    dates = "2021-2024",
                    description = "Led roadmap delivering +35% ARR with cross-functional teams of 20+."
                ),
                ExperienceDto(
                    title = "PM",
                    company = "Globex",
                    dates = "2018-2021",
                    description = "Built experimentation program increasing activation by 18%."
                )
            ),
            skills = listOf("Product Strategy", "Roadmap", "A-B Testing", "SaaS", "Leadership", "SQL", "UX Research", "Agile", "OKR", "Go-to-market", "Analytics")
        )

        val score = service.score(profile, listOf("SaaS", "Product"))

        assertThat(score.total).isGreaterThanOrEqualTo(80)
        assertThat(score.breakdown["skills"]).isGreaterThanOrEqualTo(12)
        assertThat(score.warnings).isEmpty()
    }

    @Test
    fun `should flag missing sections`() {
        val profile = ProfileDto(
            url = "https://linkedin.com/in/john",
            experiences = emptyList(),
            skills = emptyList(),
            hasPhoto = false
        )

        val score = service.score(profile)

        assertThat(score.total).isLessThan(40)
        assertThat(score.warnings).isNotEmpty()
    }
}
