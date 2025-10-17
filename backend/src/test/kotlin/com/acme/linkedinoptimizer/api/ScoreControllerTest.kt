package com.acme.linkedinoptimizer.api

import com.acme.linkedinoptimizer.model.ExperienceSnapshot
import com.acme.linkedinoptimizer.model.ProfileSnapshot
import com.acme.linkedinoptimizer.model.ScoreRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest(properties = ["api.key=test-key"])
@AutoConfigureMockMvc
class ScoreControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @Test
    fun `should reject missing api key`() {
        mockMvc.post("/api/v1/score") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ScoreRequest(profile = ProfileSnapshot()))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return score when api key provided`() {
        val request = ScoreRequest(
            profile = ProfileSnapshot(
                headline = "Data Analyst",
                about = "Je transforme les données en décisions.",
                skills = listOf("SQL"),
                experiences = listOf(
                    ExperienceSnapshot(
                        role = "Analyst",
                        company = "Acme",
                        achievements = "Réduction des coûts de 10%",
                        timeframe = "2023"
                    )
                )
            )
        )

        mockMvc.post("/api/v1/score") {
            header("X-API-Key", "test-key")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
        }
    }
}
