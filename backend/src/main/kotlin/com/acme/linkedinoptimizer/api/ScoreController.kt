package com.acme.linkedinoptimizer.api

import com.acme.linkedinoptimizer.domain.dto.ProfileDto
import com.acme.linkedinoptimizer.domain.dto.ScoreDto
import com.acme.linkedinoptimizer.service.ScoringService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Validated
class ScoreController(private val scoringService: ScoringService) {

    @PostMapping("/score")
    fun score(
        @Valid @RequestBody profile: ProfileDto,
        @RequestParam(name = "keywords", required = false) keywords: List<String>?
    ): ResponseEntity<ScoreDto> {
        val score = scoringService.score(profile, keywords ?: emptyList())
        return ResponseEntity.ok(score)
    }
}
