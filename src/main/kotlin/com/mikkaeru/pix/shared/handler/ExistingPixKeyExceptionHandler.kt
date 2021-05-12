package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.ExistingPixKeyException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ExistingPixKeyExceptionHandler: ApiExceptionHandler<RuntimeException> {

    override fun handle(e: RuntimeException): Status {
        return Status.ALREADY_EXISTS.withDescription(e.message).withCause(e)
    }

    override fun supports(e: RuntimeException): Boolean {
        return e is ExistingPixKeyException
    }
}