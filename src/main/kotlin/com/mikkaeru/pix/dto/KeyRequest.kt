package com.mikkaeru.pix.dto

import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.AssociatedAccount
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.model.PixKey
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class KeyRequest(
    @field:NotBlank(message = "clientId não estar em branco!")
    val clientId: String,

    @field:Size(max = 77, message = "O valor maximo de caracteres é de 77!")
    val key: String,

    @field:NotNull(message = "O tipo da chave não pode ser nulo!")
    val type: KeyType?,

    @field:NotNull(message = "O tipo da conta não pode ser nulo!")
    val accountType: AccountType?
) {

    fun toModel(account: AssociatedAccount): PixKey {
        return PixKey(
            clientId = this.clientId,
            type = this.type!!,
            value = this.key,
            accountType = this.accountType!!,
            account = account
        )
    }
}
