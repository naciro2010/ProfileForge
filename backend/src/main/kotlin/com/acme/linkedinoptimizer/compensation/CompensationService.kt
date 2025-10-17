package com.acme.linkedinoptimizer.compensation

import com.acme.linkedinoptimizer.model.CompensationPeriod
import com.acme.linkedinoptimizer.model.CompensationRequest
import com.acme.linkedinoptimizer.model.CompensationResponse
import com.acme.linkedinoptimizer.model.CompanyType
import com.acme.linkedinoptimizer.model.ContractType
import com.acme.linkedinoptimizer.model.SeniorityLevel
import com.acme.linkedinoptimizer.model.SourceAttribution
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CompensationService(private val providers: List<CompensationDataProvider>) {

    private val logger = LoggerFactory.getLogger(CompensationService::class.java)
    private val freelanceSources = listOf(
        SourceAttribution("BDM - Baromètre Freelance 2025", "https://www.blogdumoderateur.com"),
        SourceAttribution("Silkhom TJM 2024", "https://www.silkhom.com"),
        SourceAttribution("Hays Salary Guide 2025", "https://www.hays.fr")
    )

    fun estimate(request: CompensationRequest): CompensationResponse {
        val provider = providers.firstOrNull { it.supports(request.country) }
            ?: providers.firstOrNull()
        if (provider == null) {
            logger.warn("No compensation provider configured; returning defaults")
            return defaultResponse(request)
        }
        val baseline = provider.baseline(request.role, request.region)
            ?: provider.baseline("generic", request.region)
            ?: return defaultResponse(request)

        val multiplier = computeMultiplier(request)
        val low = baseline.p25.multiply(multiplier).setScale(0, RoundingMode.HALF_UP)
        val median = baseline.median.multiply(multiplier).setScale(0, RoundingMode.HALF_UP)
        val high = baseline.p75.multiply(multiplier).setScale(0, RoundingMode.HALF_UP)
        val currency = request.currency ?: baseline.currency
        val justifications = buildJustifications(baseline.notes, request)
        val sources = (provider.sources + if (request.contractType == ContractType.FREELANCE) freelanceSources else emptyList())
            .distinctBy { it.url }

        return if (request.contractType == ContractType.FREELANCE) {
            val daily = convertToDaily(low, median, high, request.country)
            CompensationResponse(
                currency = currency,
                period = CompensationPeriod.DAILY,
                low = daily.first,
                median = daily.second,
                high = daily.third,
                justifications = justifications + "Conversion TJM : charges & congés pris en compte",
                sources = sources
            )
        } else {
            CompensationResponse(
                currency = currency,
                period = CompensationPeriod.ANNUAL,
                low = low,
                median = median,
                high = high,
                justifications = justifications,
                sources = sources
            )
        }
    }

    private fun computeMultiplier(request: CompensationRequest): BigDecimal {
        val seniorityMultiplier = when (request.seniority ?: SeniorityLevel.MID) {
            SeniorityLevel.JUNIOR -> BigDecimal("0.85")
            SeniorityLevel.MID -> BigDecimal.ONE
            SeniorityLevel.SENIOR -> BigDecimal("1.15")
            SeniorityLevel.LEAD -> BigDecimal("1.25")
        }

        val companyMultiplier = when (request.companyType ?: CompanyType.SME) {
            CompanyType.STARTUP -> BigDecimal("0.95")
            CompanyType.SME -> BigDecimal.ONE
            CompanyType.SCALEUP -> BigDecimal("1.05")
            CompanyType.ENTERPRISE -> BigDecimal("1.10")
        }

        val industryMultiplier = when {
            request.industry.isNullOrBlank() -> BigDecimal.ONE
            request.industry.contains("data", ignoreCase = true) -> BigDecimal("1.05")
            request.industry.contains("cyber", ignoreCase = true) -> BigDecimal("1.08")
            request.industry.contains("ia", ignoreCase = true) -> BigDecimal("1.10")
            else -> BigDecimal.ONE
        }

        return seniorityMultiplier.multiply(companyMultiplier).multiply(industryMultiplier)
    }

    private fun convertToDaily(low: BigDecimal, median: BigDecimal, high: BigDecimal, country: String): Triple<BigDecimal, BigDecimal, BigDecimal> {
        val billingDays = BigDecimal("218")
        val coefficient = when (country.uppercase()) {
            "FR" -> BigDecimal("1.9")
            "UK" -> BigDecimal("1.7")
            "US" -> BigDecimal("1.6")
            else -> BigDecimal("1.8")
        }
        val divisor = billingDays
        return Triple(
            low.divide(divisor, 2, RoundingMode.HALF_UP).multiply(coefficient).setScale(0, RoundingMode.HALF_UP),
            median.divide(divisor, 2, RoundingMode.HALF_UP).multiply(coefficient).setScale(0, RoundingMode.HALF_UP),
            high.divide(divisor, 2, RoundingMode.HALF_UP).multiply(coefficient).setScale(0, RoundingMode.HALF_UP)
        )
    }

    private fun buildJustifications(baselineNotes: String, request: CompensationRequest): List<String> {
        val reasons = mutableListOf(baselineNotes)
        when (request.seniority ?: SeniorityLevel.MID) {
            SeniorityLevel.JUNIOR -> reasons.add("Séniorité junior : -15 % vs médian")
            SeniorityLevel.SENIOR -> reasons.add("Séniorité senior : +15 % vs médian")
            SeniorityLevel.LEAD -> reasons.add("Séniorité lead : +25 % vs médian")
            else -> Unit
        }
        when (request.companyType ?: CompanyType.SME) {
            CompanyType.STARTUP -> reasons.add("Startup : package plus léger mais stock options possibles")
            CompanyType.SCALEUP -> reasons.add("ETI/Scale-up : +5 % pour attractivité")
            CompanyType.ENTERPRISE -> reasons.add("Grand groupe : +10 % lié aux grilles salariales")
            else -> Unit
        }
        if (!request.industry.isNullOrBlank()) {
            reasons.add("Secteur ${request.industry} : ajustement marché appliqué")
        }
        return reasons
    }

    private fun defaultResponse(request: CompensationRequest): CompensationResponse {
        val currency = request.currency ?: "EUR"
        return CompensationResponse(
            currency = currency,
            period = CompensationPeriod.ANNUAL,
            low = BigDecimal("45000"),
            median = BigDecimal("55000"),
            high = BigDecimal("65000"),
            justifications = listOf("Fourchette par défaut — données publiques indisponibles"),
            sources = freelanceSources
        )
    }
}
