package com.mikkaeru.pix

import com.mikkaeru.SearchManagerServiceGrpc
import com.mikkaeru.SearchRequest
import com.mikkaeru.SearchResponse
import com.mikkaeru.pix.client.BcbClient
import com.mikkaeru.pix.extensions.toModel
import com.mikkaeru.pix.repository.PixKeyRepository
import com.mikkaeru.pix.shared.ExceptionHandler
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
}