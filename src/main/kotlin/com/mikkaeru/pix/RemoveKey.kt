package com.mikkaeru.pix

import com.mikkaeru.RemoveKeyPixResponse
import com.mikkaeru.pix.client.BcbClient
import com.mikkaeru.pix.client.BcbDeleteKeyRequest
import com.mikkaeru.pix.dto.RemoveKeyRequest
import com.mikkaeru.pix.repository.PixKeyRepository
import com.mikkaeru.pix.shared.exception.ForbiddenException
import com.mikkaeru.pix.shared.exception.NotFoundException
import io.micronaut.validation.Validated
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
                if (it.status.code == 403) {
                    throw ForbiddenException("Proibido a remoção dessa chave pix")
                } else if (it.status.code == 404) {
                    throw NotFoundException("Chave pix correspondente ao id ${request.pixId} não foi encontrada no banco central")
                }
        }

        repository.delete(pixKey)

        return RemoveKeyPixResponse
            .newBuilder()
            .setClientId(pixKey.clientId)
            .setPixId(pixKey.id)
            .build()
    }
}
