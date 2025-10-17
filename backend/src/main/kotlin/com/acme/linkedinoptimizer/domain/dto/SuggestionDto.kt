package com.acme.linkedinoptimizer.domain.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class ExperienceSuggestion(
    val company: String,
    val title: String,
    val bullets: List<String>
)

data class SuggestionDto(
    val headline: String,
    val about: String,
    val skills: List<String>,
    val experienceBullets: List<ExperienceSuggestion>
)

data class SuggestionRequest(
    @field:Valid
    val profile: ProfileDto,
    val provider: String? = null,
    val model: String? = null,
    val language: String? = null
)

data class TargetSuggestionRequest(
    @field:Valid
    val profile: ProfileDto,
    @field:NotBlank(message = "Target role is required")
    val targetRole: String,
    val targetDescription: String? = null
)
