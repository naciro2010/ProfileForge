package com.acme.linkedinoptimizer.service

import com.acme.linkedinoptimizer.domain.dto.ProfileDto
import com.acme.linkedinoptimizer.domain.dto.ScoreDto
import org.springframework.stereotype.Service

@Service
class ScoringService {
    private val actionVerbs = listOf("led", "managed", "built", "boosted", "created", "optimized", "developed", "implemented")

    fun score(profile: ProfileDto, targetKeywords: List<String> = emptyList()): ScoreDto {
        val breakdown = mutableMapOf<String, Int>()
        val warnings = mutableListOf<String>()
        var total = 0

        val photoScore = if (profile.hasPhoto == true) 10 else 0
        if (photoScore == 0) warnings.add("Ajoutez une photo professionnelle.")
        breakdown["photo"] = photoScore
        total += photoScore

        val headlineScore = if (!profile.headline.isNullOrBlank()) 10 else 0
        if (headlineScore == 0) warnings.add("Complétez votre headline avec un pitch clair.")
        breakdown["headline"] = headlineScore
        total += headlineScore

        val aboutLength = profile.about?.length ?: 0
        val aboutScore = when {
            aboutLength >= 800 -> 15
            aboutLength >= 400 -> 10
            aboutLength >= 200 -> 6
            else -> 0
        }
        if (aboutScore < 10) warnings.add("Rédigez une section À propos détaillée (400+ caractères).")
        breakdown["about"] = aboutScore
        total += aboutScore

        val experiences = profile.experiences
        val experienceScore = when {
            experiences.size >= 3 -> 15
            experiences.size >= 2 -> 10
            experiences.size == 1 -> 5
            else -> 0
        }
        if (experienceScore < 10) warnings.add("Ajoutez au moins deux expériences récentes.")
        breakdown["experiences"] = experienceScore
        total += experienceScore

        val achievementScore = experiences.sumOf { experience ->
            val description = experience.description?.lowercase().orEmpty()
            val verbScore = if (actionVerbs.any { description.contains(it) }) 3 else 0
            val metricScore = if (Regex("\\d+").containsMatchIn(description)) 2 else 0
            verbScore + metricScore
        }.coerceAtMost(15)
        if (achievementScore < 9) warnings.add("Ajoutez des verbes d'action et des métriques dans vos expériences.")
        breakdown["achievements"] = achievementScore
        total += achievementScore

        val skillsCount = profile.skills.size
        val skillsScore = when {
            skillsCount >= 12 -> 15
            skillsCount >= 10 -> 12
            skillsCount >= 6 -> 8
            skillsCount > 0 -> 4
            else -> 0
        }
        if (skillsScore < 12) warnings.add("Ajoutez au moins 10 compétences pertinentes.")
        breakdown["skills"] = skillsScore
        total += skillsScore

        val locationScore = if (!profile.location.isNullOrBlank()) 5 else 0
        if (locationScore == 0) warnings.add("Indiquez votre localisation.")
        breakdown["location"] = locationScore
        total += locationScore

        val keywordMatches = targetKeywords.count { keyword ->
            val lowerKeyword = keyword.lowercase()
            profile.headline.orEmpty().lowercase().contains(lowerKeyword) ||
                profile.about.orEmpty().lowercase().contains(lowerKeyword) ||
                profile.skills.any { it.lowercase().contains(lowerKeyword) }
        }
        val keywordScore = (keywordMatches.coerceAtMost(5)) * 3
        if (targetKeywords.isNotEmpty() && keywordScore < 9) warnings.add("Intégrez les mots-clés du poste ciblé.")
        breakdown["keywords"] = keywordScore
        total += keywordScore

        val normalized = total.coerceIn(0, 100)
        return ScoreDto(total = normalized, breakdown = breakdown, warnings = warnings)
    }
}
