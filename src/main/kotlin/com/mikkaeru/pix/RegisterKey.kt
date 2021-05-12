package com.mikkaeru.pix

import com.mikkaeru.KeyPixResponse
import com.mikkaeru.pix.client.ItauClient
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import com.mikkaeru.pix.shared.exception.ClientNotFoundException
import com.mikkaeru.pix.shared.exception.ExistingPixKeyException
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class RegisterKey(
    @Inject private val itauClient: ItauClient,
    @Inject private val repository: PixKeyRepository
) {

    @Transactional
    fun register(@Valid request: KeyRequest, responseObserver: StreamObserver<KeyPixResponse>?): PixKey {
        request.run {
            type!!.validate(key)
        }

        if (repository.existsByValue(request.key)) {
            throw ExistingPixKeyException("Chave pix ${request.key} existente")
        }

        val response = itauClient.findAccountById(request.clientId, request.accountType!!.equivalentName)

        val account = response.body()?.toModel() ?: throw ClientNotFoundException("Cliente não encontrado")

        val pixKey = request.toModel(account)

        return repository.save(pixKey)
    }
}