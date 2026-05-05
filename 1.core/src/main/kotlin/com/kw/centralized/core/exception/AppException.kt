package com.kw.centralized.core.exception

import com.kw.centralized.core.constant.ApiOutputStatus
import org.springframework.http.HttpStatus

class AppException(
    val httpStatus: HttpStatus,
    val apiOutputStatus: ApiOutputStatus,
    val description: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException(cause)
