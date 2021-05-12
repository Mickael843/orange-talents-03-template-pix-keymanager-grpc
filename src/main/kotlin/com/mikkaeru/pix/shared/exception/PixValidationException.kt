package com.mikkaeru.pix.shared.exception

import javax.validation.ConstraintViolationException

open class PixValidationException(message: String?): ApiException(message) {
    constructor(e: ConstraintViolationException) : this(e.localizedMessage)
}