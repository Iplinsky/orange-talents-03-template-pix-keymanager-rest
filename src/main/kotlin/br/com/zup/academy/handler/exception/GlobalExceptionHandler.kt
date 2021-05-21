package br.com.zup.academy.handler.exception

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.server.exceptions.ExceptionHandler
import javax.inject.Singleton

/**
 * Classe responsável por interceptar os erros recebidos do servidor gRPC (StatusRuntimeException)
 * Aqui é feita a tradução dos erros grpc(io.grpc) para o tipo compatível ao protocolo Http * *
 **/
@Singleton
class GlobalExceptionHandler : ExceptionHandler<StatusRuntimeException, HttpResponse<Any>> {
    override fun handle(request: HttpRequest<*>, exception: StatusRuntimeException): HttpResponse<Any> {

        val description = exception.status.description

        /**
          Destructuring -> Desestruturação do Pair originado pelo fluxo when.
         **/
        val (status, message) = when (exception.status.code) {
            Status.NOT_FOUND.code -> Pair(HttpStatus.NOT_FOUND, description ?: "")
            Status.ALREADY_EXISTS.code -> Pair(HttpStatus.UNPROCESSABLE_ENTITY, description ?: "")
            Status.INVALID_ARGUMENT.code -> Pair(HttpStatus.BAD_REQUEST, description ?: "")
            else -> Pair(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor e não foi possível prosseguir com a sua solicitação."
            )
        }

        return HttpResponse.status<JsonError>(status).body(JsonError(message))
    }
}