package com.acme.linkedinoptimizer.domain.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class ExperienceDto(
    val title: String,
    val company: String,
    val dates: String? = null,
    val description: String? = null
)

data class ProfileDto(
    @field:NotBlank(message = "Profile URL is required")
    val url: String,
    val locale: String? = null,
    val fullName: String? = null,
    val headline: String? = null,
    val about: String? = null,
    val location: String? = null,
    @field:Size(min = 0, message = "Experiences must not be null")
    @field:Valid
    val experiences: List<ExperienceDto> = emptyList(),
    @field:Size(min = 0, message = "Skills must not be null")
    val skills: List<String> = emptyList(),
    val hasPhoto: Boolean? = null
)
