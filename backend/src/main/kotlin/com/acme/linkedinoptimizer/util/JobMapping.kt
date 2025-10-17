package com.acme.linkedinoptimizer.util

import org.springframework.stereotype.Component
import java.util.Locale

@Component
class JobMapping {

    private val catalog = listOf(
        JobMetadata(
            aliases = setOf("data analyst", "analyste data", "analyste données"),
            normalized = "Data Analyst",
            escoCode = "251101",
            coreSkills = listOf("SQL", "Tableau", "Data storytelling", "Python"),
            trendingSkills = listOf("Power BI", "Automation", "IA générative"),
            niceToHave = listOf("dbt", "Looker Studio", "Communication client")
        ),
        JobMetadata(
            aliases = setOf("product manager", "chef de produit", "pm"),
            normalized = "Product Manager",
            escoCode = "243120",
            coreSkills = listOf("Discovery", "Priorisation", "Roadmapping", "Analyse utilisateur"),
            trendingSkills = listOf("IA appliquée", "Product analytics", "Activation"),
            niceToHave = listOf("OKR", "Monétisation", "Leadership transversal")
        ),
        JobMetadata(
            aliases = setOf("software engineer", "développeur", "full stack"),
            normalized = "Software Engineer",
            escoCode = "251201",
            coreSkills = listOf("Architecture", "Testing", "CI/CD", "Cloud"),
            trendingSkills = listOf("IA copilote", "Platform engineering", "Observabilité"),
            niceToHave = listOf("DevRel", "Sécurité applicative", "Mentorat")
        )
    )

    fun metadataFor(input: String?): JobMetadata? {
        if (input.isNullOrBlank()) return null
        val normalizedInput = input.lowercase(Locale.getDefault())
        return catalog.firstOrNull { metadata ->
            metadata.aliases.any { alias -> normalizedInput.contains(alias) }
        }
    }

    data class JobMetadata(
        val aliases: Set<String>,
        val normalized: String,
        val escoCode: String,
        val coreSkills: List<String>,
        val trendingSkills: List<String>,
        val niceToHave: List<String>
    )
}
