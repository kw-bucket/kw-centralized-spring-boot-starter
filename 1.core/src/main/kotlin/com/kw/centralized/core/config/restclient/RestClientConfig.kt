package com.kw.centralized.core.config.restclient

import com.kw.centralized.core.config.metric.CustomizedClientRequestObservationConvention
import io.micrometer.observation.ObservationRegistry
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.util.concurrent.TimeUnit

@Configuration
class RestClientConfig {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${centralized-core.http-client.connection.connect-timeout-seconds:30}")
    private val connectTimeoutSeconds: Long = 30

    @Value("\${centralized-core.http-client.request.connection-request-timeout-seconds:30}")
    private val connectionRequestTimeoutSeconds: Long = 30

    @Value("\${centralized-core.http-client.request.response-timeout-seconds:30}")
    private val responseTimeoutSeconds: Long = 60

    @Value("\${centralized-core.http-client.pooling.connection-limit:3000}")
    private val connectionLimit: Int = 3000

    @Value("\${centralized-core.http-client.pooling.max-connection-per-route:3000}")
    private val maxConnectionPerRoute: Int = 3000

    @Bean
    fun restClient(
        restClientBuilder: RestClient.Builder,
        observationRegistry: ObservationRegistry,
    ): RestClient =
        restClientBuilder
            .observationRegistry(observationRegistry)
            .observationConvention(CustomizedClientRequestObservationConvention())
            .requestFactory(
                HttpComponentsClientHttpRequestFactory(closeableHttpClient()),
            ).build()

    private fun closeableHttpClient(): CloseableHttpClient {
        val requestConfig: RequestConfig =
            RequestConfig
                .custom()
                .apply {
                    setConnectionRequestTimeout(connectionRequestTimeoutSeconds, TimeUnit.SECONDS)
                    setResponseTimeout(responseTimeoutSeconds, TimeUnit.SECONDS)
                }.build()
                .also {
                    logger.info(
                        """
                            |Http Request Config
                            |:. Connection-Request-Timeout=[{}]
                            |:. Response-Timeout=[{}]
                        """.trimMargin(),
                        it.connectionRequestTimeout,
                        it.responseTimeout,
                    )
                }
        val connectionConfig: ConnectionConfig =
            ConnectionConfig
                .custom()
                .apply {
                    setConnectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                }.build()
                .also {
                    logger.info(
                        """
                            |Http Connection Config
                            |:. Connect-Timeout=[{}]
                        """.trimMargin(),
                        it.connectTimeout,
                    )
                }
        val connectionManager: PoolingHttpClientConnectionManager =
            PoolingHttpClientConnectionManager()
                .apply {
                    maxTotal = connectionLimit
                    defaultMaxPerRoute = maxConnectionPerRoute

                    setDefaultConnectionConfig(connectionConfig)
                }.also {
                    logger.info(
                        """
                            |Http Pooling Connection Config
                            |:. Max-Total=[{}]
                            |:. Max-Connections-Per-Route=[{}]
                        """.trimMargin(),
                        it.maxTotal,
                        it.defaultMaxPerRoute,
                    )
                }
        return HttpClientBuilder
            .create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build()
    }
}
