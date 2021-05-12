package com.mikkaeru.pix

import com.mikkaeru.KeyPixRequest
import com.mikkaeru.KeyPixRequest.AccountType.UNKNOWN_ACCOUNT_TYPE
import com.mikkaeru.KeyPixRequest.KeyType.UNKNOWN_KEY_TYPE
import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.AssociatedAccount
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status.*
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.StringContains.containsStringIgnoringCase
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
    @Inject val grpcClient: KeymanagerServiceGrpc.KeymanagerServiceBlockingStub
) {

    private lateinit var request: KeyPixRequest.Builder

    private val clientId: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        request = KeyPixRequest
            .newBuilder()
            .setValue("teste@gmail.com")
            .setKeyType(KeyPixRequest.KeyType.EMAIL)
            .setClientId(clientId)
            .setAccountType(KeyPixRequest.AccountType.CURRENT_ACCOUNT)
    }

    @Test
    fun `deve cadastrar uma chave pix`() {
        val response = grpcClient.registerPixKey(request.build())

        with(response) {
            assertNotNull(this)
            assertNotNull(pixId)
            assertEquals(1, repository.count())
            assertEquals(request.clientId, clientId)
        }
    }

    @Test
    fun `deve cadastrar uma chave pix com valor aleatorio`() {
        val response = grpcClient.registerPixKey(request.setKeyType(KeyPixRequest.KeyType.RANDOM_KEY).setValue("").build())

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
    fun `nao deve cadastrar a chave pix quando o tipo da chave for desconhecido`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setKeyType(UNKNOWN_KEY_TYPE).build())
        }

        with(exception) {
            assertThat(status.code, equalTo(INVALID_ARGUMENT.code))
            assertThat(status.description, containsStringIgnoringCase("O tipo da chave não pode ser nulo!"))
        }
    }

    @Test
    fun `nao deve cadastrar a chave pix quando o tipo da conta for desconhecido`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setAccountType(UNKNOWN_ACCOUNT_TYPE).build())
        }

        with(exception) {
            assertThat(status.code, equalTo(INVALID_ARGUMENT.code))
            assertThat(status.description, containsStringIgnoringCase("O tipo da conta não pode ser nulo!"))
        }
    }

    @Test
    fun `nao deve cadastrar a chave pix ao passar valor maior que 77 caracteres`() {
        val value = "Um texto qualquer que deve ter mais ou menos um pouco mais de 77 caracteres que irão causar erro."

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(
                request.setValue(value)
                    .setKeyType(KeyPixRequest.KeyType.EMAIL)
                    .build()
            )
        }

        with(exception) {
            assertThat(status.code, equalTo(INVALID_ARGUMENT.code))
            assertThat(status.description, containsStringIgnoringCase("O valor maximo de caracteres é de 77!"))
        }
    }

    @Test
    fun `nao deve cadastrar a chave pix ao passar cpf invalido`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setValue("123456").setKeyType(KeyPixRequest.KeyType.CPF).build())
        }

        with(exception) {
            assertThat(status.code, equalTo(INVALID_ARGUMENT.code))
            assertThat(status.description, containsStringIgnoringCase("CPF invalido"))
        }
    }

    @Test
    fun `nao deve cadastrar a chave pix ao passar numero de telefone invalido`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setValue("1234").setKeyType(KeyPixRequest.KeyType.PHONE).build())
        }

        with(exception) {
            assertThat(status.code, equalTo(INVALID_ARGUMENT.code))
            assertThat(status.description, containsStringIgnoringCase("Numero de telefone invalido!"))
        }
    }

    @Test
    fun `nao deve cadastrar a chave pix se o cliente nao existir`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setClientId(UUID.randomUUID().toString()).build())
        }

        with(exception) {
            assertThat(status.code, equalTo(NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Cliente não encontrado"))
        }
    }

    @Test
    fun `nao deve salvar a chave pix com um valor ja existente`() {

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

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setValue(pixKey.value).setKeyType(KeyPixRequest.KeyType.EMAIL).build())
        }

        with(exception) {
            assertThat(status.code, equalTo(ALREADY_EXISTS.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix ${pixKey.value} existente"))
        }
    }
}

@Factory
class GrpcClient {

    @Singleton
    fun clientStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerServiceGrpc.KeymanagerServiceBlockingStub? {
        return KeymanagerServiceGrpc.newBlockingStub(channel)
    }
}