package com.acme.linkedinoptimizer.api

import com.acme.linkedinoptimizer.domain.dto.ProfileDto
import com.acme.linkedinoptimizer.domain.dto.ScoreDto
import com.acme.linkedinoptimizer.service.ScoringService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [ScoreController::class])
@TestPropertySource(properties = ["api.key=changeme"])
class ScoreControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var scoringService: ScoringService

    @Test
    fun `should return score when API key valid`() {
        whenever(scoringService.score(any(), any())).thenReturn(ScoreDto(total = 80, breakdown = mapOf("headline" to 10), warnings = emptyList()))

        val profile = ProfileDto(url = "https://linkedin.com/in/test")

        mockMvc.perform(
            post("/api/v1/score")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-Key", "changeme")
                .content(objectMapper.writeValueAsString(profile))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total").value(80))
    }
}
