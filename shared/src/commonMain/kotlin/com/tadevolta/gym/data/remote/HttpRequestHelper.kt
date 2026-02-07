package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.manager.TokenManager
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
 * @param authRepository AuthRepository para reautenticação
 * @param tokenManager TokenManager para controle de concorrência e prevenção de expiração
 * @param tokenProvider Função que retorna o token atual
 * @param maxRetries Número máximo de tentativas (padrão: 3)
 * @param requestBuilder Builder para configurar a requisição HTTP
 * @param responseHandler Handler para processar a resposta HTTP
 */
suspend inline fun <T> executeWithRetry(
    client: HttpClient,
    authRepository: AuthRepository? = null,
    tokenManager: TokenManager? = null,
    crossinline tokenProvider: suspend () -> String?,
    maxRetries: Int = 3,
    crossinline requestBuilder: HttpRequestBuilder.() -> Unit,
    crossinline responseHandler: (HttpResponse) -> T
): T {
    var lastException: Exception? = null
    var retryCount = 0
    
    while (retryCount <= maxRetries) {
        try {
            // Obter token antes da requisição
            val token = tokenProvider()
            
            val response = client.request {
                requestBuilder()
                // Atualizar token
                headers {
                    remove("Authorization")
                    token?.let {
                        append("Authorization", "Bearer $it")
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
                
                // Aguardar se houver outro refresh em andamento
                tokenManager?.waitForRefresh()
                
                // Tentar refresh token via AuthRepository
                val refreshSuccess = authRepository.refreshTokenIfNeeded()
                
                // Marcar refresh como concluído no TokenManager
                tokenManager?.markRefreshCompleted(refreshSuccess)
                
                if (refreshSuccess) {
                    // Refresh funcionou, fazer retry
                    continue
                }
                
                // Se refresh falhou, tentar reautenticação com cache
                val reauthResult = authRepository.reauthenticateWithCache()
                
                when (reauthResult) {
                    is com.tadevolta.gym.data.models.Result.Success -> {
                        // Reautenticação funcionou, fazer retry
                        continue
                    }
                    is com.tadevolta.gym.data.models.Result.Error -> {
                        // Reautenticação falhou, forçar logout
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
                // Excedeu número máximo de tentativas
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
            // Se não for timeout, propagar erro imediatamente
            if (e !is io.ktor.client.network.sockets.SocketTimeoutException) {
                throw e
            }
            // Timeout pode tentar novamente
            if (retryCount >= maxRetries) {
                throw e
            }
            retryCount++
        }
    }
    
    // Se chegou aqui, todas as tentativas falharam
    throw lastException ?: UnauthenticatedException("Falha após $maxRetries tentativas")
}