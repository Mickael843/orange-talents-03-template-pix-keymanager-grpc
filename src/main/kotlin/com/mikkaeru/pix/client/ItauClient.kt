package com.mikkaeru.pix.client

import com.mikkaeru.pix.dto.InstitutionResponse
import com.mikkaeru.pix.dto.OwnerResponse
import com.mikkaeru.pix.model.AssociatedAccount
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091/api/v1/clientes")
interface ItauClient {

    @Get("/{id}/contas")
    fun findAccountById(@PathVariable id: String, @QueryValue tipo: String): HttpResponse<ClientAccountResponse?>
}

data class ClientAccountResponse(
    val tipo: String,
    val numero: String,
    val agencia: String,
    val titular: OwnerResponse,
    val instituicao: InstitutionResponse
) {

    fun toModel(): AssociatedAccount {
        return AssociatedAccount(
            agency = this.agencia,
            number = this.numero,
            cpfOwner= this.titular.cpf,
            nameOwner = this.titular.nome,
            institution = this.instituicao.nome
        )
    }
}