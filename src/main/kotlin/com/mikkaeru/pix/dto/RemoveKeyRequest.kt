package com.mikkaeru.pix.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class RemoveKeyRequest(
    @field:NotBlank(message = "O clientId não pode estar vazio")
    val clientId: String,
    @field:NotBlank(message = "O pixId não pode estar vazio")
    val pixId: String
)
