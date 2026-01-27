package com.tadevolta.gym.data.remote

import io.ktor.client.*
import io.ktor.client.engine.*
import com.tadevolta.gym.utils.config.EnvironmentConfig

expect fun createHttpClient(): HttpClient

fun createApiClient(tokenProvider: () -> String?): HttpClient {
    return createHttpClient()
}
