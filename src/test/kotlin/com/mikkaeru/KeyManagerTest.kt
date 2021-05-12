package com.mikkaeru

import com.mikkaeru.KeyPixRequest.AccountType.CURRENT_ACCOUNT
import com.mikkaeru.KeyPixRequest.KeyType.*
import com.mikkaeru.KeymanagerServiceGrpc.KeymanagerServiceBlockingStub
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.AssociatedAccount
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
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
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerTest(
    @Inject val repository: PixKeyRepository,
    @Inject val grpcClient: KeymanagerServiceBlockingStub
) {

    private lateinit var request: KeyPixRequest.Builder

    private val clientId: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        request = KeyPixRequest
            .newBuilder()
            .setValue("teste@gmail.com")
            .setKeyType(EMAIL)
            .setClientId(clientId)
            .setAccountType(CURRENT_ACCOUNT)
    }

    @Test
    internal fun `deve cadastrar uma chave pix`() {
        val response = grpcClient.registerPixKey(request.build())

        with(response) {
            assertNotNull(this)
            assertNotNull(pixId)
            assertEquals(1, repository.count())
            assertEquals(request.clientId, clientId)
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
            assertEquals(request.clientId, clientId)
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
    internal fun `nao deve cadastrar a chave pix se o cliente nao existir`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setClientId(UUID.randomUUID().toString()).build())
        }
    }

    @Test
    internal fun `nao deve cadastrar a chave pix ao passar numero de telefone invalido`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setValue("1234").setKeyType(PHONE).build())
        }
    }

    @Test
    internal fun `nao deve salvar a chave pix com um valor ja existente`() {

        val pixKey = PixKey(
            clientId = clientId,
            keyType = KeyType.EMAIL,
            accountType = AccountType.CURRENT_ACCOUNT,
            value = "teste@gmail.com",
            account = AssociatedAccount(
                agency = "0001",
                number = "291900",
                cpfOwner = "02467781054",
                nameOwner = "Rafael M C Ponte",
                institution = "ITAÚ UNIBANCO S.A."
            )
        )

        repository.save(pixKey)

        assertThrows<RuntimeException> {
            grpcClient.registerPixKey(request.setValue(pixKey.value).setKeyType(EMAIL).build())
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