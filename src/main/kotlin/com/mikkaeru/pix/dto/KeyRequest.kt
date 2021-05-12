package com.mikkaeru.pix.dto

import com.mikkaeru.pix.anotation.ValidUUID
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.AssociatedAccount
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.model.KeyType.RANDOM_KEY
import com.mikkaeru.pix.model.PixKey
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class KeyRequest(
    @ValidUUID
    @field:NotBlank val clientId: String,
    @field:Size(max = 77) val key: String,
    @field:NotNull val type: KeyType?,
    @field:NotNull val accountType: AccountType?
) {

    fun toModel(account: AssociatedAccount): PixKey {
        return PixKey(
            clientId = this.clientId,
            keyType = this.type!!,
            value = if (this.type == RANDOM_KEY) UUID.randomUUID().toString() else this.key,
            accountType = this.accountType!!,
            account = account
        )
    }
}
