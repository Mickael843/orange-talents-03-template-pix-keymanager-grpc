package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.ApiException
import com.mikkaeru.pix.shared.exception.ClientNotFoundException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ClientNotFoundExceptionHandler: ApiExceptionHandler<ApiException> {

    override fun handle(e: ApiException): Status {
        return Status.NOT_FOUND.withDescription(e.message).withCause(e)
    }

    override fun supports(e: ApiException): Boolean {
        return e is ClientNotFoundException
    }
}