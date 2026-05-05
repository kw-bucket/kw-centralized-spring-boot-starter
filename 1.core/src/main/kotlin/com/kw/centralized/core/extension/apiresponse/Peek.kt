package com.kw.centralized.core.extension.apiresponse

import com.kw.centralized.core.dto.ApiOutput
import com.kw.centralized.core.service.api.ApiResponse

fun <T : Any> ApiResponse<ApiOutput<T>>.peekOutputStatus(): String {
    val code = this.body?.status?.code ?: this.httpStatus.value()
    val description = this.body?.status?.description ?: this.httpStatus.reasonPhrase

    return "$code <$description>"
}

fun ApiResponse.Error.peekError(): String {
    val code = httpStatus.value()
    val description = httpStatus.reasonPhrase + cause?.let { "|${it.message}" }.orEmpty()

    return "$code <$description>"
}

fun <T> ApiResponse<T>.peekHttpStatus(): String = "${this.httpStatus.value()} ${this.httpStatus.reasonPhrase}"
