package com.mikkaeru

import com.mikkaeru.KeyPixRequest.KeyType
import com.mikkaeru.KeyPixRequest.KeyType.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Singleton
open class KeyManager(
    private val repository: PixKeyRepository,
    private val erbClient: ErbClient
): KeymanagerServiceGrpc.KeymanagerServiceImplBase() {

    private val logger = LoggerFactory.getLogger(KeyManager::class.java)

    override fun registerPixKey(request: KeyPixRequest?, responseObserver: StreamObserver<KeyPixResponse>?) {

        logger.info("Cadastrando a chave pix para o cliente: ${request?.clientId}")

        validation(request?.keyType, request?.value).ifPresent { exception ->
            responseObserver?.onError(exception)
        }

        if (!existsClient(request)) {
            responseObserver?.onError(Status.NOT_FOUND
                .withDescription("Conta não encontrada!")
                .asRuntimeException()
            )
        }

        var value: String = request!!.value

        if (request.keyType.equals(RANDOM_KEY) && value.isBlank()) {
            value = UUID.randomUUID().toString()
        }

        var pixKey = PixKey(
            value = value,
            clientId = request.clientId,
            keyType = com.mikkaeru.KeyType.valueOf(request.keyType.toString()),
            accountType = AccountType.valueOf(request.accountType.toString())
        )

        pixKey = repository.save(pixKey)

        responseObserver?.onNext(
            KeyPixResponse
                .newBuilder()
                .setPixId(pixKey.pixCode)
                .build()
        )

        responseObserver?.onCompleted()
    }

    open fun validation(@NotNull type: KeyType?, @Size(max = 77) value: String?): Optional<RuntimeException> {

        var exception = Optional.empty<RuntimeException>()

        when(type) {
            EMAIL -> {
                emailValidation(value!!)
            }

            CPF -> {
                if (!value!!.matches("^[0-9]{11}$".toRegex())) {
                     exception = Optional.of(Status.INVALID_ARGUMENT
                         .withDescription("CPF invalido!")
                         .asRuntimeException())
                }
            }

            PHONE -> {
                if (!value!!.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) {
                     exception = Optional.of(Status.INVALID_ARGUMENT
                         .withDescription("Numero de telefone invalido!")
                         .asRuntimeException())
                }
            }

            else -> { }
        }

        return exception
    }

    open fun emailValidation(@Email value: String) { }

    private fun existsClient(request: KeyPixRequest?): Boolean {
        return try {
            val response = erbClient.findAccountById(
                request!!.clientId,AccountType.valueOf(request.accountType.toString()
            ).equivalentName).body()

            response != null
        } catch (e: HttpClientResponseException) {
            false
        }
    }
}