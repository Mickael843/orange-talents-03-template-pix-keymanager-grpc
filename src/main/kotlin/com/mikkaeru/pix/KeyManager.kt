package com.mikkaeru.pix

import com.mikkaeru.*
import com.mikkaeru.pix.extensions.toKeyRequest
import com.mikkaeru.pix.extensions.toRemoveKeyRequest
import com.mikkaeru.pix.shared.ExceptionHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class KeyManager(
    @Inject private val removeKey: RemoveKey,
    @Inject private val registerKey: RegisterKey
): KeymanagerServiceGrpc.KeymanagerServiceImplBase() {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun registerPixKey(request: KeyPixRequest?, responseObserver: StreamObserver<KeyPixResponse>?) {
        val pixKey = registerKey.register(request!!.toKeyRequest()).also {
            log.info("[${request.clientId}] Registrou uma chave pix")
        }

        responseObserver?.onNext(
            KeyPixResponse.newBuilder()
                .setPixId(pixKey.id)
                .setClientId(pixKey.clientId)
                .build()
        )

        responseObserver?.onCompleted()
    }

    override fun removePixKey(request: RemoveKeyPixRequest?, responseObserver: StreamObserver<RemoveKeyPixResponse>?) {
        responseObserver?.onNext(removeKey.remove(request!!.toRemoveKeyRequest()))
        responseObserver?.onCompleted()
    }
}