package br.com.zup.academy.factory

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton
import br.com.zup.academy.KeyManagerCadastrarGrpcServiceGrpc.newBlockingStub as cadastroBlockinStub
import br.com.zup.academy.KeyManagerRemoveGrpcServiceGrpc.newBlockingStub as removeBlockingStub

@Factory
class FactoryClientGrpc(@GrpcChannel(value = "KeyManager") val channel: ManagedChannel) {

    @Singleton
    fun cadastraChaveStub() = cadastroBlockinStub(channel)

    @Singleton
    fun removeChaveStub() = removeBlockingStub(channel)
}