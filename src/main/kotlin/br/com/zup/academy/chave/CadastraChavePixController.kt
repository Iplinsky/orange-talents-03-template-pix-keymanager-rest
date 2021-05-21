package br.com.zup.academy.chave

import br.com.zup.academy.KeyManagerCadastrarGrpcServiceGrpc
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.validation.Validated
import java.net.URI
import javax.validation.Valid

@Validated
@Controller("/chave-pix")
class CadastraChavePixController(private val clientStub: KeyManagerCadastrarGrpcServiceGrpc.KeyManagerCadastrarGrpcServiceBlockingStub) {

    @Post
    fun cadastrarChavePix(@Valid @Body request: ChavePixRequestDto): HttpResponse<Any> {
        return HttpResponse.created(URI("/chave-pix/${clientStub.cadastrarChavePix(request.toGrpcModel()).clientId}"))
    }

}