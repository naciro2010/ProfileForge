package com.acme.linkedinoptimizer.score

import com.acme.linkedinoptimizer.model.ProfileSnapshot
import com.acme.linkedinoptimizer.model.ScoreResponse
import org.springframework.stereotype.Service
import kotlin.math.roundToInt

@Service
class ScoreService {

    private val actionVerbs = setOf("led", "built", "boosted", "created", "delivered", "scaled", "piloted", "improved")
    private val numericRegex = Regex("\\d+%?")

    fun score(profile: ProfileSnapshot, keywords: List<String>): ScoreResponse {
        val breakdown = mutableMapOf<String, Int>()
        val warnings = mutableListOf<String>()
        var total = 0

        val headlineScore = when {
            profile.headline.isNullOrBlank() -> {
                warnings.add("Ajoutez un headline clair (≤ 220 caractères).")
                0
            }
            profile.headline.length > 220 -> {
                warnings.add("Le headline dépasse 220 caractères.")
                6
            }
            else -> 12
        }
        breakdown["headline"] = headlineScore
        total += headlineScore

        val aboutScore = when {
            profile.about.isNullOrBlank() -> {
                warnings.add("Rédigez une section À propos structurée (200+ caractères).")
                0
            }
            profile.about.length < 400 -> 8
            profile.about.length < 800 -> 12
            else -> 15
        }
        breakdown["about"] = aboutScore
        total += aboutScore

        val experienceScore = profile.experiences.sumOf { experience ->
            var score = 6
            val lowered = experience.achievements.lowercase()
            if (actionVerbs.any { lowered.contains(it) }) score += 4
            if (numericRegex.containsMatchIn(experience.achievements)) score += 4
            score.coerceAtMost(14)
        }
        if (profile.experiences.isEmpty()) {
            warnings.add("Ajoutez au moins une expérience récente avec des métriques.")
        }
        val cappedExperience = experienceScore.coerceAtMost(28)
        breakdown["experience"] = cappedExperience
        total += cappedExperience

        val skillsScore = when {
            profile.skills.size >= 12 -> 15
            profile.skills.size >= 8 -> 12
            profile.skills.size >= 5 -> 8
            profile.skills.isEmpty() -> {
                warnings.add("Ajoutez vos compétences clés.")
                0
            }
            else -> 5
        }
        breakdown["skills"] = skillsScore
        total += skillsScore

        val locationScore = if (!profile.location.isNullOrBlank()) 6 else 0
        if (locationScore == 0) {
            warnings.add("Indiquez votre localisation pour favoriser les recherches locales.")
        }
        breakdown["location"] = locationScore
        total += locationScore

        val keywordScore = if (keywords.isEmpty()) {
            6
        } else {
            val matchCount = keywords.count { keyword ->
                val lower = keyword.lowercase()
                profile.headline.orEmpty().lowercase().contains(lower) ||
                    profile.about.orEmpty().lowercase().contains(lower) ||
                    profile.skills.any { it.lowercase().contains(lower) }
            }
            (matchCount.toDouble() / keywords.size.toDouble()).coerceIn(0.0, 1.0)
                .times(15.0)
                .roundToInt()
        }
        if (keywordScore < 9 && keywords.isNotEmpty()) {
            warnings.add("Intégrez davantage les mots-clés du poste ciblé.")
        }
        breakdown["keywords"] = keywordScore
        total += keywordScore

        if (profile.hasPhoto != true) {
            warnings.add("Ajoutez une photo professionnelle.")
        } else {
            breakdown["photo"] = 4
            total += 4
        }

        val normalized = total.coerceIn(0, 100)
        return ScoreResponse(total = normalized, breakdown = breakdown, warnings = warnings)
    }
}
