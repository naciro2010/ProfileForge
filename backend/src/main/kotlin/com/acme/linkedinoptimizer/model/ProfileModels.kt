package com.acme.linkedinoptimizer.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant

data class ProfileSnapshot(
    @field:Size(max = 220)
    val headline: String? = null,
    @field:Size(max = 4000)
    val about: String? = null,
    @field:Size(max = 40)
    val skills: List<@NotBlank @Size(max = 80) String> = emptyList(),
    @field:Valid
    val experiences: List<ExperienceSnapshot> = emptyList(),
    val location: String? = null,
    val hasPhoto: Boolean? = null,
    val seniority: SeniorityLevel? = null,
    val targetRole: String? = null,
    @field:Size(max = 5)
    val languages: List<@Pattern(regexp = "[a-zA-Z-]{2,8}") String> = emptyList()
)

data class ExperienceSnapshot(
    @field:NotBlank
    val role: String,
    @field:NotBlank
    val company: String,
    val timeframe: String? = null,
    @field:Size(min = 10, max = 1200)
    val achievements: String
)

data class ScoreRequest(
    @field:Valid
    val profile: ProfileSnapshot,
    @field:Size(max = 20)
    val keywords: List<@Size(max = 50) String>? = null
)

data class ScoreResponse(
    val total: Int,
    val breakdown: Map<String, Int>,
    val warnings: List<String>
)

data class SuggestRequest(
    @field:Valid
    val profile: ProfileSnapshot,
    val targetRole: String? = null,
    val language: SuggestionLanguage? = SuggestionLanguage.FR
)

data class SuggestionResponse(
    val headline: List<CopyBlock>,
    val about: List<CopyBlock>,
    val skills: SkillSuggestions,
    val experiences: List<ExperienceSuggestion>
)

data class CopyBlock(
    val label: String,
    val content: String
)

data class SkillSuggestions(
    val core: List<String>,
    val trending: List<String>,
    val niceToHave: List<String>
)

data class ExperienceSuggestion(
    val role: String,
    val company: String,
    val bullets: List<String>
)

data class MarketIntelRequest(
    @field:NotBlank
    val role: String,
    @field:NotBlank
    val country: String,
    val seniority: SeniorityLevel? = null,
    val industry: String? = null
)

data class MarketIntelResponse(
    val refreshedAt: Instant,
    val skills: List<SkillInsight>,
    val recruiterSignals: List<RecruiterSignal>,
    val sources: List<SourceAttribution>
)

data class SkillInsight(
    val name: String,
    val category: SkillCategory,
    val source: String
)

data class RecruiterSignal(
    val statement: String,
    val rationale: String
)

data class SourceAttribution(
    val name: String,
    val url: String
)

data class CompensationRequest(
    @field:NotBlank
    val role: String,
    @field:NotBlank
    val country: String,
    val region: String? = null,
    val seniority: SeniorityLevel? = null,
    val companyType: CompanyType? = null,
    val contractType: ContractType? = null,
    val industry: String? = null,
    val currency: String? = null
)

data class CompensationResponse(
    val currency: String,
    val period: CompensationPeriod,
    val low: BigDecimal,
    val median: BigDecimal,
    val high: BigDecimal,
    val justifications: List<String>,
    val sources: List<SourceAttribution>
)

enum class SuggestionLanguage { FR, EN }

enum class SeniorityLevel { JUNIOR, MID, SENIOR, LEAD }

enum class SkillCategory { CORE, TRENDING, SOFT }

enum class CompanyType { STARTUP, SME, SCALEUP, ENTERPRISE }

enum class ContractType { PERMANENT, FREELANCE }

enum class CompensationPeriod { ANNUAL, DAILY }
