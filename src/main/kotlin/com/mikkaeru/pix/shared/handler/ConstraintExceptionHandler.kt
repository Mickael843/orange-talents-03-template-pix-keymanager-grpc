package com.mikkaeru.pix.shared.handler

import io.grpc.Status
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConstraintExceptionHandler: ApiExceptionHandler<RuntimeException> {

    override fun handle(e: RuntimeException): Status {
        val message = e.message!!.split(":")[1]

        return Status.INVALID_ARGUMENT
            .withDescription(message)
            .withCause(e)
    }

    override fun supports(e: RuntimeException): Boolean {
        return e is ConstraintViolationException
    }
}