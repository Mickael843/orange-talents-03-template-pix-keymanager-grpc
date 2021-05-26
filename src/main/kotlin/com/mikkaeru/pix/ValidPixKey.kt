package com.mikkaeru.pix

import com.mikkaeru.pix.dto.KeyRequest
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE

@MustBeDocumented
@Retention(RUNTIME)
@Target(CLASS, TYPE)
@Constraint(validatedBy = [PixValidator::class])
annotation class ValidPixKey(val message: String = "Invalid pix key")

@Singleton
class PixValidator: ConstraintValidator<ValidPixKey, KeyRequest> {

    override fun isValid(
        value: KeyRequest?,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value?.type == null)
            return true

        return value.type.validate(value.key)
    }
}
