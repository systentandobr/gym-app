package com.tadevolta.gym.utils

/**
 * Utilitário para conversão de datas.
 * Usa expect/actual para implementações específicas de plataforma.
 */

expect fun parseIsoToTimestamp(isoString: String): Long?
