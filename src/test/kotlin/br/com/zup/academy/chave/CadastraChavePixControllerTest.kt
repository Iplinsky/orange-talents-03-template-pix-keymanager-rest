package br.com.zup.academy.chave

import br.com.zup.academy.*
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.mockito.BDDMockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@TestMethodOrder(OrderAnnotation::class)
@MicronautTest
internal class CadastraChavePixControllerTest {

    @field:Inject
    lateinit var blockingStub: KeyManagerCadastrarGrpcServiceGrpc.KeyManagerCadastrarGrpcServiceBlockingStub

    @field:Inject
    @field:Client(value = "/")
    lateinit var httpClient: HttpClient

    companion object {
        val CLIENT_ID = "c56dfef4-7901-44fb-84e2-a2cefb157890"
        val PIX_ID = UUID.randomUUID().toString()
    }

    @AfterEach
    internal fun tearDown() {
        reset(blockingStub)
    }

    @Test
    @Order(1)
    fun `deve cadastrar uma chave PIX com sucesso`() {
        val grpcRequest =
            KeyPixRequestCadastro.newBuilder().setClientId(CLIENT_ID).setTipoConta(TipoConta.CONTA_CORRENTE)
                .setTipoChavePix(TipoChave.EMAIL).setValorChave("email@email.com").build()

        val restRequest = ChaveCadastroRequest(
            clientId = CLIENT_ID,
            tipoDaChave = TipoDaChave.EMAIL,
            valorDaChave = "email@email.com",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE
        )

        val grpcResponse = KeyPixResponseCadastro
            .newBuilder()
            .setClientId(CLIENT_ID)
            .setPixId(PIX_ID)
            .build()

        BDDMockito.given(blockingStub.cadastrarChavePix(grpcRequest)).willReturn(grpcResponse)

        val postRequest = HttpRequest.POST("/chave-pix", restRequest)
        val response = httpClient.toBlocking().exchange(postRequest, ChaveCadastroRequest::class.java)

        with(response) {
            assertEquals(HttpStatus.CREATED.code, status.code)
            assertTrue(headers.contains("Location"))
            assertTrue(header("Location").contains(PIX_ID))
        }

    }


    @Test
    @Order(2)
    fun `nao deve cadastrar uma chave PIX se a mesma ja existir`() {
        val grpcRequest =
            KeyPixRequestCadastro.newBuilder().setClientId(CLIENT_ID).setTipoConta(TipoConta.CONTA_CORRENTE)
                .setTipoChavePix(TipoChave.EMAIL).setValorChave("email@email.com").build()

        val restRequest = ChaveCadastroRequest(
            clientId = CLIENT_ID,
            tipoDaChave = TipoDaChave.EMAIL,
            valorDaChave = "email@email.com",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE
        )

        BDDMockito.given(blockingStub.cadastrarChavePix(grpcRequest))
            .willThrow(Status.ALREADY_EXISTS.asRuntimeException())

        val postRequest = HttpRequest.POST("/chave-pix", restRequest)

        val responseException = assertThrows<HttpClientResponseException> {
            httpClient.toBlocking().exchange(postRequest, ChaveCadastroRequest::class.java)
        }

        with(responseException.status) {
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.code, code)
        }


    }

    @Test
    @Order(3)
    fun `nao deve cadastrar uma chave PIX se os dados de entrada estiverem invalidos`() {
        val grpcRequest =
            KeyPixRequestCadastro.newBuilder().setClientId(CLIENT_ID).setTipoConta(TipoConta.CONTA_CORRENTE)
                .setTipoChavePix(TipoChave.CPF).setValorChave("email@email.com").build()

        val restRequest = ChaveCadastroRequest(
            clientId = CLIENT_ID,
            tipoDaChave = TipoDaChave.CPF,
            valorDaChave = "email@email.com",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE
        )

        BDDMockito.given(blockingStub.cadastrarChavePix(grpcRequest))
            .willThrow(Status.INVALID_ARGUMENT.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            httpClient.toBlocking()
                .exchange(HttpRequest.POST("/chave-pix", restRequest), ChaveCadastroRequest::class.java)
        }

        with(responseException) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertEquals("Os dados recebidos pela requisição estão inválidos.", message)
        }

    }

    @Test
    @Order(4)
    fun `nao deve cadastrar uma chave PIX caso o cliente nao seja localizado`() {

        val RANDOM_CLIENT_ID = UUID.randomUUID().toString()

        val grpcRequest =
            KeyPixRequestCadastro.newBuilder().setClientId(RANDOM_CLIENT_ID).setTipoConta(TipoConta.CONTA_CORRENTE)
                .setTipoChavePix(TipoChave.CPF).setValorChave("email@email.com").build()

        val restRequest = ChaveCadastroRequest(
            clientId = RANDOM_CLIENT_ID,
            tipoDaChave = TipoDaChave.CPF,
            valorDaChave = "email@email.com",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE
        )

        BDDMockito.given(blockingStub.cadastrarChavePix(grpcRequest))
            .willThrow(Status.NOT_FOUND.asRuntimeException())


        val responseException = assertThrows<HttpClientResponseException> {
            httpClient.toBlocking().exchange(HttpRequest.POST("chave-pix", restRequest), ChaveCadastroRequest::class.java)
        }

        with(responseException) {
            assertEquals(HttpStatus.NOT_FOUND.code, status.code)
            assertEquals("Not Found", message)
        }

    }

    @Test
    @Order(4)
    fun `nao deve cadastrar uma chave PIX caso houver algum erro no servidor`() {
        val grpcRequest =
            KeyPixRequestCadastro.newBuilder().setClientId(CLIENT_ID).setTipoConta(TipoConta.CONTA_CORRENTE)
                .setTipoChavePix(TipoChave.CPF).setValorChave("email@email.com").build()

        val restRequest = ChaveCadastroRequest(
            clientId = CLIENT_ID,
            tipoDaChave = TipoDaChave.CPF,
            valorDaChave = "email@email.com",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE
        )

        BDDMockito.given(blockingStub.cadastrarChavePix(grpcRequest)).willThrow(Status.INTERNAL.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            httpClient.toBlocking()
                .exchange(HttpRequest.POST("/chave-pix", restRequest), ChaveCadastroRequest::class.java)
        }

        with(responseException) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code, status.code)
            assertEquals("Ocorreu um erro interno no servidor e não foi possível prosseguir com a sua solicitação.", message)

        }
    }


    /**
     * Mockando gRpc Stub
     **/
    @Factory
    @Replaces(factory = FactoryClientGrpc::class)
    internal

    class MockFactoryGrpc {
        @Singleton
        fun mockBlockingStub() =
            mock(KeyManagerCadastrarGrpcServiceGrpc.KeyManagerCadastrarGrpcServiceBlockingStub::class.java)
    }
}