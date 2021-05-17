package com.mikkaeru.pix.client

import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_XML
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface BcbClient {

    @Post("/api/v1/pix/keys", consumes = [APPLICATION_XML], produces = [APPLICATION_XML])
    fun registerKey(@Body request: BcbCreateKeyRequest): HttpResponse<BcbCreateKeyResponse>

    @Delete("/api/v1/pix/keys/{key}", consumes = [APPLICATION_XML], produces = [APPLICATION_XML])
    fun deleteKey(@PathVariable key: String, @Body request: BcbDeleteKeyRequest): HttpResponse<BcbDeleteKeyResponse>

    @Get("/api/v1/pix/keys/{key}", consumes = [APPLICATION_XML])
    fun searchPixKey(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>
}

data class BcbCreateKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest
)

data class BcbCreateKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest,
    val createdAt: String
)

data class BcbDeleteKeyRequest(
    val key: String,
    val participant: String
)

data class BcbDeleteKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: String
)

data class BankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

data class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest,
    val createdAt: String
)

data class OwnerRequest(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String // CPF
)

enum class OwnerType {
    NATURAL_PERSON, LEGAL_PERSON
}