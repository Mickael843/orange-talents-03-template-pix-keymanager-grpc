package com.mikkaeru.pix.shared.handler

import com.mikkaeru.pix.dto.KeyRequest
import io.grpc.Status
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConstraintExceptionHandler: ApiExceptionHandler<RuntimeException> {

    override fun handle(e: RuntimeException): Status {
        return Status.INVALID_ARGUMENT
            .withDescription(getMessage(e as ConstraintViolationException))
            .withCause(e)
    }

    override fun supports(e: RuntimeException): Boolean {
        return e is ConstraintViolationException
    }

    private fun getMessage(e: ConstraintViolationException): String {
        var message = e.message!!.split(":")[1]

        e.constraintViolations.stream().forEach {
            if (it.invalidValue != null && it.invalidValue is KeyRequest) {
                val keyRequest = it.invalidValue as KeyRequest

                if (it.message.equals("Invalid pix key", true)) {
                    message = "Invalid format ${keyRequest.type?.name}"
                }
            }
        }

        return message
    }
}