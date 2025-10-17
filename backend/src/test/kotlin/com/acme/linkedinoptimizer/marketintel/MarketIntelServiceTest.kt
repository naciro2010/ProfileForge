package com.acme.linkedinoptimizer.marketintel

import com.acme.linkedinoptimizer.model.MarketIntelRequest
import com.acme.linkedinoptimizer.model.RecruiterSignal
import com.acme.linkedinoptimizer.model.SkillCategory
import com.acme.linkedinoptimizer.model.SkillInsight
import com.acme.linkedinoptimizer.model.SourceAttribution
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class MarketIntelServiceTest {

    private val provider = object : MarketIntelProvider {
        override fun supports(country: String): Boolean = true
        override fun fetch(request: MarketIntelRequest): ProviderResult = ProviderResult(
            refreshedAt = Instant.now(),
            skills = listOf(
                SkillInsight("SQL", SkillCategory.CORE, "Test"),
                SkillInsight("Communication", SkillCategory.SOFT, "Test")
            ),
            recruiterSignals = listOf(RecruiterSignal("Chiffrez", "Test")),
            sources = listOf(SourceAttribution("Test", "https://example.com"))
        )
    }
    private val service = MarketIntelService(listOf(provider))

    @Test
    fun `should cache market intel`() {
        val request = MarketIntelRequest(role = "Data Analyst", country = "FR")
        val first = service.marketIntel(request)
        val second = service.marketIntel(request)

        assertThat(first.skills).hasSize(2)
        assertThat(second.skills).hasSize(2)
        assertThat(first).isEqualTo(second)
    }
}
