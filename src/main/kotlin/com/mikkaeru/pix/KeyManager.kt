package com.mikkaeru.pix

import com.mikkaeru.KeyPixRequest
import com.mikkaeru.KeyPixResponse
import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.pix.client.ItauClient
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.model.KeyType.valueOf
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
open class KeyManager(
    private val repository: PixKeyRepository,
    private val itauClient: ItauClient
): KeymanagerServiceGrpc.KeymanagerServiceImplBase() {

    override fun registerPixKey(request: KeyPixRequest?, responseObserver: StreamObserver<KeyPixResponse>?) {

        valueOf(request!!.keyType.name).validate(request.value).ifPresent {
            responseObserver?.onError(it)
        }

        val pixKey = register(request.toModel(), responseObserver)

        responseObserver?.onNext(
            KeyPixResponse.newBuilder()
                .setPixId(pixKey.id)
                .setClientId(pixKey.clientId)
                .build()
        )

        responseObserver?.onCompleted()
    }

    private fun register(keyRequest: KeyRequest, responseObserver: StreamObserver<KeyPixResponse>?): PixKey {
        if (repository.existsByValue(keyRequest.key)) {
            responseObserver?.onError(Status.ALREADY_EXISTS
                .withDescription("Chave já cadastrada")
                .asRuntimeException())
        }

        val response = itauClient.findAccountById(keyRequest.clientId, keyRequest.accountType!!.equivalentName)

        val account = response.body()?.toModel()

        if (account == null) {
            responseObserver?.onError(Status.NOT_FOUND
                .withDescription("Conta não encontrada!")
                .asRuntimeException())
        }

        val pixKey = keyRequest.toModel(account!!)
        return repository.save(pixKey)
    }
}
