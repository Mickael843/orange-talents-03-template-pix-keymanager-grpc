package com.mikkaeru

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import javax.inject.Singleton

@Factory
class GrpcClient {

    @Singleton
    fun clientStubKeyManager(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerServiceGrpc.KeymanagerServiceBlockingStub? {
        return KeymanagerServiceGrpc.newBlockingStub(channel)
    }

    @Singleton
    fun clientStubSearchManager(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): SearchManagerServiceGrpc.SearchManagerServiceBlockingStub? {
        return SearchManagerServiceGrpc.newBlockingStub(channel)
    }
}