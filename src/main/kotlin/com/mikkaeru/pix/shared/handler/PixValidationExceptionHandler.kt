package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.ApiException
import com.mikkaeru.pix.shared.exception.PixValidationException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixValidationExceptionHandler: ApiExceptionHandler<PixValidationException> {
    override fun supports(e: ApiException): Boolean {
        return e is PixValidationException
    }

    override fun handle(e: PixValidationException): Status {
        return Status.INVALID_ARGUMENT.withDescription(e.localizedMessage)
    }
}