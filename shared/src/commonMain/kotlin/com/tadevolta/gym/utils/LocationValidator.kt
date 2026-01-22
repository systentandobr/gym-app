package com.tadevolta.gym.utils

import com.tadevolta.gym.data.models.Location
import kotlin.math.*

object LocationValidator {
    const val MAX_CHECKIN_DISTANCE_METERS = 200.0
    
    /**
     * Calcula a distância entre duas coordenadas usando a fórmula de Haversine
     * @return Distância em metros
     */
    fun calculateDistance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val earthRadius = 6371000.0 // Raio da Terra em metros
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Verifica se a localização do usuário está dentro do alcance permitido da unidade
     * @param userLocation Localização atual do usuário
     * @param unitLocation Localização da unidade
     * @param maxDistanceMeters Distância máxima permitida em metros (padrão: MAX_CHECKIN_DISTANCE_METERS)
     * @return true se estiver dentro do alcance, false caso contrário
     */
    fun isWithinRange(
        userLocation: Location,
        unitLocation: Location,
        maxDistanceMeters: Double = MAX_CHECKIN_DISTANCE_METERS
    ): Boolean {
        val distance = calculateDistance(
            userLocation.lat,
            userLocation.lng,
            unitLocation.lat,
            unitLocation.lng
        )
        return distance <= maxDistanceMeters
    }
    
    /**
     * Verifica se a localização do usuário está dentro do alcance permitido da unidade
     * Versão com coordenadas diretas
     */
    fun isWithinRange(
        userLat: Double,
        userLng: Double,
        unitLat: Double,
        unitLng: Double,
        maxDistanceMeters: Double = MAX_CHECKIN_DISTANCE_METERS
    ): Boolean {
        val distance = calculateDistance(userLat, userLng, unitLat, unitLng)
        return distance <= maxDistanceMeters
    }
}
