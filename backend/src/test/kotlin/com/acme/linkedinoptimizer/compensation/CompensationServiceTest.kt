package com.acme.linkedinoptimizer.compensation

import com.acme.linkedinoptimizer.model.CompensationPeriod
import com.acme.linkedinoptimizer.model.CompensationRequest
import com.acme.linkedinoptimizer.model.CompanyType
import com.acme.linkedinoptimizer.model.ContractType
import com.acme.linkedinoptimizer.model.SeniorityLevel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CompensationServiceTest {

    private val service = CompensationService(listOf(ApecInseeProvider()))

    @Test
    fun `should adjust salary by seniority and company type`() {
        val request = CompensationRequest(
            role = "Data Analyst",
            country = "FR",
            region = "Île-de-France",
            seniority = SeniorityLevel.SENIOR,
            companyType = CompanyType.ENTERPRISE,
            contractType = ContractType.PERMANENT
        )

        val response = service.estimate(request)

        assertThat(response.period).isEqualTo(CompensationPeriod.ANNUAL)
        assertThat(response.median.toInt()).isGreaterThan(60000)
        assertThat(response.justifications).anyMatch { it.contains("Séniorité") }
        assertThat(response.sources).isNotEmpty()
    }

    @Test
    fun `should convert to daily rate for freelance`() {
        val request = CompensationRequest(
            role = "Product Manager",
            country = "FR",
            contractType = ContractType.FREELANCE
        )

        val response = service.estimate(request)

        assertThat(response.period).isEqualTo(CompensationPeriod.DAILY)
        assertThat(response.low.toInt()).isGreaterThan(200)
        assertThat(response.sources).anyMatch { it.name.contains("BDM") }
    }
}
