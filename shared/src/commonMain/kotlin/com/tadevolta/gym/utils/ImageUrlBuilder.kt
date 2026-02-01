package com.tadevolta.gym.utils

import com.tadevolta.gym.utils.config.EnvironmentConfig

/**
 * Helper para construir URLs completas de imagens a partir de URLs relativas retornadas pelo backend.
 * 
 * O backend retorna URLs relativas como `/uploads/exercises/{exerciseId}/{filename}`,
 * que precisam ser convertidas para URLs completas usando `API_BASE_URL`.
 */
object ImageUrlBuilder {
    /**
     * Constrói uma URL completa a partir de uma URL relativa.
     * 
     * @param relativeUrl URL relativa (ex: `/uploads/exercises/123/image.jpg`) ou URL absoluta
     * @return URL completa se relativeUrl não for null, null caso contrário
     */
    fun buildImageUrl(relativeUrl: String?): String? {
        if (relativeUrl == null) return null
        
        // Se já é URL absoluta, retornar como está
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl
        }
        
        // Construir URL completa com API_BASE_URL
        val baseUrl = EnvironmentConfig.API_BASE_URL.trimEnd('/')
        val path = relativeUrl.trimStart('/')
        return "$baseUrl/$path"
    }
    
    /**
     * Constrói uma lista de URLs completas a partir de uma lista de URLs relativas.
     * 
     * @param relativeUrls Lista de URLs relativas
     * @return Lista de URLs completas (apenas URLs válidas são incluídas)
     */
    fun buildImageUrls(relativeUrls: List<String>?): List<String> {
        return relativeUrls?.mapNotNull { buildImageUrl(it) } ?: emptyList()
    }
}
