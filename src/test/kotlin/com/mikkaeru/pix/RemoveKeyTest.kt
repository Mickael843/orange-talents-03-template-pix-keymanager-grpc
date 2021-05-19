package com.mikkaeru.pix

import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.RemoveKeyPixRequest
import com.mikkaeru.pix.client.BcbClient
import com.mikkaeru.pix.client.BcbDeleteKeyRequest
import com.mikkaeru.pix.client.BcbDeleteKeyResponse
import com.mikkaeru.pix.model.AccountType.CACC
import com.mikkaeru.pix.model.AssociatedAccount
import com.mikkaeru.pix.model.KeyType.RANDOM
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import io.grpc.Status
import io.grpc.Status.NOT_FOUND
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.FORBIDDEN
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.StringContains.containsStringIgnoringCase
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.UUID.randomUUID
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveKeyTest(
    @Inject val repository: PixKeyRepository,
    @Inject val grpcClient: KeymanagerServiceGrpc.KeymanagerServiceBlockingStub
) {

    companion object {
        val CLIENT_ID = randomUUID().toString()
    }

    private lateinit var pixKey: PixKey

    @Inject
    lateinit var bcbClient: BcbClient

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        pixKey = PixKey(
            clientId = CLIENT_ID,
            type = RANDOM,
            accountType = CACC,
            value = randomUUID().toString(),
            account = AssociatedAccount(
                agency = "0001",
                number = "291900",
                cpfOwner = "02467781054",
                nameOwner = "Rafael M C Ponte",
                institution = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"
            ))
    }

    @Test
    fun `deve remover uma chave pix`() {
        `when`(bcbClient.deleteKey(
            key = pixKey.value,
            BcbDeleteKeyRequest(
                key = pixKey.value,
                participant = pixKey.account.ispb
            ))).thenReturn(HttpResponse.ok(
            BcbDeleteKeyResponse(
                key = pixKey.value,
                participant = pixKey.account.ispb,
                deletedAt = LocalDateTime.now().toString()
            )))

        pixKey = repository.save(pixKey)

        val response = grpcClient.removePixKey(
            RemoveKeyPixRequest.newBuilder()
                .setClientId(CLIENT_ID)
                .setPixId(pixKey.id)
                .build()
        )

        with(response) {
            assertNotNull(pixId)
            assertThat(clientId, equalTo(CLIENT_ID))
        }
    }

    @Test
    fun `deve retornar status 404 na consulta ao banco central`() {
        `when`(bcbClient.deleteKey(
            key = pixKey.value,
            BcbDeleteKeyRequest(
                key = pixKey.value,
                participant = pixKey.account.ispb
            ))).thenReturn(HttpResponse.notFound())

        pixKey = repository.save(pixKey)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removePixKey(
                RemoveKeyPixRequest.newBuilder()
                    .setClientId(CLIENT_ID)
                    .setPixId(pixKey.id)
                    .build()
            )
        }

        with(exception) {
            assertThat(status.code, equalTo(NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix correspondente ao id ${pixKey.id} não foi encontrada no banco central"))
        }
    }

    @Test
    fun `deve retornar 403 na consulta ao banco central`() {
        `when`(bcbClient.deleteKey(
            key = pixKey.value,
            BcbDeleteKeyRequest(
                key = pixKey.value,
                participant = pixKey.account.ispb
            ))).thenReturn(HttpResponse.status(FORBIDDEN))

        pixKey = repository.save(pixKey)

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removePixKey(
                RemoveKeyPixRequest.newBuilder()
                    .setClientId(CLIENT_ID)
                    .setPixId(pixKey.id)
                    .build()
            )
        }

        with(exception) {
            assertThat(status.code, equalTo(Status.PERMISSION_DENIED.code))
            assertThat(status.description, containsStringIgnoringCase("Proibido a remoção dessa chave pix"))
        }
    }

    @Test
    fun `deve retornar status NOT_FOUND`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removePixKey(
                RemoveKeyPixRequest.newBuilder()
                    .setClientId(CLIENT_ID)
                    .setPixId(pixKey.id)
                    .build()
            )
        }

        with(exception) {
            assertThat(status.code, equalTo(NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix correspondente ao id ${pixKey.id} não foi encontrada!"))
        }
    }

    @Test
    fun `nao deve remover se nao for dono da chave pix`() {
        pixKey = repository.save(pixKey)

        val invalidClientId = randomUUID().toString()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removePixKey(
                RemoveKeyPixRequest.newBuilder()
                    .setClientId(invalidClientId)
                    .setPixId(pixKey.id)
                    .build()
            )
        }

        with(exception) {
            assertThat(status.code, equalTo(NOT_FOUND.code))
            assertThat(status.description, containsStringIgnoringCase("Chave pix correspondente ao id ${pixKey.id} não foi encontrada!"))
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}