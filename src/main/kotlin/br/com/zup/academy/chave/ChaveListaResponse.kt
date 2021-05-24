package br.com.zup.academy.chave

import br.com.zup.academy.KeyPixResponseConsultaTodas.ChavePix
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class ChaveListaResponse(keyGrpc: ChavePix) {
    val pixId = keyGrpc.pixId
    val tipoDaChave = keyGrpc.tipoChave
    val valorDaChave = keyGrpc.valorChave
    val tipoDeConta = keyGrpc.tipoConta
    val criadaEm = keyGrpc.criadaEm.let {
        LocalDateTime.ofInstant(Instant.ofEpochSecond(it.seconds, it.nanos.toLong()), ZoneOffset.UTC)
    }
}