package com.mikkaeru.pix

import com.mikkaeru.KeyPixRequest
import com.mikkaeru.KeyPixRequest.AccountType.UNKNOWN_ACCOUNT_TYPE
import com.mikkaeru.KeyPixRequest.KeyType.UNKNOWN_KEY_TYPE
import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.pix.client.ClientAccountResponse
import com.mikkaeru.pix.client.ItauClient
import com.mikkaeru.pix.dto.InstitutionResponse
import com.mikkaeru.pix.dto.OwnerResponse
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
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.StringContains.containsStringIgnoringCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.UUID.randomUUID
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyManagerTest(
    @Inject val repository: PixKeyRepository,
    @Inject val grpcClient: KeymanagerServiceGrpc.KeymanagerServiceBlockingStub
) {

    private lateinit var request: KeyPixRequest.Builder

    @Inject
    lateinit var itauClient: ItauClient

    companion object {
        val CLIENT_ID = randomUUID().toString()
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        request = KeyPixRequest
            .newBuilder()
            .setValue("teste@gmail.com")
            .setKeyType(KeyPixRequest.KeyType.EMAIL)
            .setClientId(CLIENT_ID)
            .setAccountType(KeyPixRequest.AccountType.CURRENT_ACCOUNT)
    }

    @Test
    fun `deve cadastrar uma chave pix`() {
        `when`(itauClient.findAccountById(CLIENT_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(clientAccountResponse()))

        val response = grpcClient.registerPixKey(request.build())

        with(response) {
            assertNotNull(this)
            assertNotNull(pixId)
            assertEquals(CLIENT_ID, clientId)
            assertEquals(1, repository.count())
        }
    }

    @Test
    fun `deve cadastrar uma chave pix com valor aleatorio`() {
        `when`(itauClient.findAccountById(CLIENT_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(clientAccountResponse()))

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
        `when`(itauClient.findAccountById(CLIENT_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.build())
        }

        with(exception) {
            assertThat(status.code, equalTo(NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Cliente não encontrado"))
        }
    }

    @Test
    fun `nao deve salvar a chave pix com um valor ja existente`() {

        val pixKey = PixKey(
            clientId = CLIENT_ID,
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

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    private fun clientAccountResponse(): ClientAccountResponse {
        return ClientAccountResponse(
            tipo = "CONTA_CORRENTE",
            numero = "291900",
            agencia = "0001",
            OwnerResponse(
                id = CLIENT_ID,
                nome = "Rafael M C Ponte",
                cpf = "02467781054"
            ), InstitutionResponse(
                nome = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"
            ))
    }
}

@Factory
class GrpcClient {

    @Singleton
    fun clientStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerServiceGrpc.KeymanagerServiceBlockingStub? {
        return KeymanagerServiceGrpc.newBlockingStub(channel)
    }
}