package com.mikkaeru.pix

import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.SearchByKeyRequest
import com.mikkaeru.SearchPixKeyRequest
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.AssociatedAccount
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import io.grpc.Status.NOT_FOUND
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsStringIgnoringCase
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID.randomUUID
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class SearchManagerTest(
    @Inject val repository: PixKeyRepository,
    @Inject val grpcClient: KeymanagerServiceGrpc.KeymanagerServiceBlockingStub
) {

    companion object {
        val CLIENT_ID = randomUUID().toString()
    }

    private lateinit var pixKey: PixKey

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        pixKey = PixKey(
            clientId = CLIENT_ID,
            type = KeyType.EMAIL,
            accountType = AccountType.CACC,
            key = "teste@gmail.com",
            account = associatedAccount()
        )
    }

    @Test
    fun `deve buscar uma chave pix por pixId e clientId`() {
        pixKey = repository.save(pixKey)

        val response = grpcClient.searchPixKeyByClientIdAndPixId(
            SearchPixKeyRequest
                .newBuilder()
                .setClientId(pixKey.clientId)
                .setPixId(pixKey.id)
                .build()
        )

        with(response) {
            assertNotNull(response)
            assertThat(key, equalTo(pixKey.key))
            assertThat(pixId, equalTo(pixKey.id))
            assertThat(clientId, equalTo(pixKey.clientId))
            assertThat(type.name, equalTo(pixKey.type.name))

            with(owner) {
                assertThat(cpf, equalTo(pixKey.account.cpfOwner))
                assertThat(name, equalTo(pixKey.account.nameOwner))
            }

            with(account) {
                assertThat(agency, equalTo(pixKey.account.agency))
                assertThat(number, equalTo(pixKey.account.number))
                assertThat(type.name, equalTo(pixKey.accountType.name))
                assertThat(institution, equalTo(pixKey.account.institution))
            }
        }
    }

    @Test
    fun `deve buscar uma chave pix apenas pela chave`() {
        pixKey = repository.save(pixKey)

        val response = grpcClient.searchPixKeyByKey(
            SearchByKeyRequest.newBuilder().setKey(pixKey.key).build()
        )

        with(response) {
            assertNotNull(response)
            assertThat(key, equalTo(pixKey.key))
            assertThat(pixId, equalTo(pixKey.id))
            assertThat(clientId, equalTo(pixKey.clientId))
            assertThat(type.name, equalTo(pixKey.type.name))

            with(owner) {
                assertThat(cpf, equalTo(pixKey.account.cpfOwner))
                assertThat(name, equalTo(pixKey.account.nameOwner))
            }

            with(account) {
                assertThat(agency, equalTo(pixKey.account.agency))
                assertThat(number, equalTo(pixKey.account.number))
                assertThat(type.name, equalTo(pixKey.accountType.name))
                assertThat(institution, equalTo(pixKey.account.institution))
            }
        }


    }

    @Test
    fun `deve dar uma exception NOT_FOUND para um clientId e um pixId inexistente`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.searchPixKeyByClientIdAndPixId(
                SearchPixKeyRequest
                    .newBuilder()
                    .setClientId(pixKey.clientId)
                    .setPixId(pixKey.id)
                    .build()
            )
        }

        with(exception) {
            assertThat(status.code, equalTo(NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix não foi encontrada"))
        }
    }

    @Test
    fun `deve dar uma exception NOT_FOUND para uma chave inexistente`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.searchPixKeyByKey(SearchByKeyRequest.newBuilder().setKey(pixKey.key).build())
        }

        with(exception) {
            assertThat(status.code, equalTo(NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix '${pixKey.key}' não foi encontrada"))
        }
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
}