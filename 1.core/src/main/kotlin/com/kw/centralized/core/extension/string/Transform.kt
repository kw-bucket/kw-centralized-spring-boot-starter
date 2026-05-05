package com.kw.centralized.core.extension.string

import com.kw.centralized.core.service.api.ApiResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

/**
 * Transform string from camel case to snake case
 */
fun String.toSnakeCase(): String = camelRegex.replace(this) { "_${it.value}" }.lowercase()

/**
 * Parse JSON string into an object. otherwise, null
 */
inline fun <reified T : Any> String.parseOrNull(): T? {
    val logger: Logger = LoggerFactory.getLogger("StringExt")

    return try {
        JsonMapper
            .builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()
            .readValue(this, object : TypeReference<T>() {})
    } catch (e: Exception) {
        logger.error("{} Parse Error!!!", T::class.java.simpleName)
        e.printStackTrace()

        null
    }
}

/**
 * Find a resource with a string that provide the location of file
 */
fun String.asResource() = {}::class.java.getResource(this)?.readText() ?: "read resource fail: $this"

/**
 * Parse json string into a nullable object as body of ApiResponse.Success
 */
inline fun <reified T : Any> String.asApiResponseSuccess(): ApiResponse.Success<T> =
    this.parseOrNull<T>().let {
        ApiResponse.Success(httpStatus = HttpStatus.OK, httpHeaders = HttpHeaders.EMPTY, body = it)
    }

/**
 * Parse JSON string into a nullable object as body of ApiResponse.Failure
 */
inline fun <reified T : Any> String.asApiResponseFailure(
    httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
): ApiResponse.Failure<T> =
    this.parseOrNull<T>().let {
        ApiResponse.Failure(
            httpStatus = httpStatus,
            httpHeaders = HttpHeaders.EMPTY,
            body = it,
        )
    }

/**
 * Parse JSON string into a nullable object as bodyAsString of ApiResponse.Error
 */
fun String.asApiResponseError(httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR): ApiResponse.Error =
    ApiResponse.Error(httpStatus = httpStatus, httpHeaders = HttpHeaders.EMPTY, bodyAsString = this)

fun String.collapse(length: Int = 100): String =
    if (this.length > length) {
        val center = " ######## "
        val n = (length - center.length) / 2

        "${this.take(n)}$center${this.takeLast(n)}"
    } else {
        this
    }
