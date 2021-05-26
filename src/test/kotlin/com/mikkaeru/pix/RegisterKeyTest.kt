package com.mikkaeru.pix

import com.mikkaeru.AccountType.CACC
import com.mikkaeru.AccountType.UNKNOWN_ACCOUNT_TYPE
import com.mikkaeru.KeyPixRequest
import com.mikkaeru.KeyType.*
import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.pix.client.*
import com.mikkaeru.pix.dto.InstitutionResponse
import com.mikkaeru.pix.dto.OwnerResponse
import com.mikkaeru.pix.model.AssociatedAccount
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import io.grpc.Status.*
import io.grpc.StatusRuntimeException
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
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.UUID.randomUUID
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class KeyManagerTest(
    @Inject val repository: PixKeyRepository,
    @Inject val grpcClient: KeymanagerServiceGrpc.KeymanagerServiceBlockingStub
) {

    companion object {
        val CLIENT_ID = randomUUID().toString()
    }

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    private lateinit var request: KeyPixRequest.Builder

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        request = KeyPixRequest
            .newBuilder()
            .setKey("teste@gmail.com")
            .setType(EMAIL)
            .setClientId(CLIENT_ID)
            .setAccountType(CACC)
    }

    @Test
    fun `deve cadastrar uma chave pix`() {
        val accountResponse = clientAccountResponse()

        `when`(itauClient.findAccountById(CLIENT_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))

        `when`(bcbClient.registerKey(bcbCreateKeyRequest()))
            .thenReturn(HttpResponse.created(bcbCreateKeyResponse()))

        val response = grpcClient.registerPixKey(request.build())

        verify(itauClient, times(1)).findAccountById(CLIENT_ID, "CONTA_CORRENTE")
        verify(bcbClient, times(1)).registerKey(bcbCreateKeyRequest())

        with(response) {
            assertNotNull(this)
            assertNotNull(pixId)
            assertEquals(CLIENT_ID, clientId)
            assertEquals(1, repository.count())
        }
    }

    @Test
    fun `nao deve cadastrar uma chave pix que ja foi cadastrada no bcb`() {
        `when`(itauClient.findAccountById(CLIENT_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(clientAccountResponse()))

        `when`(bcbClient.registerKey(bcbCreateKeyRequest())).thenReturn(HttpResponse.unprocessableEntity())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.build())
        }

        with(exception) {
            assertEquals(0, repository.count())
            assertThat(status.code, equalTo(ALREADY_EXISTS.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix ${request.key} já cadastrada no Banco central"))
        }

        verify(itauClient, times(1)).findAccountById(CLIENT_ID, "CONTA_CORRENTE")
        verify(bcbClient, times(1)).registerKey(bcbCreateKeyRequest())
    }

    @Test
    fun `deve cadastrar uma chave pix com valor aleatorio`() {
        val accountResponse = clientAccountResponse()

        `when`(itauClient.findAccountById(CLIENT_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))

        `when`(bcbClient.registerKey(BcbCreateKeyRequest(
            keyType = KeyType.RANDOM,
            key = "",
            bankAccount = bankAccountRequest(accountResponse),
            owner = ownerRequest(accountResponse)
        ))).thenReturn(HttpResponse.created(BcbCreateKeyResponse(
            keyType = KeyType.RANDOM,
            key = randomUUID().toString(),
            bankAccount = bankAccountRequest(accountResponse),
            owner = ownerRequest(accountResponse),
            createdAt = LocalDateTime.now().toString()
        )))

        val response = grpcClient.registerPixKey(request.setType(RANDOM).setKey("").build())

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
            grpcClient.registerPixKey(request.setType(UNKNOWN_KEY_TYPE).build())
        }

        with(exception) {
            assertEquals(0, repository.count())
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
            assertEquals(0, repository.count())
            assertThat(status.code, equalTo(INVALID_ARGUMENT.code))
            assertThat(status.description, containsStringIgnoringCase("O tipo da conta não pode ser nulo!"))
        }
    }

    @Test
    fun `nao deve cadastrar a chave pix ao passar cpf invalido`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setKey("13465785890").setType(CPF).build())
        }

        with(exception) {
            assertEquals(0, repository.count())
            assertThat(status.code, equalTo(INVALID_ARGUMENT.code))
            assertThat(status.description, containsStringIgnoringCase("Invalid format CPF"))
        }
    }

    @Test
    fun `nao deve cadastrar a chave pix ao passar numero de telefone invalido`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setKey("999309941").setType(PHONE).build())
        }

        with(exception) {
            assertEquals(0, repository.count())
            assertThat(status.code, equalTo(INVALID_ARGUMENT.code))
            assertThat(status.description, containsStringIgnoringCase("Invalid format PHONE"))
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
            assertEquals(0, repository.count())
            assertThat(status.code, equalTo(NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Cliente não encontrado"))
        }
    }

    @Test
    fun `nao deve salvar a chave pix com um valor ja existente`() {

        val pixKey = PixKey(
            clientId = CLIENT_ID,
            type = KeyType.EMAIL,
            accountType = com.mikkaeru.pix.model.AccountType.CACC,
            value = "teste@gmail.com",
            account = AssociatedAccount(
                agency = "0001",
                number = "291900",
                cpfOwner = "02467781054",
                nameOwner = "Rafael M C Ponte",
                institution = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"
            )
        )

        repository.save(pixKey)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registerPixKey(request.setKey(pixKey.value).setType(EMAIL).build())
        }

        with(exception) {
            assertEquals(1, repository.count())
            assertThat(status.code, equalTo(ALREADY_EXISTS.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix ${pixKey.value} existente"))
        }
    }

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
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

    private fun bcbCreateKeyRequest(): BcbCreateKeyRequest {
        val accountResponse = clientAccountResponse()

        return BcbCreateKeyRequest(
            keyType = KeyType.valueOf(request.type.name),
            key = request.key,
            bankAccount = bankAccountRequest(accountResponse),
            owner = ownerRequest(accountResponse)
        )
    }

    private fun bcbCreateKeyResponse(): BcbCreateKeyResponse {
        val accountResponse = clientAccountResponse()

        return BcbCreateKeyResponse(
            keyType = KeyType.valueOf(request.type.name),
            key = request.key,
            bankAccount = bankAccountRequest(accountResponse),
            owner = ownerRequest(accountResponse),
            createdAt = LocalDateTime.now().toString()
        )
    }

    private fun bankAccountRequest(accountResponse: ClientAccountResponse): BankAccountRequest {
        return BankAccountRequest(
            participant = accountResponse.instituicao.ispb,
            branch = accountResponse.agencia,
            accountType = com.mikkaeru.pix.model.AccountType.valueOf(request.accountType.name),
            accountNumber = accountResponse.numero
        )
    }

    private fun ownerRequest(accountResponse: ClientAccountResponse): OwnerRequest {
        return OwnerRequest(
            type = OwnerType.NATURAL_PERSON,
            name = accountResponse.titular.nome,
            taxIdNumber = accountResponse.titular.cpf
        )
    }
}