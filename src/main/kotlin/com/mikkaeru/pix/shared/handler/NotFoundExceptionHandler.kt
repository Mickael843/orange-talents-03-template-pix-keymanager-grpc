package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.NotFoundException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class NotFoundExceptionHandler: ApiExceptionHandler<RuntimeException> {

    override fun handle(e: RuntimeException): Status {
        return Status.NOT_FOUND.withDescription(e.message).withCause(e)
    }

    override fun supports(e: RuntimeException): Boolean {
        return e is NotFoundException
    }
}