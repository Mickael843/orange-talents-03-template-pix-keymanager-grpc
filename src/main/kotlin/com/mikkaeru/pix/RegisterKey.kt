package com.mikkaeru.pix

import com.mikkaeru.pix.client.*
import com.mikkaeru.pix.client.OwnerType.NATURAL_PERSON
import com.mikkaeru.pix.dto.KeyRequest
import com.mikkaeru.pix.model.KeyType
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import com.mikkaeru.pix.shared.exception.ExistingPixKeyException
import com.mikkaeru.pix.shared.exception.NotFoundException
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class RegisterKey(
    @Inject private val bcbClient: BcbClient,
    @Inject private val itauClient: ItauClient,
    @Inject private val repository: PixKeyRepository
) {

    @Transactional
    fun register(@Valid request: KeyRequest): PixKey {
        request.run {
            type!!.validate(key)
        }

        if (repository.existsByKey(request.key)) {
            throw ExistingPixKeyException("Chave pix ${request.key} existente")
        }

        val itauResponse = itauClient.findAccountById(request.clientId, request.accountType!!.equivalentName)

        val account = itauResponse.body()?.toModel() ?: throw NotFoundException("Cliente não encontrado")

        val pixKey: PixKey?
        val pixKeyTmp = request.toModel(account)

        bcbClient.registerKey(pixKeyTmp.toBcbKeyRequest()).also {
            if (it.status.code == 422) {
                throw ExistingPixKeyException("Chave pix ${request.key} já cadastrada no Banco central")
            }

            val body = it.body()!!

            pixKey = if (body.keyType == KeyType.RANDOM) {
                PixKey(
                    clientId = pixKeyTmp.clientId,
                    type = pixKeyTmp.type,
                    accountType = pixKeyTmp.accountType,
                    key = body.key,
                    account = pixKeyTmp.account
                )
            } else { pixKeyTmp }
        }

        return repository.save(pixKey!!)
    }

    private fun PixKey.toBcbKeyRequest(): BcbCreateKeyRequest {
        return BcbCreateKeyRequest(
            keyType = type,
            key = key,
            bankAccount = BankAccountRequest(
                participant = account.ispb,
                branch = account.agency,
                accountType = accountType,
                accountNumber = account.number
            ),
            owner = OwnerRequest(
                type = NATURAL_PERSON,
                name = account.nameOwner,
                taxIdNumber = account.cpfOwner
            )
        )
    }
}