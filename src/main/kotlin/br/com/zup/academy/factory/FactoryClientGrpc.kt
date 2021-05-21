package br.com.zup.academy.factory

import br.com.zup.academy.KeyManagerCadastrarGrpcServiceGrpc.newBlockingStub
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class FactoryClientGrpc(@GrpcChannel(value = "KeyManager") val channel: ManagedChannel) {

    @Singleton
    fun cadastraChaveStub() = newBlockingStub(channel)
}