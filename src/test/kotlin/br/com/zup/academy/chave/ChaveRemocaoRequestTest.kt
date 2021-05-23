package br.com.zup.academy.chave

import br.com.zup.academy.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.academy.KeyPixRequestRemove
import br.com.zup.academy.KeyPixResponseRemove
import br.com.zup.academy.factory.FactoryClientGrpc
import io.grpc.Status
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.mockito.BDDMockito
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@TestMethodOrder(OrderAnnotation::class)
@MicronautTest
internal class ChaveRemocaoRequestTest {

    @field:Inject
    lateinit var blockingStub: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub

    @field:Inject
    @field:Client("/")
    lateinit var httpClient: HttpClient

    companion object {
        val CLIENT_ID = "c56dfef4-7901-44fb-84e2-a2cefb157890"
        val PIX_ID = UUID.randomUUID().toString()
    }

    @AfterEach
    internal fun tearDown() {
        Mockito.reset(blockingStub)
    }

    @Test
    @Order(1)
    fun `deve remover a chave PIX com sucesso`() {
        BDDMockito.given(blockingStub.removerChavePix(grpcRequest())).willReturn(grpcResponse())

        val response = httpClient.toBlocking()
            .exchange(HttpRequest.DELETE("/chave-pix/remove", restRequest()), ChaveRemocaoRequest::class.java)

        with(response.status) {
            assertEquals(HttpStatus.NO_CONTENT.code, code)
        }
    }

    @Test
    @Order(2)
    fun `nao deve remover uma chave PIX inexistente`() {
        BDDMockito.given(blockingStub.removerChavePix(grpcRequest())).willThrow(Status.NOT_FOUND.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            val response = httpClient.toBlocking()
                .exchange(HttpRequest.DELETE("/chave-pix/remove", restRequest()), ChaveRemocaoRequest::class.java)
        }

        with(responseException) {
            assertEquals(HttpStatus.NOT_FOUND.code, status.code)
            assertEquals("Not Found", message)
        }
    }

    @Test
    @Order(3)
    fun `nao deve remover uma chave PIX com dados de entrada invalidos`() {
        val restRequest = ChaveRemocaoRequest(pixId = "PIX_ID_INVALIDO", clientId = "CLIENT_ID_INVALIDO")
        val grpcRequest = KeyPixRequestRemove.newBuilder().setClientId("CLIENT_ID_INVALIDO").setPixId("PIX_ID_INVALIDO").build()

        BDDMockito.given(blockingStub.removerChavePix(grpcRequest())).willThrow(Status.INVALID_ARGUMENT.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            val response = httpClient.toBlocking()
                .exchange(HttpRequest.DELETE("/chave-pix/remove", restRequest()), ChaveRemocaoRequest::class.java)
        }
        with(responseException) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertEquals("Os dados recebidos pela requisição estão inválidos.", message)
        }
    }

    @Test
    @Order(4)
    fun `nao deve remover uma chave PIX caso ocorrer um erro interno no servidor`() {
        BDDMockito.given(blockingStub.removerChavePix(grpcRequest())).willThrow(Status.INTERNAL.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            val response = httpClient.toBlocking()
                .exchange(HttpRequest.DELETE("/chave-pix/remove", restRequest()), ChaveRemocaoRequest::class.java)
        }

        with(responseException) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code, status.code)
            assertEquals("Ocorreu um erro interno no servidor e não foi possível prosseguir com a sua solicitação.", message)
        }
    }

    /**
     * Retornando os objetos de requisição/resposta preenchidos
     **/
    fun restRequest() = ChaveRemocaoRequest(pixId = PIX_ID, clientId = CLIENT_ID)
    fun grpcRequest() = KeyPixRequestRemove.newBuilder().setClientId(CLIENT_ID).setPixId(PIX_ID).build()
    fun grpcResponse() = KeyPixResponseRemove.newBuilder().setPixId(PIX_ID).build()

    /**
     * Mockando a factory gRPC
     **/
    @Factory
    @Replaces(factory = FactoryClientGrpc::class)
    internal class MockFactoryGrpc {
        @Singleton
        fun mockFactory() =
            Mockito.mock(KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub::class.java)
    }

}