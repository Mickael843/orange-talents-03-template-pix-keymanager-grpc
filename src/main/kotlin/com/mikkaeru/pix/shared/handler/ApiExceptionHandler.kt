package com.mikkaeru.pix.shared.handler

import io.grpc.Status

interface ApiExceptionHandler<E: RuntimeException> {

    fun handle(e: RuntimeException): Status

    fun supports(e: RuntimeException): Boolean
}