package com.mikkaeru.pix.repository

import com.mikkaeru.pix.model.PixKey
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixKeyRepository: JpaRepository<PixKey, String> {
    fun existsByValue(value: String): Boolean
}