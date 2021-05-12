package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.shared.exception.ApiException
import com.mikkaeru.pix.shared.exception.ExistingPixKeyException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ExistingPixKeyExceptionHandler: ApiExceptionHandler<ApiException> {

    override fun handle(e: ApiException): Status {
        return Status.ALREADY_EXISTS.withDescription(e.message).withCause(e)
    }

    override fun supports(e: ApiException): Boolean {
        return e is ExistingPixKeyException
    }
}