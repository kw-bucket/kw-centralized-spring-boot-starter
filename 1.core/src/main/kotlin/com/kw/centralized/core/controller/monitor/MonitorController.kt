package com.kw.centralized.core.controller.monitor

import org.springframework.boot.actuate.info.InfoEndpoint
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint
import org.springframework.boot.health.actuate.endpoint.HttpCodeStatusMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MonitorController(
    private val healthEndpoint: HealthEndpoint,
    private val infoEndpoint: InfoEndpoint,
    private val httpCodeStatusMapper: HttpCodeStatusMapper,
) {
    @GetMapping("/health")
    fun getHealth(): ResponseEntity<HealthDescriptor> =
        healthEndpoint.health().let { h ->
            ResponseEntity.status(httpCodeStatusMapper.getStatusCode(h.status)).body(h)
        }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(infoEndpoint.info())
}
