package com.mikkaeru.pix.repository

import com.mikkaeru.pix.model.PixKey
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixKeyRepository: JpaRepository<PixKey, String> {
    fun existsByValue(key: String): Boolean
    fun findByIdAndClientId(pixId: String, clientId: String): Optional<PixKey>
    fun findByValue(key: String): Optional<PixKey>
    fun findAllByClientId(clientId: String): List<PixKey>
}