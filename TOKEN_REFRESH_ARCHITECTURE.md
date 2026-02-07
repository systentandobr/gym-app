# Arquitetura de Autenticação e Refresh Token

## Visão Geral

O projeto implementa um sistema robusto de gerenciamento de tokens com:

1. **Refresh automático de token** quando recebe HTTP 401
2. **Reautenticação com cache** usando credenciais salvas
3. **Prevenção de race conditions** com TokenManager centralizado
4. **Retry automático** em background sem intervenção do usuário
5. **Fallback progressivo** (refresh token → reautenticação → logout)

## Componentes Principais

### 1. TokenManager (`com.tadevolta.gym.data.manager.TokenManager`)

Gerenciador centralizado que controla o refresh de tokens de forma thread-safe.

**Características:**
- Usa `Mutex` para garantir que apenas um refresh ocorra por vez
- Evita múltiplos refreshs simultâneos (race conditions)
- Timeout de 30 segundos para operações de refresh
- Prevenção pró-ativa de expiração (renova a cada 5 minutos)

**Uso:**
```kotlin
// Injeta o TokenManager
class MeuViewModel @Inject constructor(
    private val tokenManager: TokenManager
) {
    suspend fun exemplo() {
        // Obtém token com refresh preventivo automático
        val token = tokenManager.getAccessToken()
        
        // Força refresh se necessário
        val success = tokenManager.refreshTokenIfNeeded()
    }
}
```

### 2. executeWithRetry (`com.tadevolta.gym.data.remote.HttpRequestHelper`)

Helper que executa requisições HTTP com retry automático em caso de erro 401.

**Fluxo de Retry:**
1. Executa requisição com token atual
2. Se 401 → tenta refresh do token via TokenManager
3. Se refresh falhar → tenta reautenticação com cache
4. Se reautenticação falhar → força logout e lança `UnauthenticatedException`
5. Se sucesso → retry da requisição original (máximo 3 tentativas)

**Uso em Services:**
```kotlin
class MeuServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) {
    suspend fun buscarDados(): Result<Dados> {
        return try {
            val response = executeWithRetry(
                client = client,
                authRepository = authRepository,
                tokenManager = tokenManager,
                tokenProvider = tokenProvider,
                maxRetries = 3,
                requestBuilder = {
                    url("${EnvironmentConfig.API_BASE_URL}/dados")
                },
                responseHandler = { it }
            )
            // Processar resposta...
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### 3. AuthRepository (`com.tadevolta.gym.data.repositories.AuthRepository`)

Responsável pela lógica de autenticação e reautenticação.

**Métodos principais:**

- **`refreshTokenIfNeeded()`**: Renova o access token usando refresh token
- **`reauthenticateWithCache()`**: Tenta login automático com credenciais em cache
- **`forceLogout()`**: Limpa todos os tokens e dados quando autenticação falha

**Fluxo de Reautenticação:**
```
refreshTokenIfNeeded()
    ↓ (se falhar)
reauthenticateWithCache()
    ↓ (se falhar)
forceLogout() + UnauthenticatedException
```

### 4. TokenStorage (`com.tadevolta.gym.data.repositories.TokenStorage`)

Armazena tokens de forma segura usando `EncryptedSharedPreferences`.

**Características:**
- Criptografia AES256_GCM
- Tratamento de corrupção de dados (limpa e recria se necessário)
- Verificação automática de expiração
- Armazena: access token, refresh token, timestamp de expiração

## Configuração de DI (AppModule)

```kotlin
@Provides
@Singleton
fun provideTokenManager(
    tokenStorage: SecureTokenStorage,
    authRepository: Lazy<AuthRepository>
): TokenManager {
    return TokenManager(tokenStorage, authRepository.get())
}

@Provides
@Singleton
fun provideMeuService(
    client: HttpClient,
    tokenStorage: SecureTokenStorage,
    authRepository: Lazy<AuthRepository>
): MeuService {
    return MeuServiceImpl(
        client = client,
        tokenProvider = { 
            kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
        },
        authRepository = authRepository.get()
    )
}
```

## Fluxo Completo de Requisição

```
1. ViewModel chama Service
   ↓
2. Service usa executeWithRetry
   ↓
3. HTTP Request com token atual
   ↓
4. Recebe 401 Unauthorized?
   ├── NÃO → Retorna resposta normal
   └── SIM → Continua...
        ↓
5. TokenManager.refreshTokenIfNeeded()
   ├── SUCESSO → Retry com novo token (volta para 3)
   └── FALHA → Continua...
        ↓
6. AuthRepository.reauthenticateWithCache()
   ├── SUCESSO → Retry com novo token (volta para 3)
   └── FALHA → Continua...
        ↓
7. AuthRepository.forceLogout()
   ↓
8. Lança UnauthenticatedException
   ↓
