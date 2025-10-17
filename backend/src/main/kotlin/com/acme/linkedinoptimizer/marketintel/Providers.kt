package com.acme.linkedinoptimizer.marketintel

import com.acme.linkedinoptimizer.model.MarketIntelRequest
import com.acme.linkedinoptimizer.model.RecruiterSignal
import com.acme.linkedinoptimizer.model.SkillCategory
import com.acme.linkedinoptimizer.model.SkillInsight
import com.acme.linkedinoptimizer.model.SourceAttribution
import org.springframework.stereotype.Component
import java.time.Instant

interface MarketIntelProvider {
    fun supports(country: String): Boolean
    fun fetch(request: MarketIntelRequest): ProviderResult
}

data class ProviderResult(
    val refreshedAt: Instant,
    val skills: List<SkillInsight>,
    val recruiterSignals: List<RecruiterSignal>,
    val sources: List<SourceAttribution>
)

@Component
class IndeedHiringLabProvider : MarketIntelProvider {
    private val coverage = setOf("US", "FR", "UK", "CA", "DE")

    override fun supports(country: String): Boolean = coverage.contains(country.uppercase())

    override fun fetch(request: MarketIntelRequest): ProviderResult {
        val lowerRole = request.role.lowercase()
        val skills = when {
            lowerRole.contains("data") -> listOf(
                SkillInsight("SQL", SkillCategory.CORE, "Indeed Hiring Lab"),
                SkillInsight("Analyse exploratoire", SkillCategory.CORE, "Indeed Hiring Lab"),
                SkillInsight("Storytelling data", SkillCategory.SOFT, "Indeed Hiring Lab")
            )
            lowerRole.contains("product") -> listOf(
                SkillInsight("Discovery", SkillCategory.CORE, "Indeed Hiring Lab"),
                SkillInsight("Collaboration cross-fonction", SkillCategory.SOFT, "Indeed Hiring Lab"),
                SkillInsight("Expérimentation", SkillCategory.TRENDING, "Indeed Hiring Lab")
            )
            else -> listOf(
                SkillInsight("Communication", SkillCategory.SOFT, "Indeed Hiring Lab"),
                SkillInsight("Adaptabilité", SkillCategory.SOFT, "Indeed Hiring Lab"),
                SkillInsight("Compétences numériques", SkillCategory.CORE, "Indeed Hiring Lab")
            )
        }
        val signals = listOf(
            RecruiterSignal(
                statement = "Chiffrez l’impact dans les 90 premiers jours.",
                rationale = "Indeed Hiring Lab observe une hausse des offres valorisant les résultats mesurables."
            ),
            RecruiterSignal(
                statement = "Mettez en avant les compétences transférables.",
                rationale = "Les recruteurs privilégient l’approche skills-first."
            )
        )
        return ProviderResult(
            refreshedAt = Instant.now(),
            skills = skills,
            recruiterSignals = signals,
            sources = listOf(
                SourceAttribution("Indeed Hiring Lab 2025", "https://hiringlab.org")
            )
        )
    }
}

@Component
class ShrmProvider : MarketIntelProvider {
    private val coverage = setOf("US", "CA", "UK")

    override fun supports(country: String): Boolean = coverage.contains(country.uppercase())

    override fun fetch(request: MarketIntelRequest): ProviderResult = ProviderResult(
        refreshedAt = Instant.now().minusSeconds(86_400),
        skills = listOf(
            SkillInsight("Soft skills collaboratives", SkillCategory.SOFT, "SHRM"),
            SkillInsight("Upskilling continu", SkillCategory.TRENDING, "SHRM")
        ),
        recruiterSignals = listOf(
            RecruiterSignal("Mentionnez votre démarche d’upskilling IA.", "SHRM recommande de prouver l’adaptabilité."),
            RecruiterSignal("Illustrez la collaboration inter-équipes.", "Les recruteurs américains valorisent cette compétence en 2025.")
        ),
        sources = listOf(SourceAttribution("SHRM Talent Trends 2025", "https://www.shrm.org"))
    )
}

@Component
class PressInsightsProvider : MarketIntelProvider {
    override fun supports(country: String): Boolean = true

    override fun fetch(request: MarketIntelRequest): ProviderResult = ProviderResult(
        refreshedAt = Instant.now().minusSeconds(172_800),
        skills = listOf(
            SkillInsight("IA literacy", SkillCategory.TRENDING, "Forbes"),
            SkillInsight("Communication", SkillCategory.SOFT, "Axios"),
            SkillInsight("Résilience", SkillCategory.SOFT, "Financial Times")
        ),
        recruiterSignals = listOf(
            RecruiterSignal(
                "Prouvez votre maîtrise de l’IA générative.",
                "La presse économique souligne l’attente d’une maîtrise concrète des outils IA."
            )
        ),
        sources = listOf(
            SourceAttribution("Forbes - Skills on the Rise 2025", "https://www.forbes.com"),
            SourceAttribution("Axios - Soft Skills 2025", "https://www.axios.com")
        )
    )
}
