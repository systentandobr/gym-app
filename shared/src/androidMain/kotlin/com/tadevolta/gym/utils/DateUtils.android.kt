package com.tadevolta.gym.utils

import android.annotation.SuppressLint
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Implementação Android para conversão de ISO 8601 para timestamp.
 * Usa java.time com core library desugaring para suportar API 24+.
 */
@SuppressLint("NewApi")
actual fun parseIsoToTimestamp(isoString: String): Long? {
    return try {
        // Tentar parsear como ISO 8601 completo (com timezone)
        val instant = Instant.parse(isoString)
        instant.toEpochMilli()
    } catch (e: Exception) {
        // Se falhar, tentar formato sem timezone
        try {
            // Formato sem timezone: "2026-01-31T23:45:05"
            val localDateTime = LocalDateTime.parse(isoString)
            localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e2: Exception) {
            null
        }
    }
}
