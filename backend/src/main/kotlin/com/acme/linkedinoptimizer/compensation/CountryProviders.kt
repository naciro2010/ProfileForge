package com.acme.linkedinoptimizer.compensation

import com.acme.linkedinoptimizer.model.SourceAttribution
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

interface CompensationDataProvider {
    fun supports(country: String): Boolean
    fun baseline(role: String, region: String? = null): BaselineCompensation?
    fun defaultCurrency(country: String): String
    val sources: List<SourceAttribution>
}

data class BaselineCompensation(
    val p25: BigDecimal,
    val median: BigDecimal,
    val p75: BigDecimal,
    val currency: String,
    val notes: String
)

@Component
class BlsProvider : CompensationDataProvider {
    private val coverage = setOf("US")

    override fun supports(country: String): Boolean = coverage.contains(country.uppercase())

    override fun baseline(role: String, region: String?): BaselineCompensation? {
        val lower = role.lowercase(Locale.getDefault())
        val data = when {
            lower.contains("data") -> Triple(75_000, 95_000, 120_000)
            lower.contains("product") -> Triple(90_000, 120_000, 150_000)
            lower.contains("software") || lower.contains("engineer") -> Triple(100_000, 135_000, 180_000)
            else -> Triple(70_000, 90_000, 110_000)
        }
        val regionAdjustment = when {
            region.isNullOrBlank() -> 1.0
            region.contains("california", ignoreCase = true) -> 1.18
            region.contains("new york", ignoreCase = true) -> 1.2
            region.contains("texas", ignoreCase = true) -> 0.95
            else -> 1.0
        }
        val factor = regionAdjustment.toBigDecimal()
        return BaselineCompensation(
            p25 = data.first.toBigDecimal().multiply(factor),
            median = data.second.toBigDecimal().multiply(factor),
            p75 = data.third.toBigDecimal().multiply(factor),
            currency = defaultCurrency("US"),
            notes = "BLS OEWS 2024 — ajustement coût de la vie"
        )
    }

    override fun defaultCurrency(country: String): String = "USD"

    override val sources: List<SourceAttribution> = listOf(
        SourceAttribution("Bureau of Labor Statistics OEWS 2024", "https://www.bls.gov/oes/"),
        SourceAttribution("Cost of Living adjustments", "https://www.bls.gov/cpi/")
    )
}

@Component
class OnsAsheProvider : CompensationDataProvider {
    private val coverage = setOf("UK")

    override fun supports(country: String): Boolean = coverage.contains(country.uppercase())

    override fun baseline(role: String, region: String?): BaselineCompensation? {
        val lower = role.lowercase(Locale.getDefault())
        val data = when {
            lower.contains("data") -> Triple(38_000, 50_000, 65_000)
            lower.contains("product") -> Triple(45_000, 62_000, 80_000)
            lower.contains("software") -> Triple(50_000, 70_000, 90_000)
            else -> Triple(35_000, 45_000, 55_000)
        }
        val regionFactor = when {
            region.isNullOrBlank() -> 1.0
            region.contains("london", ignoreCase = true) -> 1.25
            region.contains("scotland", ignoreCase = true) -> 0.92
            else -> 1.0
        }
        val factor = regionFactor.toBigDecimal()
        return BaselineCompensation(
            p25 = data.first.toBigDecimal().multiply(factor),
            median = data.second.toBigDecimal().multiply(factor),
            p75 = data.third.toBigDecimal().multiply(factor),
            currency = defaultCurrency("UK"),
            notes = "ONS ASHE 2024 — ajustement régional"
        )
    }

    override fun defaultCurrency(country: String): String = "GBP"

    override val sources: List<SourceAttribution> = listOf(
        SourceAttribution("ONS ASHE 2024", "https://www.ons.gov.uk/"),
        SourceAttribution("London uplift 2024", "https://www.ons.gov.uk/employmentandlabourmarket/")
    )
}

@Component
class ApecInseeProvider : CompensationDataProvider {
    private val coverage = setOf("FR")

    override fun supports(country: String): Boolean = coverage.contains(country.uppercase())

    override fun baseline(role: String, region: String?): BaselineCompensation? {
        val lower = role.lowercase(Locale.getDefault())
        val data = when {
            lower.contains("data") -> Triple(42_000, 48_000, 60_000)
            lower.contains("product") -> Triple(45_000, 55_000, 68_000)
            lower.contains("software") || lower.contains("ingénieur") -> Triple(45_000, 55_000, 70_000)
            else -> Triple(38_000, 46_000, 58_000)
        }
        val regionFactor = when {
            region.isNullOrBlank() -> 1.0
            region.contains("paris", ignoreCase = true) || region.contains("île-de-france", ignoreCase = true) -> 1.12
            region.contains("lyon", ignoreCase = true) -> 1.05
            else -> 0.95
        }
        val factor = regionFactor.toBigDecimal()
        return BaselineCompensation(
            p25 = data.first.toBigDecimal().multiply(factor),
            median = data.second.toBigDecimal().multiply(factor),
            p75 = data.third.toBigDecimal().multiply(factor),
            currency = defaultCurrency("FR"),
            notes = "APEC 2024 & INSEE — cadres du numérique"
        )
    }

    override fun defaultCurrency(country: String): String = "EUR"

    override val sources: List<SourceAttribution> = listOf(
        SourceAttribution("APEC Baromètre 2024", "https://corporate.apec.fr"),
        SourceAttribution("INSEE Salaires", "https://www.insee.fr")
    )
}

@Component
class EurostatProvider : CompensationDataProvider {
    private val coverage = setOf("DE", "ES", "IT", "NL", "BE", "IE")

    override fun supports(country: String): Boolean = coverage.contains(country.uppercase())

    override fun baseline(role: String, region: String?): BaselineCompensation? {
        val lower = role.lowercase(Locale.getDefault())
        val baseMedian = when {
            lower.contains("data") -> 52_000
            lower.contains("product") -> 58_000
            lower.contains("software") -> 62_000
            else -> 46_000
        }
        return BaselineCompensation(
            p25 = (baseMedian * 0.8).toBigDecimal(),
            median = baseMedian.toBigDecimal(),
            p75 = (baseMedian * 1.25).toBigDecimal(),
            currency = defaultCurrency(country),
            notes = "Eurostat Labour Cost Index 2024"
        )
    }

    override fun defaultCurrency(country: String): String = if (country.uppercase() == "IE") "EUR" else "EUR"

    override val sources: List<SourceAttribution> = listOf(
        SourceAttribution("Eurostat Earnings 2024", "https://ec.europa.eu/eurostat")
    )
}

private fun Int.toBigDecimal(): BigDecimal = BigDecimal(this)
private fun Double.toBigDecimal(): BigDecimal = BigDecimal(this).setScale(2, RoundingMode.HALF_UP)
