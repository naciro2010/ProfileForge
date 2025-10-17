package com.acme.linkedinoptimizer.api

import com.acme.linkedinoptimizer.model.SuggestRequest
import com.acme.linkedinoptimizer.model.SuggestionResponse
import com.acme.linkedinoptimizer.suggestion.SuggestionService
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
    fun suggest(@Valid @RequestBody request: SuggestRequest): ResponseEntity<SuggestionResponse> {
        val response = suggestionService.suggest(request.profile, request.targetRole, request.language)
        return ResponseEntity.ok(response)
    }
}
