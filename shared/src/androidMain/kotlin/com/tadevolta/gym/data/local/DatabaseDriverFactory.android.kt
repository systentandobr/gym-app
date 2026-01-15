package com.tadevolta.gym.data.local

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.tadevolta.gym.data.local.TadevoltaDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = TadevoltaDatabase.Schema,
            context = context,
            name = "tadevolta_gym.db"
        )
    }
}
