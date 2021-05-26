package com.mikkaeru.pix

import com.mikkaeru.RemoveKeyPixResponse
import com.mikkaeru.pix.client.BcbClient
import com.mikkaeru.pix.client.BcbDeleteKeyRequest
import com.mikkaeru.pix.dto.RemoveKeyRequest
import com.mikkaeru.pix.repository.PixKeyRepository
import com.mikkaeru.pix.shared.exception.ForbiddenException
import com.mikkaeru.pix.shared.exception.NotFoundException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoveKey(
    @Inject private val bcbClient: BcbClient,
    @Inject private val repository: PixKeyRepository
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun remove(@Valid request: RemoveKeyRequest): RemoveKeyPixResponse {
        val pixKey = repository.findByIdAndClientId(request.pixId, request.clientId)
            .orElseThrow { throw NotFoundException("Chave pix correspondente ao id ${request.pixId} não foi encontrada!") }

        bcbClient.deleteKey(
            key = pixKey.value,
            request = BcbDeleteKeyRequest(
                key = pixKey.value,
                participant = pixKey.account.ispb
            )).let {
            log.info("[${request.clientId}] Deletando a chave pix no banco central (BCB)")

            if (it.status.code == 403) {
                throw ForbiddenException("Proibido a remoção dessa chave pix").also {
                    log.info("[${request.clientId}] Proibido remoção da chave pix no banco central (BCB)")
                }
            } else if (it.status.code == 404) {
                throw NotFoundException("Chave pix correspondente ao id ${request.pixId} não foi encontrada no banco central")
            }
        }

        repository.delete(pixKey).also {
            log.info("[${request.clientId}] Chave pix com o id '${request.pixId}' deletada")
        }

        return RemoveKeyPixResponse
            .newBuilder()
            .setClientId(pixKey.clientId)
            .setPixId(pixKey.id)
            .build()
    }
}
