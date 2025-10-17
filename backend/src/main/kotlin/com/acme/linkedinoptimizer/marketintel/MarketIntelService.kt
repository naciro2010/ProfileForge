package com.acme.linkedinoptimizer.marketintel

import com.acme.linkedinoptimizer.model.MarketIntelRequest
import com.acme.linkedinoptimizer.model.MarketIntelResponse
import com.acme.linkedinoptimizer.model.RecruiterSignal
import com.acme.linkedinoptimizer.model.SkillInsight
import com.acme.linkedinoptimizer.model.SourceAttribution
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class MarketIntelService(
    private val providers: List<MarketIntelProvider>
) {

    private val logger = LoggerFactory.getLogger(MarketIntelService::class.java)
    private val cache: Cache<CacheKey, MarketIntelResponse> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofDays(30))
        .maximumSize(200)
        .build()

    private val warmupTargets = listOf(
        MarketIntelRequest(role = "Data Analyst", country = "FR"),
        MarketIntelRequest(role = "Product Manager", country = "US"),
        MarketIntelRequest(role = "Software Engineer", country = "UK")
    )

    fun marketIntel(request: MarketIntelRequest): MarketIntelResponse {
        val key = CacheKey(request)
        return cache.get(key) { aggregate(request) }
    }

    @Scheduled(fixedDelayString = "PT12H")
    fun warmupCache() {
        warmupTargets.forEach { request ->
            runCatching { marketIntel(request) }
                .onFailure { logger.warn("Unable to warm market intel cache for {}: {}", request.role, it.message) }
        }
    }

    private fun aggregate(request: MarketIntelRequest): MarketIntelResponse {
        val matchingProviders = providers.filter { it.supports(request.country) }
        if (matchingProviders.isEmpty()) {
            logger.warn("No provider configured for country {}", request.country)
        }
        val results = matchingProviders.ifEmpty { providers }
            .map { provider -> provider.fetch(request) }

        val refreshedAt = results.maxOfOrNull { it.refreshedAt } ?: Instant.now()
        val skills = deduplicateSkills(results.flatMap { it.skills })
        val signals = deduplicateSignals(results.flatMap { it.recruiterSignals })
        val sources = deduplicateSources(results.flatMap { it.sources })

        return MarketIntelResponse(
            refreshedAt = refreshedAt,
            skills = skills,
            recruiterSignals = signals,
            sources = sources
        )
    }

    private fun deduplicateSkills(skills: List<SkillInsight>): List<SkillInsight> {
        val seen = mutableSetOf<String>()
        return skills.filter { skill ->
            val key = "${skill.category}:${skill.name.lowercase()}"
            seen.add(key)
        }.distinctBy { it.name.lowercase() + it.category }
    }

    private fun deduplicateSignals(signals: List<RecruiterSignal>): List<RecruiterSignal> =
        signals.distinctBy { it.statement.lowercase() }

    private fun deduplicateSources(sources: List<SourceAttribution>): List<SourceAttribution> =
        sources.distinctBy { it.url }

    private data class CacheKey(
        val role: String,
        val country: String,
        val seniority: String?,
        val industry: String?
    ) {
        constructor(request: MarketIntelRequest) : this(
            role = request.role.lowercase(),
            country = request.country.uppercase(),
            seniority = request.seniority?.name,
            industry = request.industry?.lowercase()
        )
    }
}
