package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.ApiException
import io.grpc.Status

interface ApiExceptionHandler<E: ApiException> {

    fun handle(e: E): Status

    fun supports(e: ApiException): Boolean
}