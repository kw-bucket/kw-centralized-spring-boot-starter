package com.kw.centralized.core.service.api

import com.kw.centralized.core.extension.string.collapse
import org.apache.hc.client5.http.ConnectTimeoutException
import org.apache.hc.core5.http.ConnectionRequestTimeoutException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponents
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import java.lang.reflect.Type
import java.net.SocketTimeoutException

open class ApiService(
    private val restClient: RestClient,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val mapper =
        JsonMapper
            .builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

    @Value("\${centralized-core.api.logging.enabled:true}")
    private val isLogEnabled: Boolean = true

    @Value("\${centralized-core.api.logging.body-length:100}")
    private val strLogLength: Int = 100

    fun <T : Any> execute(
        httpMethod: HttpMethod,
        uriComponents: UriComponents,
        uriVariables: Map<String, Any>?,
        httpEntity: HttpEntity<Any>?,
        responseBodyType: ParameterizedTypeReference<T>,
    ): ApiResponse<T> {
        if (isLogEnabled) {
            logger.info(
                """
                    |Api Request >> {} {}
                    |:. query= {}
                    |:. uri.variables= {}
                    |:. headers= {}
                    |:. body= [{}]
                """.trimMargin(),
                httpMethod.name(),
                uriComponents.path,
                uriComponents.query ?: "None",
                uriVariables?.entries?.joinToString(prefix = "[", postfix = "]") { "${it.key}: \"${it.value}\"" },
                httpEntity?.headers?.toString() ?: "None",
                httpEntity?.body?.let {
                    val body =
                        when (httpEntity.headers.contentType) {
                            MediaType.APPLICATION_JSON -> mapper.writeValueAsString(it)
                            else -> it.toString()
                        }

                    body.collapse(length = strLogLength)
                } ?: "None",
            )
        }

        val apiResponse =
            try {
                val response =
                    call(
                        httpMethod = httpMethod,
                        httpHeaders = httpEntity?.headers ?: HttpHeaders.EMPTY,
                        uriString = uriComponents.toUriString(),
                        uriVariables = uriVariables ?: emptyMap(),
                        requestBody = httpEntity?.body,
                        responseBodyType = responseBodyType,
                    )
                val httpStatus = HttpStatus.valueOf(response.statusCode.value())

                if (response.statusCode.is2xxSuccessful) {
                    ApiResponse.Success(
                        httpStatus = httpStatus,
                        httpHeaders = response.headers,
                        body = response.body,
                    )
                } else {
                    ApiResponse.Failure(
                        httpStatus = httpStatus,
                        httpHeaders = response.headers,
                        body = response.body,
                    )
                }
            } catch (ex: HttpStatusCodeException) {
                ApiResponse
                    .Error(
                        httpStatus = HttpStatus.valueOf(ex.statusCode.value()),
                        httpHeaders = ex.responseHeaders ?: HttpHeaders.EMPTY,
                        bodyAsString = ex.responseBodyAsString,
                        cause = ex,
                    ).also {
                        logger.error(
                            """
                                |Api Request ** {} {}
                                |:. Http Specific Exception! <{}>
                                |:. Stack Trace: {}
                            """.trimMargin(),
                            httpMethod.name(),
                            uriComponents.path,
                            ex.javaClass.canonicalName,
                            ex.stackTrace?.joinToString(separator = "\n\t") { s -> s.toString() },
                        )
                    }
            } catch (ex: Exception) {
                val (httpStatus, cause) =
                    when (ex) {
                        is ResourceAccessException ->
                            when (val cause: Throwable? = ex.cause) {
                                is ConnectionRequestTimeoutException,
                                is ConnectTimeoutException,
                                -> Pair(HttpStatus.INTERNAL_SERVER_ERROR, cause)
                                is SocketTimeoutException -> Pair(HttpStatus.GATEWAY_TIMEOUT, cause)
                                else -> Pair(HttpStatus.INTERNAL_SERVER_ERROR, ex)
                            }

                        else ->
                            Pair(HttpStatus.INTERNAL_SERVER_ERROR, ex)
                    }

                ApiResponse
                    .Error(
                        httpStatus = httpStatus,
                        cause = cause,
                    ).also {
                        logger.error(
                            """
                                |Api Request !! {} {}
                                |:. Unexpected Exception! <{}>
                                |:. Stack Trace: {}
                            """.trimMargin(),
                            httpMethod.name(),
                            uriComponents.path,
                            (it.cause ?: ex).javaClass.canonicalName,
                            (it.cause ?: ex).stackTrace.joinToString(separator = "\n\t") { s -> s.toString() },
                        )
                    }
            }

        return apiResponse.also {
            if (isLogEnabled) {
                logger.info(
                    """
                        |Api Response << {} {} [{}]
                        |:. headers= {}
                        |:. body= [{}]
                    """.trimMargin(),
                    httpMethod.name(),
                    uriComponents.path,
                    it.httpStatus,
                    it.httpHeaders.toString(),
                    it.body?.let { b ->
                        mapper.writeValueAsString(b).collapse(length = strLogLength)
                    } ?: "None",
                )
            }
        }
    }

    fun <T : Any> call(
        httpMethod: HttpMethod,
        httpHeaders: HttpHeaders,
        uriString: String,
        uriVariables: Map<String, Any>,
        requestBody: Any?,
        responseBodyType: ParameterizedTypeReference<T>,
    ): ResponseEntity<T> =
        try {
            val reqSpec: RestClient.RequestBodySpec =
                restClient
                    .method(httpMethod)
                    .uri(uriString, uriVariables)

            reqSpec.apply {
                headers { headers -> headers.addAll(httpHeaders) }

                if (requestBody != null) {
                    body(requestBody)
                }
            }

            reqSpec
                .retrieve()
                .toEntity(responseBodyType)
        } catch (ex: HttpStatusCodeException) {
            handleHttpException(ex, responseBodyType.type)
        } catch (ex: Exception) {
            throw ex
        }

    private fun <T : Any> handleHttpException(
        httpException: HttpStatusCodeException,
        responseType: Type,
    ): ResponseEntity<T> =
        try {
            val parsedBody: T =
                mapper.readValue(
                    httpException.responseBodyAsString,
                    object : TypeReference<T>() {
                        override fun getType(): Type = responseType
                    },
                )

            ResponseEntity
                .status(httpException.statusCode)
                .headers(httpException.responseHeaders)
                .body(parsedBody)
        } catch (_: Exception) {
            throw httpException
        }
}
