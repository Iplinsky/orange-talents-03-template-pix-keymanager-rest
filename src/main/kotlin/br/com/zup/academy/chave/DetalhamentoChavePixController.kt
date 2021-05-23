package br.com.zup.academy.chave

import br.com.zup.academy.KeyPixRequestConsulta
import br.com.zup.academy.KeymanagerConsultarGrpcServiceGrpc
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import java.util.*

@Controller("/chave-pix")
class DetalhamentoChavePixController(private val clientStub: KeymanagerConsultarGrpcServiceGrpc.KeymanagerConsultarGrpcServiceBlockingStub) {

    @Get("/consulta/{pixId}/client/{clientId}")
    fun consultarChaveDetalhe(
        @PathVariable pixId: UUID,
        @PathVariable clientId: UUID
    ): HttpResponse<ChaveDetalheResponse> {

        val request = KeyPixRequestConsulta
            .newBuilder()
            .setPixId(
                KeyPixRequestConsulta.SearchByPixAndClientId
                    .newBuilder()
                    .setPixId(pixId.toString())
                    .setClientId(clientId.toString())
                    .build()
            )
            .build()

        return HttpResponse.ok(ChaveDetalheResponse(clientStub.consultarChavePix(request)))
    }

}