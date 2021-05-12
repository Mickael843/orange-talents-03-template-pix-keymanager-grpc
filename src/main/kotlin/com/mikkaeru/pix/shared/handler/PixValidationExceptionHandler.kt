package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.PixValidationException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixValidationExceptionHandler: ApiExceptionHandler<RuntimeException> {

    override fun supports(e: RuntimeException): Boolean {
        return e is PixValidationException
    }

    override fun handle(e: RuntimeException): Status {
        return Status.INVALID_ARGUMENT.withDescription(e.localizedMessage)
    }
}