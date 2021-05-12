package com.mikkaeru.pix.model

import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.time.OffsetDateTime.now
import java.util.UUID.randomUUID
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class PixKey(

    @field:NotBlank
    @Column(nullable = false)
    val clientId: String,

    @field:NotNull
    @Column(nullable = false)
    val keyType: KeyType,

    @field:NotBlank
    @Column(nullable = false)
    val accountType: AccountType,

    @field:NotBlank
    @Column(nullable = false, unique = true)
    val value: String,

    @Embedded
    @field:NotNull
    val account: AssociatedAccount
) {
    @Id
    val id: String = randomUUID().toString()

    @CreationTimestamp
    val createdAt: OffsetDateTime = now()
}