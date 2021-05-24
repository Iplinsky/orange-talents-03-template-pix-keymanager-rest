package br.com.zup.academy.chave

import br.com.zup.academy.*
import br.com.zup.academy.factory.FactoryClientGrpc
import com.google.protobuf.Timestamp
import io.grpc.Status
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@TestMethodOrder(OrderAnnotation::class)
@MicronautTest
internal class ListaChavesPixControllerTest {

    @field:Inject
    lateinit var blockingStub: KeyManagerConsultarTodasGrpcServiceGrpc.KeyManagerConsultarTodasGrpcServiceBlockingStub

    @field:Inject
    @field:Client(value = "/")
    lateinit var httpClient: HttpClient

    companion object {
        val CLIENT_ID = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    }

    @AfterEach
    internal fun tearDown() {
        Mockito.reset(blockingStub)
    }

    @Test
    @Order(1)
    fun `deve listar todas as chaves cadastradas do cliente com sucesso`() {
        BDDMockito.given(
            blockingStub.consultarTodasChavesPix(
                KeyPixRequestConsultaTodas.newBuilder().setClientId(CLIENT_ID).build()
            )
        ).willReturn(keyPixResponseGrpc())

        val response = httpClient.toBlocking()
            .exchange("/chave-pix/listar/$CLIENT_ID", mutableListOf<ChaveListaResponse>()::class.java)

        with(response) {
            assertNotNull(response.body())
            assertEquals(HttpStatus.OK.code, status.code)
            assertEquals(3, body().size)
        }
    }

    @Test
    @Order(2)
    fun `nao deve listar nenhuma chave caso os dados de entrada estiverem invalidos`() {
        BDDMockito.given(
            blockingStub.consultarTodasChavesPix(
                KeyPixRequestConsultaTodas.newBuilder().setClientId("CLIENT_ID").build()
            )
        ).willThrow(Status.INVALID_ARGUMENT.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            httpClient.toBlocking()
                .exchange("/chave-pix/listar/CLIENT_ID", mutableListOf<ChaveListaResponse>()::class.java)
        }

        with(responseException) {
            assertNull(response.body())
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
        }
    }

    @Test
    @Order(3)
    fun `nao deve listar nenhuma chave para um cliente que nao possua chave`() {
        val randomClient = UUID.randomUUID().toString()

        BDDMockito.given(
            blockingStub.consultarTodasChavesPix(
                KeyPixRequestConsultaTodas.newBuilder().setClientId(randomClient).build()
            )
        ).willThrow(Status.NOT_FOUND.asRuntimeException())

        val responseException = assertThrows<HttpClientResponseException> {
            httpClient.toBlocking()
                .exchange("/chave-pix/listar/$randomClient", mutableListOf<ChaveListaResponse>()::class.java)
        }

        with(responseException) {
            assertNull(response.body())
            assertEquals(HttpStatus.NOT_FOUND.code, status.code)
        }
    }

    /**
     * Preenchendo o objeto de retorno gRPC - lista
     **/

    fun keyPixResponseGrpc(): KeyPixResponseConsultaTodas {
        val cpfKeyPix = KeyPixResponseConsultaTodas
            .ChavePix
            .newBuilder()
            .setPixId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.CPF)
            .setValorChave("28073711087")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .setCriadaEm(
                Timestamp
                    .newBuilder()
                    .setNanos(123456789)
                    .setSeconds(1556322834)
                    .build()
            )
            .build()

        val emailKeyPix = KeyPixResponseConsultaTodas
            .ChavePix
            .newBuilder()
            .setPixId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setValorChave("email@gmail.com")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .setCriadaEm(
                Timestamp
                    .newBuilder()
                    .setNanos(223456789)
                    .setSeconds(2556322834)
                    .build()
            )
            .build()

        val telefoneKeyPix = KeyPixResponseConsultaTodas
            .ChavePix
            .newBuilder()
            .setPixId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.TELEFONE)
            .setValorChave("+5534998987878")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .setCriadaEm(
                Timestamp
                    .newBuilder()
                    .setNanos(223456789)
                    .setSeconds(2556322834)
                    .build()
            )
            .build()

        return KeyPixResponseConsultaTodas.newBuilder()
            .addAllChavesPix(listOf(cpfKeyPix, emailKeyPix, telefoneKeyPix)).build()
    }

    /**
     * Mockando gRpc Stub
     **/

    @Factory
    @Replaces(factory = FactoryClientGrpc::class)
    internal class MockFactoryGrpc {
        @Singleton
        fun mockFactory() =
            mock(KeyManagerConsultarTodasGrpcServiceGrpc.KeyManagerConsultarTodasGrpcServiceBlockingStub::class.java)
    }

}