9. ViewModel recebe erro → Navega para Login
```

## Services Protegidos (com executeWithRetry)

Todos os services abaixo já estão protegidos com retry automático:

- ✅ UserService
- ✅ TrainingService
- ✅ TrainingPlanService
- ✅ CheckInService
- ✅ GamificationService
- ✅ TeamService
- ✅ ExerciseService
- ✅ ReferralService
- ✅ StudentService
- ✅ SubscriptionService
- ✅ BioimpedanceService
- ✅ FranchiseService
- ✅ UnitOccupancyService

## Services Públicos (sem autenticação)

- LeadService (endpoint público para captura de leads)

## Tratamento de Erros

### 1. UnauthenticatedException

Lançada quando todas as tentativas de reautenticação falham.

```kotlin
try {
    val result = service.buscarDados()
} catch (e: UnauthenticatedException) {
    // Redirecionar para tela de login
    navController.navigate("login")
}
```

### 2. Network Errors

Timeout e erros de conectividade também são tratados com retry:

```kotlin
// Timeout aguarda e tenta novamente (até maxRetries)
catch (e: SocketTimeoutException) {
    if (retryCount < maxRetries) {
        retryCount++
        continue // Tenta novamente
    }
    throw e
}
```

## Melhores Práticas

### 1. Sempre use executeWithRetry

Para novos services, sempre utilize `executeWithRetry`:

```kotlin
// ❌ SEM proteção
val response = client.get(url) {
    headers { tokenProvider()?.let { append("Authorization", "Bearer $it") } }
}

// ✅ COM proteção
val response = executeWithRetry(
    client = client,
    authRepository = authRepository,
    tokenProvider = tokenProvider,
    maxRetries = 3,
    requestBuilder = { url(url) },
    responseHandler = { it }
)
```

### 2. Inclua authRepository no construtor

```kotlin
class MeuServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: suspend () -> String?,
    private val authRepository: AuthRepository? = null,
    private val tokenManager: TokenManager? = null
) : MeuService
```

### 3. Atualize AppModule

```kotlin
@Provides
@Singleton
fun provideMeuService(
    client: HttpClient,
    tokenStorage: SecureTokenStorage,
    authRepository: Lazy<AuthRepository>
): MeuService {
    return MeuServiceImpl(
        client = client,
        tokenProvider = { 
            kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
        },
        authRepository = authRepository.get()
    )
}
```

## Configurações

### Constantes do TokenManager

```kotlin
companion object {
    // Intervalo mínimo entre refreshs (5 minutos)
    private const val MIN_REFRESH_INTERVAL_MS = 5 * 60 * 1000L
    
    // Timeout para operação de refresh (30 segundos)
    private const val REFRESH_TIMEOUT_MS = 30_000L
}
```

### Configuração de Retry

```kotlin
executeWithRetry(
    maxRetries = 3,  // Padrão: 3 tentativas
    // ...
)
```

## Monitoramento e Logs

O sistema gera logs em cada etapa:

```
// Sucesso no refresh
TokenManager: Token refreshed successfully

// Falha no refresh
TokenManager: Token refresh failed, attempting reauthentication

// Reautenticação bem-sucedida
AuthRepository: Reauthenticated with cached credentials

// Falha completa
AuthRepository: Authentication failed, forcing logout
```

## Troubleshooting

### Problema: Múltiplos refreshs simultâneos

**Solução:** Já resolvido pelo TokenManager com Mutex.

### Problema: Token expira muito rápido

**Solução:** O TokenManager já tenta renovar preventivamente a cada 5 minutos.

### Problema: Reautenticação falha constantemente

**Verificar:**
1. Credenciais em cache estão válidas (`CachedCredentials.isValid()`)
2. Cache não expirou (7 dias por padrão)
3. Usuário não alterou senha

### Problema: Erro 401 persistente

**Causas comuns:**
1. Refresh token expirado
2. Credenciais em cache inválidas
3. Usuário desativado

**Solução:** O sistema automaticamente força logout e redireciona para login.

## Futuras Melhorias

1. **Interceptor Global**: Implementar interceptor no Ktor para tratamento automático em todas as requisições
2. **Proactive Token Refresh**: Background job para renovar token antes de expirar
3. **Multiple Token Support**: Suporte a múltiplos tokens (APIs diferentes)
4. **Token Rotation**: Implementar rotação de refresh tokens para maior segurança

---

## Resumo

✅ **Refresh automático** em caso de 401  
✅ **Reautenticação** com credenciais salvas  
✅ **Thread-safe** com Mutex  
✅ **Retry automático** (até 3x)  
✅ **Fallback progressivo** para logout  
✅ **Transparente** para o usuário  
✅ **Todos os services** protegidos  

O sistema garante que o usuário permaneça autenticado de forma transparente, mesmo quando tokens expiram ou a conexão é instável.