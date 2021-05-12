package com.mikkaeru.pix.model

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class AssociatedAccount(

    @field:NotBlank
    @Column(nullable = false)
    val agency: String,

    @field:NotBlank
    @Column(nullable = false)
    val number: String,

    @field:NotBlank
    @Column(nullable = false)
    val cpfOwner: String,

    @field:NotBlank
    @Column(nullable = false)
    val nameOwner: String,

    @field:NotBlank
    @Column(nullable = false)
    val institution: String
) {

}
