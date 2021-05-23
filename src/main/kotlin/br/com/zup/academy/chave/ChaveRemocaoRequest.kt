package br.com.zup.academy.chave

import br.com.zup.academy.KeyPixRequestRemove
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class ChaveRemocaoRequest(
    @field:NotBlank
    val pixId: String,

    @field:NotBlank
    val clientId: String
) {
    fun toGrpcModel(): KeyPixRequestRemove {
        return KeyPixRequestRemove
            .newBuilder()
            .setPixId(pixId)
            .setClientId(clientId)
            .build()
    }
}
