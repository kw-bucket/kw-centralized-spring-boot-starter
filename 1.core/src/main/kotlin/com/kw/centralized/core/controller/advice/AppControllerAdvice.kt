package com.kw.centralized.core.controller.advice

import com.kw.centralized.core.constant.ApiOutputStatus
import com.kw.centralized.core.dto.ApiOutput
import com.kw.centralized.core.exception.AppException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import tools.jackson.databind.exc.InvalidFormatException

@ControllerAdvice
class AppControllerAdvice {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${centralized-core.application-code:APP}")
    private val applicationCode: String = "APP"

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiOutput<Nothing>> =
        buildResponse(
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            status = ExceptionStatus.E_500_0,
            description = ex.message,
        ).also {
            logger.error("Exception: {}", ex.stackTraceToString())
        }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException) =
        buildResponse(
            httpStatus = HttpStatus.BAD_REQUEST,
            status = ExceptionStatus.E_400_0,
            description = ex.message,
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException) =
        buildResponse(
            httpStatus = HttpStatus.CONFLICT,
            status = ExceptionStatus.E_409_0,
            description = ex.message,
        )

    // When @RequestBody (JSON) validation fails.
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleConstraintsFailure(ex: MethodArgumentNotValidException) =
        buildResponse(
            httpStatus = HttpStatus.BAD_REQUEST,
            status = ExceptionStatus.E_400_0,
            description =
                ex.bindingResult.fieldErrors.takeIf { it.isNotEmpty() }?.joinToString { error ->
                    val reason =
                        when (error.code) {
                            "typeMismatch" -> "wrong data type provided!"
                            "NotNull", "NotBlank" -> "field is required and cannot be null or empty!"
                            "Min", "Max" -> "value is out of range!"
                            else -> "validation failed"
                        }
                    "'${error.field}' $reason (for value '${error.rejectedValue}')"
                } ?: ex.message,
        ).also {
            logger.error("Handler MethodArgumentNotValidException: {}", ex.message)
        }

    // When a @PathVariable or @RequestParam fails to convert.
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMismatchedMethodArgument(ex: MethodArgumentTypeMismatchException) =
        buildResponse(
            httpStatus = HttpStatus.BAD_REQUEST,
            status = ExceptionStatus.E_400_0,
            description = "'${ex.name}' should be a valid '${ex.requiredType?.simpleName}' but '${ex.value}' is not!",
        ).also {
            logger.error("Handle MethodArgumentTypeMismatchException: {}", ex.message)
        }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingRequestParam(ex: MissingServletRequestParameterException) =
        buildResponse(
            httpStatus = HttpStatus.BAD_REQUEST,
            status = ExceptionStatus.E_400_0,
            description =
                "Required request parameter '${ex.parameterName}' for type ${ex.parameterType} is not present!",
        ).also {
            logger.error("Handle MissingServletRequestParameterException: {}", ex.message)
        }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMalformedJson(ex: HttpMessageNotReadableException): ResponseEntity<ApiOutput<Nothing>> {
        val description =
            when (val cause = ex.cause) {
                is InvalidFormatException -> "Invalid value '${cause.value}' for field '${cause.path[0].propertyName}'"
                else -> "Malformed JSON request"
            }

        return buildResponse(
            httpStatus = HttpStatus.BAD_REQUEST,
            status = ExceptionStatus.E_400_0,
            description = description,
        ).also {
            logger.error("Handler HttpMessageNotReadableException: {}", ex.message)
        }
    }

    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException): ResponseEntity<ApiOutput<Nothing>> =
        buildResponse(ex.httpStatus, ex.apiOutputStatus, ex.description)

    private fun buildResponse(
        httpStatus: HttpStatus,
        status: ApiOutputStatus,
        description: String? = null,
    ): ResponseEntity<ApiOutput<Nothing>> =
        ApiOutput.fromStatus<Nothing>(status, description).let { ResponseEntity.status(httpStatus).body(it) }

    private fun buildResponse(
        httpStatus: HttpStatus,
        status: ExceptionStatus,
        description: String? = null,
    ): ResponseEntity<ApiOutput<Nothing>> {
        val code = "$applicationCode${status.code}"
        val apiOutput: ApiOutput<Nothing> =
            ApiOutput.fromCustomStatus(
                code = code,
                message = status.message,
                description = description ?: status.description,
            )

        return ResponseEntity.status(httpStatus).body(apiOutput)
    }
}

private enum class ExceptionStatus(
    override val code: String,
    override val message: String,
    override val description: String,
) : ApiOutputStatus {
    E_400_0("_400_0", "Bad Request", "Bad Request"),
    E_409_0("_409_0", "Conflict", "Conflict"),
    E_500_0("_500_0", "Internal Server Error", "Internal Server Error"),
}
