package com.acme.linkedinoptimizer.config

import com.bucket4j.Bandwidth
import com.bucket4j.Bucket
import com.bucket4j.Refill
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RateLimitingFilter(
    @Value("\${api.rate-limit.capacity:30}") private val capacity: Long,
    @Value("\${api.rate-limit.refill-period:PT1M}") private val refillPeriod: Duration
) : OncePerRequestFilter() {
    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        return path.startsWith("/actuator") || path.startsWith("/v3/api-docs")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader("X-API-Key") ?: "anonymous"
        val bucket = buckets.computeIfAbsent(apiKey) {
            Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, refillPeriod)))
                .build()
        }
        if (!bucket.tryConsume(1)) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("Rate limit exceeded")
            return
        }
        filterChain.doFilter(request, response)
    }
}

@Configuration
class RateLimitConfig {
    @Bean
    fun rateLimiterRegistration(filter: RateLimitingFilter): FilterRegistrationBean<RateLimitingFilter> =
        FilterRegistrationBean<RateLimitingFilter>(filter).apply { order = Ordered.HIGHEST_PRECEDENCE + 1 }
}
