package com.acme.linkedinoptimizer.service

import com.acme.linkedinoptimizer.domain.LlmProvider
import com.acme.linkedinoptimizer.domain.SuggestionContext
import com.acme.linkedinoptimizer.domain.dto.ExperienceSuggestion
import com.acme.linkedinoptimizer.domain.dto.ProfileDto
import com.acme.linkedinoptimizer.domain.dto.SuggestionDto
import com.acme.linkedinoptimizer.service.llm.LlmClient
import com.acme.linkedinoptimizer.util.TextUtils
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SuggestionService(
    private val llmClients: List<LlmClient>,
    private val textUtils: TextUtils,
    private val objectMapper: ObjectMapper,
    @Value("\${linkedin-optimizer.llm.cache-ttl:PT5M}") private val cacheTtl: Duration,
    @Value("\${linkedin-optimizer.llm.provider:ollama}") private val defaultProvider: String,
    @Value("\${linkedin-optimizer.llm.model:llama3:instruct}") private val defaultModel: String,
    @Value("\${linkedin-optimizer.llm.language:fr}") private val defaultLanguage: String
) {
    private data class CachedSuggestion(val key: String, val createdAt: Instant, val suggestion: SuggestionDto)

    private val cache = ConcurrentHashMap<String, CachedSuggestion>()
    private val clientsByProvider = llmClients.associateBy { it.provider }

    fun suggest(
        profile: ProfileDto,
        context: SuggestionContext? = null,
        targetRole: String? = null,
        targetDescription: String? = null
    ): SuggestionDto {
        val resolvedContext = context ?: SuggestionContext(
            provider = parseProvider(defaultProvider),
            model = defaultModel,
            language = defaultLanguage
        )

        val cacheKey = buildCacheKey(profile, resolvedContext, targetRole, targetDescription)
        val cached = cache[cacheKey]
        if (cached != null && Instant.now().isBefore(cached.createdAt.plus(cacheTtl))) {
            return cached.suggestion
        }

        val client = clientsByProvider[resolvedContext.provider]
            ?: error("No LLM client registered for provider ${resolvedContext.provider}")

        val prompt = buildPrompt(profile, resolvedContext, targetRole, targetDescription)
        val raw = runBlocking { client.generate(resolvedContext.model, prompt) }
        val suggestion = parseSuggestion(raw, profile)
        cache[cacheKey] = CachedSuggestion(cacheKey, Instant.now(), suggestion)
        return suggestion
    }

    private fun buildCacheKey(
        profile: ProfileDto,
        context: SuggestionContext,
        targetRole: String?,
        targetDescription: String?
    ): String {
        val base = listOf(
            profile.url,
            profile.headline.orEmpty(),
            profile.about.orEmpty().take(200),
            profile.skills.joinToString(separator = ","),
            context.provider.name,
            context.model,
            context.language,
            targetRole.orEmpty(),
            targetDescription.orEmpty().take(200)
        )
        return base.joinToString(separator = "|")
    }

    private fun buildPrompt(
        profile: ProfileDto,
        context: SuggestionContext,
        targetRole: String?,
        targetDescription: String?
    ): String {
        val languageInstruction = if (context.language.lowercase() == "en") {
            "Write in English."
        } else {
            "Rédige en français."
        }
        val targetBlock = if (!targetRole.isNullOrBlank()) {
            """
            Cible métier: $targetRole
            Description du poste: ${targetDescription.orEmpty()}
            """.trimIndent()
        } else ""

        val experiencesText = profile.experiences.joinToString(separator = "\n") { experience ->
            """
            - Titre: ${experience.title}
              Entreprise: ${experience.company}
              Période: ${experience.dates.orEmpty()}
              Description: ${experience.description.orEmpty()}
            """.trimIndent()
        }

        return """
            Tu es un coach LinkedIn senior. $languageInstruction
            Génère une réponse JSON stricte avec le schéma suivant:
            {
              "headline": "string <= 220 chars",
              "about": "3 à 5 paragraphes",
              "skills": ["mot-clé", ... 10 à 12 valeurs],
              "experienceBullets": [
                {
                  "company": "nom",
                  "title": "poste",
                  "bullets": ["action + métrique", ...]
                }
              ]
            }
            Contraintes:
            - Headline <= 220 caractères, ton concret, inclure compétences principales.
            - Section About : 3 à 5 paragraphes, verbs d'action, résultats chiffrés.
            - Skills : 5 à 12 mots-clés distincts.
            - Chaque bullet d'expérience commence par un verbe d'action et inclut métrique (%/€/chiffres) quand possible.
            - Respecte la persona LinkedIn professionnelle.
            - Pas de markdown supplémentaire.
            $targetBlock

            Profil actuel:
            Nom: ${profile.fullName.orEmpty()}
            Headline: ${profile.headline.orEmpty()}
            À propos: ${profile.about.orEmpty()}
            Localisation: ${profile.location.orEmpty()}
            Compétences: ${profile.skills.joinToString()}
            Expériences:
            $experiencesText
        """.trimIndent()
    }

    private fun parseSuggestion(raw: String, profile: ProfileDto): SuggestionDto {
        val root: JsonNode = try {
            objectMapper.readTree(raw)
        } catch (ex: Exception) {
            val fallback = objectMapper.createObjectNode().apply {
                put("headline", profile.headline.orEmpty())
                put("about", profile.about.orEmpty())
                putArray("skills").addAll(profile.skills.map { objectMapper.nodeFactory.textNode(it) })
                putArray("experienceBullets")
            }
            fallback
        }
        val headline = textUtils.truncate(textUtils.sanitize(root.path("headline").asText(profile.headline.orEmpty())), 220)
        val about = textUtils.enforceParagraphCount(textUtils.sanitize(root.path("about").asText(profile.about.orEmpty())))
        val skills = root.path("skills")
            .map { it.asText() }
            .filter { it.isNotBlank() }
            .map { textUtils.sanitize(it) }
            .distinct()
            .take(12)
        val experienceBullets = root.path("experienceBullets")
            .map { node ->
                ExperienceSuggestion(
                    company = textUtils.sanitize(node.path("company").asText()),
                    title = textUtils.sanitize(node.path("title").asText()),
                    bullets = node.path("bullets")
                        .map { bullet -> textUtils.sanitize(bullet.asText()) }
                        .filter { it.isNotBlank() }
                        .map { ensureActionVerb(it) }
                        .take(5)
                )
            }
        return SuggestionDto(
            headline = headline,
            about = about,
            skills = skills,
            experienceBullets = experienceBullets
        )
    }

    private fun parseProvider(value: String): LlmProvider = try {
        LlmProvider.valueOf(value.uppercase())
    } catch (_: IllegalArgumentException) {
        LlmProvider.OLLAMA
    }

    private fun ensureActionVerb(bullet: String): String {
        val verbs = listOf("Piloté", "Dirigé", "Optimisé", "Déployé", "Créé", "Développé", "Accéléré")
        val sanitized = textUtils.sanitize(bullet)
        return if (verbs.any { sanitized.startsWith(it, ignoreCase = true) }) {
            sanitized
        } else {
            "Optimisé $sanitized"
        }
    }
}
