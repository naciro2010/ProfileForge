package com.acme.linkedinoptimizer.api

import com.acme.linkedinoptimizer.marketintel.MarketIntelService
import com.acme.linkedinoptimizer.model.MarketIntelRequest
import com.acme.linkedinoptimizer.model.MarketIntelResponse
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
class MarketController(private val marketIntelService: MarketIntelService) {

    @PostMapping("/market-intel")
    fun marketIntel(@Valid @RequestBody request: MarketIntelRequest): ResponseEntity<MarketIntelResponse> {
        val response = marketIntelService.marketIntel(request)
        return ResponseEntity.ok(response)
    }
}
