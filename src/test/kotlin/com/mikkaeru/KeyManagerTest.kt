package com.mikkaeru

import com.mikkaeru.KeyPixRequest.AccountType.CURRENT_ACCOUNT
import com.mikkaeru.KeyPixRequest.KeyType.*
import com.mikkaeru.KeymanagerServiceGrpc.KeymanagerServiceBlockingStub
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerTest(
    @Inject val repository: PixKeyRepository,
    @Inject val grpcClient: KeymanagerServiceBlockingStub
) {

    private lateinit var request: KeyPixRequest.Builder

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        request = KeyPixRequest
            .newBuilder()
            .setValue("teste@gmail.com")
            .setKeyType(EMAIL)
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setAccountType(CURRENT_ACCOUNT)
    }

    @Test
    internal fun `deve cadastrar uma chave pix`() {
        val response = grpcClient.registerPixKey(request.build())

        with(response) {
            assertNotNull(this)
            assertNotNull(pixId)
            assertEquals(1, repository.count())
        }
    }

    @Test
    internal fun `deve cadastrar uma chave pix com valor aleatorio`() {
        val response = grpcClient.registerPixKey(request.setKeyType(RANDOM_KEY).setValue("").build())

        val list = repository.findAll()

        with(response) {
            assertNotNull(this)
            assertNotNull(pixId)
            assertEquals(1, list.size)
            assertNotNull(list[list.size -1].value)
        }
    }

    @Test
    internal fun `nao deve cadastrar a chave pix ao passar valor maior que 77 caracteres`() {
        val value = "Um texto qualquer que deve ter mais ou menos um pouco mais de 77 caracteres que irão causar erro."

        assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(
                request.setValue(value)
                    .setKeyType(EMAIL)
                    .build()
            )
        }
    }

    @Test
    internal fun `nao deve cadastrar a chave pix ao passar cpf invalido`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setValue("123456").setKeyType(CPF).build())
        }
    }

    @Test
    internal fun `nao deve cadastrar a chave pix ao passar numero de telefone invalido`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setValue("1234").setKeyType(PHONE).build())
        }
    }
}

@Factory
class GrpcClient {

    @Singleton
    fun clientStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerServiceBlockingStub? {
        return KeymanagerServiceGrpc.newBlockingStub(channel)
    }
}