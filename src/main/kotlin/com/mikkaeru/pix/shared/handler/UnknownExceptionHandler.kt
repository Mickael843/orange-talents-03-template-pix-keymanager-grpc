package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.UnknownException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class UnknownExceptionHandler: ApiExceptionHandler<RuntimeException> {

    override fun handle(e: RuntimeException): Status {
        return Status.UNKNOWN.withDescription(e.message).withCause(e)
    }

    override fun supports(e: RuntimeException): Boolean {
        return e is UnknownException
    }
}