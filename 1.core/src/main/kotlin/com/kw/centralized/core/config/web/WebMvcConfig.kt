package com.kw.centralized.core.config.web

import com.kw.centralized.core.http.constant.Paths
import com.kw.centralized.core.http.intercept.handler.RequestHandlerInterceptor
import com.kw.centralized.core.http.intercept.log.LogInterceptor
import com.kw.centralized.core.http.intercept.secure.SecureEndpointInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class WebMvcConfig(
    private val requestHandlerInterceptor: RequestHandlerInterceptor,
    private val logInterceptor: LogInterceptor,
    private val secureEndpointInterceptor: SecureEndpointInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(requestHandlerInterceptor).excludePathPatterns(Paths.exclusion)
        registry.addInterceptor(logInterceptor).excludePathPatterns(Paths.exclusion)
        registry.addInterceptor(secureEndpointInterceptor).excludePathPatterns(Paths.exclusion)
    }
}
