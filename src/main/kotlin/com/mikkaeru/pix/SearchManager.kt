package com.mikkaeru.pix

import com.mikkaeru.*
import com.mikkaeru.SearchPixKeyResponse.Account
import com.mikkaeru.SearchPixKeyResponse.Owner
import com.mikkaeru.pix.model.PixKey
import com.mikkaeru.pix.repository.PixKeyRepository
import com.mikkaeru.pix.shared.ExceptionHandler
import com.mikkaeru.pix.shared.exception.NotFoundException
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class SearchManager(
    @Inject private val repository: PixKeyRepository
): KeymanagerServiceGrpc.KeymanagerServiceImplBase() {

    override fun searchPixKeyByClientIdAndPixId(request: SearchPixKeyRequest?, responseObserver: StreamObserver<SearchPixKeyResponse>?) {
        if (request?.pixId.isNullOrEmpty()) {
            throw IllegalStateException("O campo pixId não pode ser nulo")
        } else if (request?.clientId.isNullOrEmpty()) {
            throw IllegalStateException("O campo clientId não pode ser nulo")
        }

        val pixKey = repository.findByIdAndClientId(request!!.pixId, request.clientId).orElseThrow {
            throw NotFoundException("Chave pix não foi encontrada")
        }

        responseObserver?.onNext(searchPixKeyResponse(pixKey))
        responseObserver?.onCompleted()
    }

    override fun searchPixKeyByKey(request: SearchByKeyRequest?, responseObserver: StreamObserver<SearchPixKeyResponse>?) {
        if (request?.key.isNullOrEmpty()) {
            throw IllegalStateException("A chave não pode estar em branco")
        }

        val pixKey = repository.findByKey(request!!.key).orElseThrow {
            throw NotFoundException("Chave pix '${request.key}' não foi encontrada")
        }

        responseObserver?.onNext(searchPixKeyResponse(pixKey))
        responseObserver?.onCompleted()
    }

    private fun searchPixKeyResponse(pixKey: PixKey): SearchPixKeyResponse {
        return SearchPixKeyResponse.newBuilder()
            .setPixId(pixKey.id)
            .setClientId(pixKey.clientId)
            .setType(KeyType.valueOf(pixKey.type.name))
            .setKey(pixKey.key)
            .setOwner(
                Owner.newBuilder()
                    .setCpf(pixKey.account.cpfOwner)
                    .setName(pixKey.account.nameOwner)
                    .build()
            )
            .setAccount(
                Account.newBuilder()
                    .setInstitution(pixKey.account.institution)
                    .setAgency(pixKey.account.agency)
                    .setNumber(pixKey.account.number)
                    .setType(AccountType.valueOf(pixKey.accountType.name))
            )
            .setCreateAt(pixKey.createdAt.toString())
            .build()
    }
}