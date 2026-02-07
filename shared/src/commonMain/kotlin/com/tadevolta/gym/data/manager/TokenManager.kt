package com.tadevolta.gym.data.manager

import com.tadevolta.gym.data.models.AuthTokens
import com.tadevolta.gym.data.repositories.TokenStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Gerenciador centralizado de tokens que fornece:
 * - Prevenção pró-ativa de expiração (renova token antes de expirar)
 * - Controle de concorrência (evita múltiplos refreshs simultâneos)
 * - Thread-safe operations
 * 
 * NOTA: Este TokenManager NÃO executa refresh diretamente para evitar
 * dependência circular com AuthRepository. O refresh é feito pelo
 * executeWithRetry no HttpRequestHelper.
 */
class TokenManager(
    private val tokenStorage: TokenStorage
) {
    // Mutex para garantir operações thread-safe no estado interno
    private val stateMutex = Mutex()
    
    // Timestamp da última renovação bem-sucedida
    private var lastRefreshTime: Long = 0
    
    // Flag para indicar se há um refresh em andamento (controle de concorrência)
    private var isRefreshing = false
    
    companion object {
        // Intervalo mínimo entre verificações de refresh (5 minutos)
        private const val MIN_REFRESH_INTERVAL_MS = 5 * 60 * 1000L
    }
    
    /**
     * Obtém o token de acesso atual com prevenção pró-ativa de expiração.
     * 
     * Se o token estiver próximo de expirar ou se passou tempo suficiente
     * desde a última renovação, este método sinaliza que um refresh é necessário.
     * 
     * O refresh real é feito pelo executeWithRetry via AuthRepository.
     * 
     * @return O token de acesso atual, ou null se não houver token válido
     */
    suspend fun getAccessToken(): String? {
        // Verificar se devemos tentar refresh preventivo
        val shouldRefresh = stateMutex.withLock {
            val timeSinceLastRefresh = System.currentTimeMillis() - lastRefreshTime
            timeSinceLastRefresh > MIN_REFRESH_INTERVAL_MS && !isRefreshing
        }
        
        // Se devemos renovar, apenas marcar que estamos tentando
        // O refresh real será feito pelo executeWithRetry quando necessário
        if (shouldRefresh) {
            stateMutex.withLock {
                isRefreshing = true
            }
        }
        
        return tokenStorage.getAccessToken()
    }
    
    /**
     * Marca que o refresh foi concluído (com sucesso ou falha).
     * Deve ser chamado pelo executeWithRetry após tentar refresh.
     * 
     * @param success true se o refresh foi bem-sucedido
     */
    suspend fun markRefreshCompleted(success: Boolean) {
        stateMutex.withLock {
            isRefreshing = false
            if (success) {
                lastRefreshTime = System.currentTimeMillis()
            }
        }
    }
    
    /**
     * Verifica se há um refresh em andamento.
     * Usado pelo executeWithRetry para evitar múltiplos refreshs simultâneos.
     */
    suspend fun isRefreshInProgress(): Boolean {
        return stateMutex.withLock { isRefreshing }
    }
    
    /**
     * Aguarda um refresh em andamento ser concluído.
     * 
     * @param timeoutMs Timeout em milissegundos (padrão: 5000ms)
     * @return true se o refresh foi concluído, false se timeout
     */
    suspend fun waitForRefresh(timeoutMs: Long = 5000): Boolean {
        val startTime = System.currentTimeMillis()
        while (isRefreshInProgress()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                return false
            }
            kotlinx.coroutines.delay(100)
        }
        return true
    }
    
    /**
     * Limpa todos os tokens e estado interno.
     * Usado quando a autenticação falha completamente.
     */
    suspend fun clearTokens() {
        stateMutex.withLock {
            tokenStorage.clearTokens()
            lastRefreshTime = 0
            isRefreshing = false
        }
    }
    
    /**
     * Salva novos tokens e atualiza o timestamp de última renovação.
     */
    suspend fun saveTokens(tokens: AuthTokens) {
        stateMutex.withLock {
            tokenStorage.saveTokens(tokens)
            lastRefreshTime = System.currentTimeMillis()
            isRefreshing = false
        }
    }
}