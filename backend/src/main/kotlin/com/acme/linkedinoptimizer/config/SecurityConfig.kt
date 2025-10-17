package com.acme.linkedinoptimizer.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyFilter(@Value("\${api.key:changeme}") private val apiKey: String) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        return path.startsWith("/actuator") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestKey = request.getHeader("X-API-Key")
        if (requestKey.isNullOrBlank() || requestKey != apiKey) {
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.writer.write("Missing or invalid API key")
            return
        }
        filterChain.doFilter(request, response)
    }
}

@Configuration
class SecurityConfig(@Value("\${cors.allowed-origins:chrome-extension://*}") private val allowedOrigins: String) {
    @Bean
    fun apiKeyFilterRegistration(filter: ApiKeyFilter): FilterRegistrationBean<ApiKeyFilter> =
        FilterRegistrationBean<ApiKeyFilter>(filter).apply { order = Ordered.HIGHEST_PRECEDENCE }

    @Bean
    fun corsFilter(): CorsFilter {
        val configuration = CorsConfiguration()
        val origins = allowedOrigins
            .replace("[", "")
            .replace("]", "")
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        configuration.allowedOrigins = origins
        configuration.allowedMethods = listOf("GET", "POST", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = false

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return CorsFilter(source)
    }
}
