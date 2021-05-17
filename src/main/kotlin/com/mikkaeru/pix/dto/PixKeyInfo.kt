package com.mikkaeru.pix.dto

import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.AssociatedAccount
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.model.PixKey
import java.time.OffsetDateTime
import java.time.OffsetDateTime.now

data class PixKeyInfo(
    val pixId: String? = null,
    val clientId: String? = null,
    val type: KeyType,
    val key: String,
    val accountType: AccountType,
    val account: AssociatedAccount,
    val createAt: OffsetDateTime = now()
) {

    companion object {

        fun of(pixKey: PixKey): PixKeyInfo {
            return PixKeyInfo(
                pixId = pixKey.id,
                clientId = pixKey.clientId,
                type = pixKey.type,
                key = pixKey.key,
                accountType = pixKey.accountType,
                account = pixKey.account,
                createAt = pixKey.createdAt
            )
        }
    }
}
