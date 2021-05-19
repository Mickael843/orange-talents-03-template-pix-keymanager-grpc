package com.mikkaeru.pix

import com.mikkaeru.SearchAllRequest
import com.mikkaeru.SearchManagerServiceGrpc
import com.mikkaeru.SearchRequest
import com.mikkaeru.pix.client.*
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.AssociatedAccount
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.StringContains.containsStringIgnoringCase
import org.hamcrest.text.IsEmptyString.emptyOrNullString
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.UUID.randomUUID
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class SearchManagerTest(
    @Inject val repository: PixKeyRepository,
    @Inject val grpcClient: SearchManagerServiceGrpc.SearchManagerServiceBlockingStub
) {

    companion object {
        val CLIENT_ID = randomUUID().toString()
    }

    private lateinit var pixKeyTmp: PixKey

    @Inject
    lateinit var bcbClient: BcbClient

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        pixKeyTmp = PixKey(
            clientId = CLIENT_ID,
            type = KeyType.EMAIL,
            accountType = AccountType.CACC,
            value = "teste@gmail.com",
            account = associatedAccount()
        )
    }

    @Test
    fun `deve buscar um chave por clientId e pixId`() {
        val pixKeySaved = repository.save(pixKeyTmp)

        val response = grpcClient.searchPixKey(
            SearchRequest
                .newBuilder()
                .setPixId(
                    SearchRequest.FilterById.newBuilder()
                        .setClientId(pixKeySaved.clientId)
                        .setPixId(pixKeySaved.id)
                        .build()
                ).build())

        with(response) {
            assertNotNull(response)
            assertThat(pixId, equalTo(pixKeySaved.id))
            assertThat(pixKey.key, equalTo(pixKeySaved.value))
            assertThat(clientId, equalTo(pixKeySaved.clientId))
            assertThat(pixKey.type.name, equalTo(pixKeySaved.type.name))

            with(pixKey.owner) {
                assertThat(cpf, equalTo(pixKeySaved.account.cpfOwner))
                assertThat(name, equalTo(pixKeySaved.account.nameOwner))
            }

            with(pixKey.account) {
                assertThat(agency, equalTo(pixKeySaved.account.agency))
                assertThat(number, equalTo(pixKeySaved.account.number))
                assertThat(type.name, equalTo(pixKeySaved.accountType.name))
                assertThat(institution, equalTo(pixKeySaved.account.institution))
            }
        }
    }

    @Test
    fun `deve buscar uma chave pix pela chave `() {
        val pixKeySaved = repository.save(pixKeyTmp)

        val response = grpcClient.searchPixKey(
            SearchRequest
                .newBuilder()
                .setKey(pixKeySaved.value)
                .build())

        with(response) {
            assertNotNull(response)
            assertThat(pixId, equalTo(pixKeySaved.id))
            assertThat(pixKey.key, equalTo(pixKeySaved.value))
            assertThat(clientId, equalTo(pixKeySaved.clientId))
            assertThat(pixKey.type.name, equalTo(pixKeySaved.type.name))

            with(pixKey.owner) {
                assertThat(cpf, equalTo(pixKeySaved.account.cpfOwner))
                assertThat(name, equalTo(pixKeySaved.account.nameOwner))
            }

            with(pixKey.account) {
                assertThat(agency, equalTo(pixKeySaved.account.agency))
                assertThat(number, equalTo(pixKeySaved.account.number))
                assertThat(type.name, equalTo(pixKeySaved.accountType.name))
                assertThat(institution, equalTo(pixKeySaved.account.institution))
            }
        }
    }

    @Test
    fun `deve buscar uma chave pix pela chave no sistema do banco do brasil`() {
        `when`(bcbClient.searchPixKey(pixKeyTmp.value)).thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        val response = grpcClient.searchPixKey(
            SearchRequest
                .newBuilder()
                .setKey(pixKeyTmp.value)
                .build())

        with(response) {
            assertThat(pixId, emptyOrNullString())
            assertThat(clientId, emptyOrNullString())
            assertThat(pixKey.key, equalTo(pixKeyTmp.value))
            assertThat(pixKey.type.name, equalTo(pixKeyTmp.type.name))

            with(pixKey.owner) {
                assertThat(cpf, equalTo(pixKeyTmp.account.cpfOwner))
                assertThat(name, equalTo(pixKeyTmp.account.nameOwner))
            }

            with(pixKey.account) {
                assertThat(agency, equalTo(pixKeyTmp.account.agency))
                assertThat(number, equalTo(pixKeyTmp.account.number))
                assertThat(type.name, equalTo(pixKeyTmp.accountType.name))
                assertThat(institution, equalTo(pixKeyTmp.account.institution))
            }
        }
    }

    @Test
    fun `deve retornar status 404 ao buscar uma chave pix pelo pixId`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.searchPixKey(
                SearchRequest
                    .newBuilder()
                    .setPixId(
                        SearchRequest.FilterById.newBuilder()
                            .setClientId(pixKeyTmp.clientId)
                            .setPixId(pixKeyTmp.id)
                            .build())
                    .build()
            )
        }

        with(exception) {
            assertThat(status.code, equalTo(Status.NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix não encontrada"))
        }
    }

    @Test
    fun `deve retornar status 404  ao buscar uma chave pix no sistema do banco do brasil`() {
        `when`(bcbClient.searchPixKey(pixKeyTmp.value)).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.searchPixKey(
                SearchRequest
                    .newBuilder()
                    .setKey(pixKeyTmp.value)
                    .build()
            )
        }

        with(exception) {
            assertThat(status.code, equalTo(Status.NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix não encontrada"))
        }
    }

    @Test
    fun `deve retornar uma lista de chaves`() {
        repository.save(pixKeyTmp)

        val response = grpcClient.searchAllByOwner(
            SearchAllRequest.newBuilder().setClientId(pixKeyTmp.clientId).build()
        )

        with(response) {
            assertNotNull(response)
            assertThat(pixKeysCount, equalTo(1))
            assertThat(clientId, equalTo(pixKeyTmp.clientId))
        }
    }

    @Test
    fun `deve retornar uma lista vazia`() {
        val response = grpcClient.searchAllByOwner(
            SearchAllRequest.newBuilder().setClientId(pixKeyTmp.clientId).build()
        )

        with(response) {
            assertNotNull(response)
            assertThat(pixKeysCount, equalTo(0))
            assertThat(clientId, equalTo(pixKeyTmp.clientId))
        }
    }

    @Test
    fun `deve retornar um error de campo invalido`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.searchAllByOwner(
                SearchAllRequest.newBuilder().setClientId("").build()
            )
        }

        with(exception) {
            assertThat(status.code, equalTo(Status.INVALID_ARGUMENT.code))
            assertThat(status.description, equalTo("O campo clientId não pode ser nulo"))
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    private fun associatedAccount(): AssociatedAccount {
        return AssociatedAccount(
            agency = "0001",
            number = "291900",
            cpfOwner = "02467781054",
            nameOwner = "Rafael M C Ponte",
            institution = "ITAÚ UNIBANCO S.A.",
            ispb = "60701190"
        )
    }

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = pixKeyTmp.type,
            key = pixKeyTmp.value,
            bankAccount = BankAccountRequest(
                participant = pixKeyTmp.account.ispb,
                branch = pixKeyTmp.account.agency,
                accountType = pixKeyTmp.accountType,
                accountNumber = pixKeyTmp.account.number
            ),
            owner = OwnerRequest(
                type = OwnerType.NATURAL_PERSON,
                name = pixKeyTmp.account.nameOwner,
                taxIdNumber = pixKeyTmp.account.cpfOwner
            ),
            createdAt = pixKeyTmp.createdAt.toString()
        )
    }
}