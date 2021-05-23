package br.com.zup.academy.chave

import br.com.zup.academy.KeyPixRequestCadastro
import io.micronaut.core.annotation.Introspected
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
class ChaveCadastroRequest(
    @field:NotBlank
    val clientId: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipoDaChave: TipoDaChave?,

    @field:NotBlank
    @field:Size(max = 77)
    val valorDaChave: String?,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipoDeConta: TipoDeConta,
) {
    fun toGrpcModel(): KeyPixRequestCadastro {
        return KeyPixRequestCadastro
            .newBuilder()
            .setClientId(clientId)
            .setTipoChavePix(enumValueOf(tipoDaChave!!.name))
            .setValorChave(valorDaChave)
            .setTipoConta(enumValueOf(tipoDeConta.name))
            .build()
    }

}
