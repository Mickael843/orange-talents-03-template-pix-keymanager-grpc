package com.mikkaeru.pix

import com.google.protobuf.Timestamp
import com.mikkaeru.*
import com.mikkaeru.pix.client.BcbClient
import com.mikkaeru.pix.extensions.toModel
import com.mikkaeru.pix.repository.PixKeyRepository
import com.mikkaeru.pix.shared.ExceptionHandler
import com.mikkaeru.pix.shared.exception.InvalidArgumentException
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class SearchManager(
    @Inject private val bcbClient: BcbClient,
    @Inject private val validator: Validator,
    @Inject private val repository: PixKeyRepository
): SearchManagerServiceGrpc.SearchManagerServiceImplBase() {

    override fun searchPixKey(request: SearchRequest?, responseObserver: StreamObserver<SearchResponse>?) {
        val filter = request?.toModel(validator)
        val response = filter!!.filter(repository, bcbClient)

        responseObserver?.onNext(SearchResponseConverter().convert(response))
        responseObserver?.onCompleted()
    }

    override fun searchAllByOwner(request: SearchAllRequest?, responseObserver: StreamObserver<SearchAllResponse>?) {
        if (request?.clientId.isNullOrEmpty()) {
            throw InvalidArgumentException("O campo clientId não pode ser nulo")
        }

        val pixKeyDetails = repository.findAllByClientId(clientId = request!!.clientId).map {
            SearchAllResponse.PixKeyDetails.newBuilder()
                .setPixId(it.id)
                .setType(KeyType.valueOf(it.type.name))
                .setKey(it.key)
                .setAccountType(AccountType.valueOf(it.accountType.name))
                .setCreateAt(
                    Timestamp.newBuilder().setNanos(it.createdAt.nano).setSeconds(it.createdAt.second.toLong()).build()
                ).build()
        }

        responseObserver?.onNext(
            SearchAllResponse.newBuilder()
                .setClientId(request.clientId)
                .addAllPixKeys(pixKeyDetails)
                .build())

        responseObserver?.onCompleted()
    }
}