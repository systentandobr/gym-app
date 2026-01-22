package com.tadevolta.gym.data.repositories

interface UserSessionStorage {
    suspend fun saveSelectedUnit(unitId: String, unitName: String)
    suspend fun getSelectedUnit(): Pair<String?, String?>
    suspend fun clearSelectedUnit()
}

expect class SecureUserSessionStorage : UserSessionStorage {
    override suspend fun saveSelectedUnit(unitId: String, unitName: String)
    override suspend fun getSelectedUnit(): Pair<String?, String?>
    override suspend fun clearSelectedUnit()
}
