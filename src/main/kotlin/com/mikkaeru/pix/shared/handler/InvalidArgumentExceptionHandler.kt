package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.InvalidArgumentException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class InvalidArgumentExceptionHandler: ApiExceptionHandler<RuntimeException> {

    override fun handle(e: RuntimeException): Status {
        return Status.INVALID_ARGUMENT.withDescription(e.message).withCause(e)
    }

    override fun supports(e: RuntimeException): Boolean {
        return e is InvalidArgumentException
    }
}