package com.mikkaeru

import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.time.OffsetDateTime.now
import java.util.UUID.randomUUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id

@Entity
class PixKey(
    @Column(nullable = false) val clientId: String,
    @Column(nullable = false) val keyType: KeyType,
    @Column(nullable = false) val accountType: AccountType,
    @Column(nullable = false, unique = true) val value: String,
    @Column(nullable = false) val pixCode: String = randomUUID().toString()
) {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null

    @CreationTimestamp
    val createdAt: OffsetDateTime = now()
}