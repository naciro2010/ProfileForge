package com.acme.linkedinoptimizer.api

import com.acme.linkedinoptimizer.domain.LlmProvider
import com.acme.linkedinoptimizer.domain.SuggestionContext
import com.acme.linkedinoptimizer.domain.dto.SuggestionDto
import com.acme.linkedinoptimizer.domain.dto.SuggestionRequest
import com.acme.linkedinoptimizer.service.SuggestionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Validated
class SuggestController(private val suggestionService: SuggestionService) {

    @PostMapping("/suggest")
    fun suggest(@Valid @RequestBody request: SuggestionRequest): ResponseEntity<SuggestionDto> {
        val context = if (request.provider != null || request.model != null || request.language != null) {
            SuggestionContext(
                provider = runCatching { LlmProvider.valueOf((request.provider ?: "ollama").uppercase()) }
                    .getOrDefault(LlmProvider.OLLAMA),
                model = request.model ?: "llama3:instruct",
                language = request.language ?: "fr"
            )
        } else {
            null
        }
        val suggestion = suggestionService.suggest(request.profile, context)
        return ResponseEntity.ok(suggestion)
    }
}
