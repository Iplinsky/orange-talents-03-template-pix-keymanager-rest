package br.com.zup.academy.chave

import br.com.zup.academy.*
import br.com.zup.academy.KeyPixRequestConsulta.SearchByPixAndClientId
import br.com.zup.academy.factory.FactoryClientGrpc
import com.google.protobuf.Timestamp
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@TestMethodOrder(OrderAnnotation::class)
@MicronautTest
internal class DetalhamentoChavePixControllerTest {

    @field:Inject
    lateinit var blockingStub: KeymanagerConsultarGrpcServiceGrpc.KeymanagerConsultarGrpcServiceBlockingStub

    @field:Inject
    @field:Client(value = "/")
    lateinit var httpClient: HttpClient

    @AfterEach
    internal fun tearDown() {
        Mockito.reset(blockingStub)
    }

    companion object {
        val PIX_ID = UUID.randomUUID().toString()
        val CLIENT_ID = UUID.randomUUID().toString()
    }

    @Test
    @Order(1)
    fun `deve consultar uma chave pix existente com sucesso`() {

        BDDMockito.given(blockingStub.consultarChavePix(pixRequestConsulta())).willReturn(pixResponseConsulta())

        val response = httpClient.toBlocking()
            .exchange<String, ChaveDetalheResponse>(HttpRequest.GET(URI("/chave-pix/consulta/$PIX_ID/client/$CLIENT_ID")))

        with(response) {
            assertNotNull(response)
            assertEquals(HttpStatus.OK.code, status.code)
        }
    }

    @Test
    @Order(2)
    fun `nao deve recuperar uma chave pix inexistente`() {

        BDDMockito.given(blockingStub.consultarChavePix(pixRequestConsulta()))
            .willThrow(Status.NOT_FOUND.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            httpClient.toBlocking()
                .exchange<String, ChaveDetalheResponse>(HttpRequest.GET(URI("/chave-pix/consulta/$PIX_ID/client/$CLIENT_ID")))
        }

        with(responseException) {
            assertEquals(HttpStatus.NOT_FOUND.code, status.code)
        }
    }

    @Test
    @Order(3)
    fun `nao deve recuperar uma chave pix caso os dados da requisicao estiverem invalidos`() {

        BDDMockito.given(blockingStub.consultarChavePix(pixRequestConsulta()))
            .willThrow(Status.INVALID_ARGUMENT.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            httpClient.toBlocking()
                .exchange<String, ChaveDetalheResponse>(HttpRequest.GET(URI("/chave-pix/consulta/INVALID_PIX/client/INVALID_CLIENT")))
        }

        with(responseException) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
        }
    }

    @Test
    @Order(4)
    fun `nao deve recuperar uma chave pix caso houver um erro interno no servidor`() {

        BDDMockito.given(blockingStub.consultarChavePix(pixRequestConsulta()))
            .willThrow(Status.INTERNAL.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            httpClient.toBlocking()
                .exchange<String, ChaveDetalheResponse>(HttpRequest.GET(URI("/chave-pix/consulta/$PIX_ID/client/$CLIENT_ID")))
        }

        with(responseException) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code, status.code)
        }
    }

    /**
     * Montando os objetos de requisição e resposta para o cenário de busca detalhada
     **/

    fun pixRequestConsulta() =
        KeyPixRequestConsulta.newBuilder().setPixId(
            SearchByPixAndClientId
                .newBuilder()
                .setClientId(CLIENT_ID)
                .setPixId(PIX_ID)
                .build()
        ).build()

    fun pixResponseConsulta() = KeyPixResponseConsulta.newBuilder()
        .setClienteId(CLIENT_ID)
        .setPixId(PIX_ID)
        .setChave(
            KeyPixResponseConsulta.ChavePix
                .newBuilder()
                .setTipo(TipoChave.EMAIL)
                .setChave("email_teste@gmail.com")
                .setConta(
                    KeyPixResponseConsulta.ChavePix.ContaInfo.newBuilder()
                        .setTipo(TipoConta.CONTA_CORRENTE)
                        .setInstituicao("Instituição de teste")
                        .setNomeDoTitular("Nome teste")
                        .setCpfDoTitular("13579531579")
                        .setAgencia("51123")
                        .setNumeroDaConta("0000023")
                        .build()
                )
                .setCriadaEm(LocalDateTime.now().let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
        )
        .build()

    /**
     * Mockando gRpc Stub
     **/
    @Factory
    @Replaces(factory = FactoryClientGrpc::class)
    class MockFactoryGrpc {
        @Singleton
        fun mockFactory() =
            mock(KeymanagerConsultarGrpcServiceGrpc.KeymanagerConsultarGrpcServiceBlockingStub::class.java)
    }
}