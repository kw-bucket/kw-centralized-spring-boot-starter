package com.kw.centralized.core.http.constant

object Paths {
    val actuator =
        listOf(
            "/health",
            "/info",
            "/metrics",
        )

    val exclusion =
        actuator +
            listOf(
                "/prometheus",
                "/actuator**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**",
                "/v2/api-docs",
                "/configuration/**",
            )
}
