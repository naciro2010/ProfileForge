package com.acme.linkedinoptimizer.api

import com.acme.linkedinoptimizer.compensation.CompensationService
import com.acme.linkedinoptimizer.model.CompensationRequest
import com.acme.linkedinoptimizer.model.CompensationResponse
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
class CompensationController(private val compensationService: CompensationService) {

    @PostMapping("/compensation")
    fun estimate(@Valid @RequestBody request: CompensationRequest): ResponseEntity<CompensationResponse> {
        val response = compensationService.estimate(request)
        return ResponseEntity.ok(response)
    }
}
