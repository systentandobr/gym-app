package com.tadevolta.gym.utils.auth

/**
 * Exceção lançada quando a reautenticação falha após todas as tentativas
 * ou quando o cache de credenciais expirou.
 */
class UnauthenticatedException(
    message: String = "Usuário não autenticado. Por favor, faça login novamente.",
    cause: Throwable? = null
) : Exception(message, cause)
