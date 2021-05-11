package com.mikkaeru

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091/api/v1/clientes")
interface ErbClient {

    @Get("/{id}/contas")
    fun findAccountById(@PathVariable id: String, @QueryValue tipo: String): HttpResponse<ClientAccountResponse?>
}

data class ClientAccountResponse(val tipo: String, val agencia: String, val numero: String)