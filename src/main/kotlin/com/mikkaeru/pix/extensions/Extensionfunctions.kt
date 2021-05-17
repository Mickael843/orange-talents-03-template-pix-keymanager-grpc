package com.mikkaeru.pix.extensions

import com.mikkaeru.AccountType.UNKNOWN_ACCOUNT_TYPE
import com.mikkaeru.KeyPixRequest
import com.mikkaeru.KeyType.UNKNOWN_KEY_TYPE
import com.mikkaeru.RemoveKeyPixRequest
import com.mikkaeru.SearchRequest
import com.mikkaeru.pix.dto.Filter
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.dto.RemoveKeyRequest
import com.mikkaeru.pix.model.AccountType
import com.mikkaeru.pix.model.KeyType
import io.micronaut.validation.validator.Validator
import javax.validation.ConstraintViolationException

fun KeyPixRequest.toKeyRequest(): KeyRequest {
    return KeyRequest(
        clientId = clientId,
        key = key,
        type = when (type) {
            UNKNOWN_KEY_TYPE -> null
            else -> KeyType.valueOf(type.name)
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

fun SearchRequest.toModel(validator: Validator): Filter {
    val filter = when (filterCase!!) {
        SearchRequest.FilterCase.PIXID -> pixId.let {
            Filter.ByPixId(clientId = it.clientId, pixId = it.pixId)
        }

        SearchRequest.FilterCase.KEY -> Filter.ByKey(key)
        SearchRequest.FilterCase.FILTER_NOT_SET -> Filter.Invalid()
    }

    val violations = validator.validate(filter)

    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filter
}