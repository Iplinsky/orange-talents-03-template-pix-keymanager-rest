package br.com.zup.academy.chave

import br.com.zup.academy.KeyManagerRemoveGrpcServiceGrpc
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Validated
@Controller("/chave-pix/remove")
class RemoveChavePixController(private val clientStub: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Delete
    fun removerChavePix(@Valid @Body request: ChaveRemocaoRequest): HttpResponse<Any> {
        LOGGER.info("Iniciando o processo de remoção da chave ${request.pixId}.")
        clientStub.removerChavePix(request.toGrpcModel())

        return HttpResponse.noContent()
    }
}