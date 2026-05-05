package com.kw.centralized.core.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.kw.centralized.core.constant.ApiOutputStatus
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiOutput<T : Any>(
    val status: Status,
    val data: T?,
) {
    companion object {
        fun <T : Any> fromStatus(
            apiOutputStatus: ApiOutputStatus,
            description: String? = null,
            data: T? = null,
        ): ApiOutput<T> =
            ApiOutput(
                status =
                    Status(
                        code = apiOutputStatus.code,
                        message = apiOutputStatus.message,
                        description = description ?: apiOutputStatus.description,
                    ),
                data = data,
            )

        fun <T : Any> fromCustomStatus(
            code: String,
            message: String,
            description: String? = null,
            data: T? = null,
        ): ApiOutput<T> =
            ApiOutput(
                status = Status(code = code, message = message, description = description),
                data = data,
            )
    }

    data class Status(
        val code: String? = null,
        val message: String? = null,
        val description: String? = null,
    )
}
