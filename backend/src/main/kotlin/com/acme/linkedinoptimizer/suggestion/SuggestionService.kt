package com.acme.linkedinoptimizer.suggestion

import com.acme.linkedinoptimizer.model.CopyBlock
import com.acme.linkedinoptimizer.model.ExperienceSuggestion
import com.acme.linkedinoptimizer.model.ProfileSnapshot
import com.acme.linkedinoptimizer.model.SkillSuggestions
import com.acme.linkedinoptimizer.model.SuggestionLanguage
import com.acme.linkedinoptimizer.model.SuggestionResponse
import com.acme.linkedinoptimizer.suggestion.llm.LlmClient
import com.acme.linkedinoptimizer.suggestion.llm.OllamaClient
import com.acme.linkedinoptimizer.suggestion.llm.OpenRouterClient
import com.acme.linkedinoptimizer.util.JobMapping
import com.acme.linkedinoptimizer.util.TextUtils
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class SuggestionService(
    private val properties: LlmProperties,
    private val ollamaClient: OllamaClient,
    private val openRouterClient: OpenRouterClient,
    private val textUtils: TextUtils,
    private val jobMapping: JobMapping
) {

    private val metricRegex = Regex("(\\d+[,.]\\d*%?)")

    fun suggest(profile: ProfileSnapshot, explicitTargetRole: String?, language: SuggestionLanguage?): SuggestionResponse {
        val targetRole = explicitTargetRole ?: profile.targetRole ?: inferRole(profile)
        val metadata = jobMapping.metadataFor(targetRole)
        val normalizedRole = metadata?.normalized ?: targetRole ?: "Professional"
        val skills = buildSkills(metadata, profile)

        val headline = buildHeadlines(normalizedRole, profile, skills)
        val about = buildAbout(normalizedRole, profile, skills)
        val experiences = buildExperienceSuggestions(profile)

        val llmBlocks = callLlm(profile, normalizedRole, language ?: SuggestionLanguage.valueOf(properties.defaultLanguage.uppercase(Locale.ROOT)))
        val combinedAbout = if (llmBlocks != null) about + llmBlocks else about

        val finalSkills = skills.copy(
            core = deduplicate(skills.core),
            trending = deduplicate(skills.trending),
            niceToHave = deduplicate(skills.niceToHave)
        )

        return SuggestionResponse(
            headline = headline,
            about = combinedAbout,
            skills = finalSkills,
            experiences = experiences
        )
    }

    private fun buildSkills(metadata: JobMapping.JobMetadata?, profile: ProfileSnapshot): SkillSuggestions {
        val baseSkills = profile.skills.map { textUtils.sanitize(it) }
        val core = (metadata?.coreSkills ?: baseSkills.take(6)).ifEmpty { baseSkills.take(6) }
        val trending = metadata?.trendingSkills ?: baseSkills.drop(6).take(4)
        val niceToHave = metadata?.niceToHave ?: baseSkills.takeLast(4)
        return SkillSuggestions(core = core, trending = trending, niceToHave = niceToHave)
    }

    private fun buildHeadlines(role: String, profile: ProfileSnapshot, skills: SkillSuggestions): List<CopyBlock> {
        val impact = extractImpact(profile)
        val location = profile.location?.takeIf { it.isNotBlank() }
        val topSkills = (skills.core + skills.trending).distinct().take(3)
        val metricsLabel = impact ?: "impact mesurable"

        val variants = listOf(
            CopyBlock(
                label = "Pitch orienté résultat",
                content = "$role | ${topSkills.joinToString(" • ")} | +$metricsLabel"
            ),
            CopyBlock(
                label = "Positionnement marché",
                content = listOfNotNull(role, location, topSkills.firstOrNull()).joinToString(" • ")
            ),
            CopyBlock(
                label = "Approche valeur",
                content = "$role | ${skills.trending.firstOrNull() ?: "Focus data"} | ${impact ?: "ROI prouvé"}"
            )
        )
        return variants.map { it.copy(content = textUtils.truncate(it.content, 220)) }
    }

    private fun buildAbout(role: String, profile: ProfileSnapshot, skills: SkillSuggestions): List<CopyBlock> {
        val achievements = profile.experiences.flatMap { extractBullets(it.achievements) }
        val metricHighlights = achievements.filter { metricRegex.containsMatchIn(it) }.take(3)
        val storyParagraphs = listOfNotNull(
            "${role} avec ${skills.core.firstOrNull() ?: "expertise multi-domaines"}, j’aide les équipes à livrer des résultats mesurables.",
            if (metricHighlights.isNotEmpty()) "Impact démontré : ${metricHighlights.joinToString(separator = " · ")}." else null,
            if (skills.trending.isNotEmpty()) "Focus 2025 : ${skills.trending.joinToString(", ")} pour accélérer les projets IA/data." else null,
            "Ce que l’on dit de moi : ${skills.niceToHave.take(2).joinToString(" & ") { it.lowercase(Locale.getDefault()) }}."
        )
        val paragraphAlt = listOfNotNull(
            "Mission : transformer les données en décisions actionnables et fédérer les parties prenantes.",
            "Stack privilégiée : ${skills.core.take(3).joinToString(", ")}.",
            achievements.take(2).joinToString(" ")
        )

        return listOf(
            CopyBlock(
                label = "Narratif orienté impact",
                content = textUtils.enforceParagraphCount(storyParagraphs.joinToString("\n\n"))
            ),
            CopyBlock(
                label = "Résumé direct",
                content = textUtils.enforceParagraphCount(paragraphAlt.joinToString("\n\n"))
            )
        )
    }

    private fun buildExperienceSuggestions(profile: ProfileSnapshot): List<ExperienceSuggestion> {
        return profile.experiences.map { experience ->
            val bullets = extractBullets(experience.achievements).take(3)
            ExperienceSuggestion(
                role = experience.role,
                company = experience.company,
                bullets = bullets
            )
        }
    }

    private fun callLlm(profile: ProfileSnapshot, role: String, language: SuggestionLanguage): List<CopyBlock>? {
        val client = resolveClient()
        if (client == null) return null

        val prompt = buildString {
            appendLine("Tu es un coach LinkedIn. Tu écris en ${language.name.lowercase()}.")
            appendLine("Rôle ciblé : $role")
            appendLine("Headline actuel : ${profile.headline ?: ""}")
            appendLine("About actuel : ${profile.about ?: ""}")
            appendLine("Skills : ${profile.skills.joinToString(", ")}")
            appendLine("Expériences :")
            profile.experiences.forEach { exp ->
                appendLine("- ${exp.role} chez ${exp.company} : ${exp.achievements}")
            }
            appendLine("Donne 2 paragraphes About concis (3-5 phrases chacun). Format: ABOUT::titre||texte")
        }

        val response = client.generateCompletion(prompt)
        if (response.isBlank()) return null

        return response.lines()
            .filter { it.startsWith("ABOUT::") }
            .mapNotNull { line ->
                val parts = line.removePrefix("ABOUT::").split("||", limit = 2)
                if (parts.size == 2) {
                    CopyBlock(label = parts[0].trim().ifBlank { "LLM" }, content = parts[1].trim())
                } else {
                    null
                }
            }
            .takeIf { it.isNotEmpty() }
    }

    private fun resolveClient(): LlmClient? = when (properties.provider) {
        LlmProperties.Provider.OLLAMA -> ollamaClient
        LlmProperties.Provider.OPENROUTER -> openRouterClient
    }

    private fun extractImpact(profile: ProfileSnapshot): String? {
        profile.experiences.forEach { experience ->
            val match = metricRegex.find(experience.achievements)
            if (match != null) return match.value
        }
        return null
    }

    private fun extractBullets(text: String): List<String> {
        val sanitized = textUtils.sanitize(text)
        val fromLines = sanitized.split(Regex("[\n•-]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        return fromLines.ifEmpty { listOf(sanitized) }
    }

    private fun deduplicate(items: List<String>): List<String> = items.map { textUtils.sanitize(it) }
        .map { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() } }
        .distinct()

    private fun inferRole(profile: ProfileSnapshot): String? {
        return listOfNotNull(profile.targetRole, profile.headline, profile.experiences.firstOrNull()?.role)
            .firstOrNull { !it.isNullOrBlank() }
    }
}
