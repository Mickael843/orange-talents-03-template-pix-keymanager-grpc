package com.mikkaeru.pix

import com.google.protobuf.Timestamp
import com.mikkaeru.AccountType
import com.mikkaeru.KeyType
import com.mikkaeru.SearchResponse
import com.mikkaeru.pix.dto.PixKeyInfo

class SearchResponseConverter {

    fun convert(pixKeyInfo: PixKeyInfo): SearchResponse {
        return SearchResponse.newBuilder()
            .setClientId(pixKeyInfo.clientId ?: "")
            .setPixId(pixKeyInfo.pixId ?: "")
            .setPixKey(
                SearchResponse.PixKey.newBuilder()
                    .setType(KeyType.valueOf(pixKeyInfo.type.name))
                    .setKey(pixKeyInfo.key)
                    .setOwner(
                        SearchResponse.PixKey.Owner.newBuilder()
                            .setCpf(pixKeyInfo.account.cpfOwner)
                            .setName(pixKeyInfo.account.nameOwner)
                            .build())
                    .setAccount(
                        SearchResponse.PixKey.Account.newBuilder()
                            .setInstitution(pixKeyInfo.account.institution)
                            .setAgency(pixKeyInfo.account.agency)
                            .setNumber(pixKeyInfo.account.number)
                            .setType(AccountType.valueOf(pixKeyInfo.accountType.name))
                            .build())
                    .setCreateAt(
                        Timestamp.newBuilder()
                            .setNanos(pixKeyInfo.createAt.nano)
                            .setSeconds(pixKeyInfo.createAt.second.toLong())
                            .build()
                    )).build()
    }
}