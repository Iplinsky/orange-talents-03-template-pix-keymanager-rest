package br.com.zup.academy.chave

import br.com.zup.academy.KeyPixResponseConsulta
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class ChaveDetalheResponse(chavePixRetorno: KeyPixResponseConsulta) {
    val clientId = chavePixRetorno.clienteId
    val pixId = chavePixRetorno.pixId
    val tipo = chavePixRetorno.chave.tipo
    val chave = chavePixRetorno.chave.chave
    val conta = mapOf(
        Pair("tipo", chavePixRetorno.chave.conta.tipo.name),
        Pair("instituicao", chavePixRetorno.chave.conta.instituicao),
        Pair("nomeDoTitular", chavePixRetorno.chave.conta.nomeDoTitular),
        Pair("cpfDoTitular", chavePixRetorno.chave.conta.cpfDoTitular),
        Pair("agencia", chavePixRetorno.chave.conta.agencia),
        Pair("numeroDaConta", chavePixRetorno.chave.conta.numeroDaConta)
    )
    val criadaEm = chavePixRetorno.chave.criadaEm.let {
        LocalDateTime.ofInstant(Instant.ofEpochSecond(it.seconds, it.nanos.toLong()), ZoneOffset.UTC)
    }
}