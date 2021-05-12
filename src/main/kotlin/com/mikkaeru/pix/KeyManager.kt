package com.mikkaeru.pix

import com.mikkaeru.KeyPixRequest
import com.mikkaeru.KeyPixResponse
import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.pix.extensions.toModel
import com.mikkaeru.pix.shared.ExceptionHandler
import com.mikkaeru.pix.shared.exception.UnknownException
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class KeyManager(@Inject private val service: RegisterKey): KeymanagerServiceGrpc.KeymanagerServiceImplBase() {

    override fun registerPixKey(request: KeyPixRequest?, responseObserver: StreamObserver<KeyPixResponse>?) {

        if (request?.accountType == KeyPixRequest.AccountType.UNKNOWN_ACCOUNT_TYPE) {
            throw UnknownException("Tipo da conta não pode ser desconhecido")
        }

        val pixKey = service.register(request!!.toModel(), responseObserver)

        responseObserver?.onNext(
            KeyPixResponse.newBuilder()
                .setPixId(pixKey.id)
                .setClientId(pixKey.clientId)
                .build()
        )

        responseObserver?.onCompleted()
    }
}
