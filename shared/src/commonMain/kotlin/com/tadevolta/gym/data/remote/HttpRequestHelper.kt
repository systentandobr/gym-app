package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.repositories.AuthRepository
import com.tadevolta.gym.utils.auth.UnauthenticatedException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Helper para executar requisições HTTP com retry automático em caso de erro 401.
 * Tenta refresh token primeiro, depois reautenticação com cache, e faz retry da requisição original.
 * 
 * @param client HttpClient para fazer as requisições
 * @param authRepository AuthRepository para reautenticação (opcional)
 * @param tokenProvider Função que retorna o token atual (será atualizada após reautenticação)
 * @param maxRetries Número máximo de tentativas (padrão: 3)
 * @param requestBuilder Builder para configurar a requisição HTTP
 * @param responseHandler Handler para processar a resposta HTTP
 */
suspend inline fun <T> executeWithRetry(
    client: HttpClient,
    authRepository: AuthRepository?,
    crossinline tokenProvider: () -> String?,
    maxRetries: Int = 3,
    crossinline requestBuilder: HttpRequestBuilder.() -> Unit,
    crossinline responseHandler: (HttpResponse) -> T
): T {
    var lastException: Exception? = null
    var retryCount = 0
    
    while (retryCount <= maxRetries) {
        try {
            val response = client.request {
                requestBuilder()
                // Atualizar token se necessário (o requestBuilder já deve ter adicionado, mas atualizamos para garantir)
                headers {
                    // Remover Authorization antigo se existir
                    remove("Authorization")
                    // Adicionar token atualizado
                    tokenProvider()?.let { token ->
                        append("Authorization", "Bearer $token")
                    }
                }
            }
            
            // Se não for 401, processar resposta normalmente
            if (response.status != HttpStatusCode.Unauthorized) {
                return responseHandler(response)
            }
            
            // Se for 401 e ainda tiver tentativas, tentar reautenticação
            if (retryCount < maxRetries && authRepository != null) {
                retryCount++
                
                // Tentar refresh token primeiro
                val refreshSuccess = authRepository.refreshTokenIfNeeded()
                
                if (refreshSuccess) {
                    // Refresh token funcionou, fazer retry da requisição com novo token
                    continue
                }
                
                // Refresh token falhou, tentar reautenticação com cache
                val reauthResult = authRepository.reauthenticateWithCache()
                
                when (reauthResult) {
                    is com.tadevolta.gym.data.models.Result.Success -> {
                        // Reautenticação funcionou, fazer retry da requisição com novo token
                        continue
                    }
                    is com.tadevolta.gym.data.models.Result.Error -> {
                        // Reautenticação falhou, forçar logout e lançar exceção
                        try {
                            authRepository.forceLogout()
                        } catch (e: Exception) {
                            // Ignorar erros ao fazer logout
                        }
                        throw UnauthenticatedException(
                            "Falha ao reautenticar. Por favor, faça login novamente.",
                            reauthResult.exception
                        )
                    }
                    else -> {
                        // Erro desconhecido
                        try {
                            authRepository.forceLogout()
                        } catch (e: Exception) {
                            // Ignorar erros ao fazer logout
                        }
                        throw UnauthenticatedException(
                            "Falha ao reautenticar. Erro desconhecido."
                        )
                    }
                }
            } else {
                // Excedeu número máximo de tentativas ou authRepository não disponível
                if (authRepository != null) {
                    try {
                        authRepository.forceLogout()
                    } catch (e: Exception) {
                        // Ignorar erros ao fazer logout
                    }
                }
                throw UnauthenticatedException(
                    "Falha ao reautenticar após $maxRetries tentativas. Por favor, faça login novamente."
                )
            }
        } catch (e: UnauthenticatedException) {
            // Reautenticação falhou, propagar exceção
            throw e
        } catch (e: Exception) {
            lastException = e
            // Se não for 401, propagar erro imediatamente
            if (e !is io.ktor.client.network.sockets.SocketTimeoutException) {
                throw e
            }
            // Timeout pode tentar novamente se ainda tiver tentativas
            if (retryCount >= maxRetries) {
                throw e
            }
            retryCount++
        }
    }
    
    // Se chegou aqui, todas as tentativas falharam
    throw lastException ?: UnauthenticatedException("Falha após $maxRetries tentativas")
}
