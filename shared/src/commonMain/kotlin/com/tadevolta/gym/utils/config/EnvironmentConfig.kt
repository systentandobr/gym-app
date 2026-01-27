package com.tadevolta.gym.utils.config

expect object EnvironmentConfig {
    val API_BASE_URL: String
    val SYS_SEGURANCA_API_KEY: String
    val SYS_SEGURANCA_BASE_URL: String
    val DOMAIN: String
}

// Constante para unidade padrão quando usuário não seleciona uma unidade
const val DEFAULT_UNIT_ID = "#BR#ALL#SYSTEM#0001"
