package com.tadevolta.gym.data.repositories

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

actual class SecureUserSessionStorage(
    private val context: Context
) : UserSessionStorage {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    actual override suspend fun saveSelectedUnit(unitId: String, unitName: String) {
        sharedPreferences.edit()
            .putString("selected_unit_id", unitId)
            .putString("selected_unit_name", unitName)
            .apply()
    }
    
    actual override suspend fun getSelectedUnit(): Pair<String?, String?> {
        val unitId = sharedPreferences.getString("selected_unit_id", null)
        val unitName = sharedPreferences.getString("selected_unit_name", null)
        return Pair(unitId, unitName)
    }
    
    actual override suspend fun clearSelectedUnit() {
        sharedPreferences.edit()
            .remove("selected_unit_id")
            .remove("selected_unit_name")
            .apply()
    }
}
