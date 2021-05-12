package com.mikkaeru.pix.endpoint

import com.mikkaeru.KeyPixRequest
import com.mikkaeru.KeyPixResponse
import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.pix.client.ItauClient
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.extensions.toModel
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import com.mikkaeru.pix.shared.ExceptionHandler
import com.mikkaeru.pix.shared.exception.ClientNotFoundException
import com.mikkaeru.pix.shared.exception.ExistingPixKeyException
import com.mikkaeru.pix.shared.exception.UnknownException
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
@ExceptionHandler
class KeyManager(
    private val repository: PixKeyRepository,
    private val itauClient: ItauClient
): KeymanagerServiceGrpc.KeymanagerServiceImplBase() {

    override fun registerPixKey(request: KeyPixRequest?, responseObserver: StreamObserver<KeyPixResponse>?) {

        if (request?.accountType == KeyPixRequest.AccountType.UNKNOWN_ACCOUNT_TYPE) {
            throw UnknownException("Tipo da conta não pode ser desconhecido")
        }

        val pixKey = register(request!!.toModel(), responseObserver)

        responseObserver?.onNext(
            KeyPixResponse.newBuilder()
                .setPixId(pixKey.id)
                .setClientId(pixKey.clientId)
                .build()
        )

        responseObserver?.onCompleted()
    }

    private fun register(@Valid keyRequest: KeyRequest, responseObserver: StreamObserver<KeyPixResponse>?): PixKey {
        keyRequest.run {
            type!!.validate(key)
        }

        if (repository.existsByValue(keyRequest.key)) {
            throw ExistingPixKeyException("Chave pix ${keyRequest.key} existente")
        }

        val response = itauClient.findAccountById(keyRequest.clientId, keyRequest.accountType!!.equivalentName)

        val account = response.body()?.toModel() ?: throw ClientNotFoundException("Cliente não encontrado")

        val pixKey = keyRequest.toModel(account)

        return repository.save(pixKey)
    }
}
