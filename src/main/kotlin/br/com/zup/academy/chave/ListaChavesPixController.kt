package br.com.zup.academy.chave

import br.com.zup.academy.KeyManagerConsultarTodasGrpcServiceGrpc.KeyManagerConsultarTodasGrpcServiceBlockingStub
import br.com.zup.academy.KeyPixRequestConsultaTodas
import br.com.zup.academy.KeyPixResponseConsultaTodas
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import java.util.*

@Controller("/chave-pix")
class ListaChavesPixController(private val clientStub: KeyManagerConsultarTodasGrpcServiceBlockingStub) {

    @Get("/listar/{clientId}")
    fun listarChavesPix(@PathVariable clientId: UUID): HttpResponse<List<ChaveListaResponse>> {

        val response: KeyPixResponseConsultaTodas =
            clientStub.consultarTodasChavesPix(KeyPixRequestConsultaTodas.newBuilder().setClientId(clientId.toString()).build())

        return HttpResponse.ok(response.chavesPixList.map { ChaveListaResponse(it) })
    }

}