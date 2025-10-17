package com.acme.linkedinoptimizer.api

import com.acme.linkedinoptimizer.domain.LlmProvider
import com.acme.linkedinoptimizer.domain.SuggestionContext
import com.acme.linkedinoptimizer.domain.dto.SuggestionDto
import com.acme.linkedinoptimizer.domain.dto.TargetSuggestionRequest
import com.acme.linkedinoptimizer.service.SuggestionService
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
class TargetController(private val suggestionService: SuggestionService) {

    @PostMapping("/target")
    fun target(
        @Valid @RequestBody request: TargetSuggestionRequest,
        @RequestParam(required = false, name = "provider") provider: String?,
        @RequestParam(required = false, name = "model") model: String?,
        @RequestParam(required = false, name = "language") language: String?
    ): ResponseEntity<SuggestionDto> {
        val context = if (provider != null || model != null || language != null) {
            SuggestionContext(
                provider = runCatching { LlmProvider.valueOf((provider ?: "ollama").uppercase()) }
                    .getOrDefault(LlmProvider.OLLAMA),
                model = model ?: "llama3:instruct",
                language = language ?: "fr"
            )
        } else {
            null
        }
        val suggestion = suggestionService.suggest(
            profile = request.profile,
            context = context,
            targetRole = request.targetRole,
            targetDescription = request.targetDescription
        )
        return ResponseEntity.ok(suggestion)
    }
}
