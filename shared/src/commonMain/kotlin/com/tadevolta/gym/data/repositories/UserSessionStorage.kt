package com.tadevolta.gym.data.repositories

import com.tadevolta.gym.data.models.CachedCredentials
import com.tadevolta.gym.data.models.State
import com.tadevolta.gym.data.models.City

interface UserSessionStorage {
    suspend fun saveSelectedUnit(unitId: String, unitName: String)
    suspend fun getSelectedUnit(): Pair<String?, String?>
    suspend fun clearSelectedUnit()
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun isOnboardingCompleted(): Boolean
    
    // Métodos para cache de credenciais
    suspend fun saveCredentials(username: String, email: String, password: String)
    suspend fun getCachedCredentials(): CachedCredentials?
    suspend fun clearCachedCredentials()
    suspend fun isCredentialsCacheValid(): Boolean
    
    // Métodos para cache de estados e cidades
    suspend fun saveStatesAndCities(states: List<State>, cities: List<City>)
    suspend fun getStates(): List<State>
    suspend fun getCitiesByState(stateId: String): List<City>
}

expect class SecureUserSessionStorage : UserSessionStorage {
    override suspend fun saveSelectedUnit(unitId: String, unitName: String)
    override suspend fun getSelectedUnit(): Pair<String?, String?>
    override suspend fun clearSelectedUnit()
    override suspend fun setOnboardingCompleted(completed: Boolean)
    override suspend fun isOnboardingCompleted(): Boolean
    
    override suspend fun saveCredentials(username: String, email: String, password: String)
    override suspend fun getCachedCredentials(): CachedCredentials?
    override suspend fun clearCachedCredentials()
    override suspend fun isCredentialsCacheValid(): Boolean
    
    override suspend fun saveStatesAndCities(states: List<State>, cities: List<City>)
    override suspend fun getStates(): List<State>
    override suspend fun getCitiesByState(stateId: String): List<City>
}
