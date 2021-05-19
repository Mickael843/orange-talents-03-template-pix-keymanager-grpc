package com.mikkaeru.pix.dto

import com.mikkaeru.pix.client.BcbClient
import com.mikkaeru.pix.repository.PixKeyRepository
import com.mikkaeru.pix.shared.exception.NotFoundException
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filter {

    abstract fun filter(repository: PixKeyRepository, bcbClient: BcbClient): PixKeyInfo

    @Introspected
    data class ByPixId(
        @field:NotBlank val clientId: String,
        @field:NotBlank val pixId: String,
    ): Filter() {

        override fun filter(repository: PixKeyRepository, bcbClient: BcbClient): PixKeyInfo {
            return repository.findByIdAndClientId(clientId = clientId, pixId = pixId)
                .map(PixKeyInfo::of)
                .orElseThrow { throw NotFoundException("Chave pix não encontrada") }
        }
    }

    @Introspected
    data class ByKey(@field:NotBlank @Size(max = 77) val key: String): Filter() {

        private val log = LoggerFactory.getLogger(this::class.java)

        override fun filter(repository: PixKeyRepository, bcbClient: BcbClient): PixKeyInfo {
            return repository.findByValue(key)
                .map(PixKeyInfo::of)
                .orElseGet {
                    log.info("Consultando a chave Pix '$key' no Banco Central do Brasil (BCB)")

                    val response = bcbClient.searchPixKey(key)

                    when(response.status) {
                        HttpStatus.OK -> response.body()?.toPixKeyInfo()
                        else -> throw NotFoundException("Chave pix não encontrada")
                    }
                }
        }
    }

    @Introspected
    class Invalid: Filter() {

        override fun filter(repository: PixKeyRepository, bcbClient: BcbClient): PixKeyInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }
}
