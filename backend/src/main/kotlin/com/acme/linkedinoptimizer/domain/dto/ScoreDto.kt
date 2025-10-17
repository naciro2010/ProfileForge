package com.acme.linkedinoptimizer.domain.dto

data class ScoreDto(
    val total: Int,
    val breakdown: Map<String, Int>,
    val warnings: List<String>
)
