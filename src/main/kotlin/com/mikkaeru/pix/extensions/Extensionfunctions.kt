package com.mikkaeru.pix.extensions

import com.mikkaeru.KeyPixRequest
import com.mikkaeru.KeyPixRequest.AccountType.UNKNOWN_ACCOUNT_TYPE
import com.mikkaeru.KeyPixRequest.KeyType.UNKNOWN_KEY_TYPE
import com.mikkaeru.RemoveKeyPixRequest
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.dto.RemoveKeyRequest
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType

fun KeyPixRequest.toKeyRequest(): KeyRequest {
    return KeyRequest(
        clientId = clientId,
        key = value,
        type = when (keyType) {
            UNKNOWN_KEY_TYPE -> null
            else -> KeyType.valueOf(keyType.name)
        },
        accountType = when(accountType) {
            UNKNOWN_ACCOUNT_TYPE -> null
            else -> AccountType.valueOf(accountType.name)
        }
    )
}

fun RemoveKeyPixRequest.toRemoveKeyRequest(): RemoveKeyRequest {
    return RemoveKeyRequest(clientId = clientId, pixId = pixId)
}