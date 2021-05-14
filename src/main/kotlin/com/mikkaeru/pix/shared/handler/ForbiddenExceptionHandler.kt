package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.ForbiddenException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ForbiddenExceptionHandler: ApiExceptionHandler<RuntimeException> {

    override fun handle(e: RuntimeException): Status {
        return Status.PERMISSION_DENIED.withDescription(e.message).withCause(e)
    }

    override fun supports(e: RuntimeException): Boolean {
        return e is ForbiddenException
    }
}