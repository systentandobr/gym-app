# Atualizações Necessárias no App Mobile para Suporte a Times

## Análise de Aderência

Após análise dos serviços do app mobile Kotlin, identifiquei que **o app NÃO está aderente** às novas implementações de times. Abaixo estão os pontos que precisam ser atualizados:

---

## 1. Modelo de Dados - StudentModels.kt

### ❌ Problema Atual
O modelo `Student` não possui o campo `teamId` que foi adicionado no backend.

### ✅ Solução
Adicionar campo `teamId` opcional no modelo `Student`:

```kotlin
@Serializable
data class Student(
    val id: String,
    val unitId: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val cpf: String? = null,
    val birthDate: String? = null,
    val gender: Gender? = null,
    val address: Address? = null,
    val emergencyContact: EmergencyContact? = null,
    val healthInfo: HealthInfo? = null,
    val subscription: StudentSubscription? = null,
    val teamId: String? = null, // ✅ NOVO CAMPO
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
```

---

## 2. CheckInService.kt

### ⚠️ Problema Identificado
O endpoint usado no app (`/students/$studentId/check-in`) **NÃO foi encontrado no backend**. 

**Endpoints existentes no backend:**
- `GET /gamification/students/:studentId/check-ins` - Para histórico (plural) ✅
- `GET /gamification/students/:studentId/weekly-activity` - Atividade semanal ✅

**Endpoint que falta:**
- `POST /students/:studentId/check-in` ou `POST /gamification/students/:studentId/check-in` - Para criar check-in ❌

**Observação:** O check-in parece ser registrado através de `PointTransaction` com `sourceType: 'CHECK_IN'`, mas não há endpoint explícito no controller. É necessário criar este endpoint ou usar outro mecanismo.

### ✅ Solução
Atualizar os endpoints para corresponder ao backend:

```kotlin
// CheckInService.kt

override suspend fun checkIn(studentId: String, location: Location?): Result<CheckIn> {
    return try {
        // ✅ Verificar se o endpoint correto existe no backend
        // Se não existir, usar o endpoint de gamificação
        val response = client.post("/gamification/students/$studentId/check-in") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
            setBody(mapOf(
                "location" to location
            ))
        }
        // ... resto do código
    }
}

override suspend fun getCheckInHistory(studentId: String, limit: Int): Result<List<CheckIn>> {
    return try {
        // ✅ Endpoint correto: /gamification/students/:studentId/check-ins (plural)
        val response = client.get("/gamification/students/$studentId/check-ins") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
            parameter("limit", limit)
        }
        // ... resto do código
    }
}

override suspend fun getCheckInStats(studentId: String): Result<CheckInStats> {
    return try {
        // ✅ Usar endpoint de gamificação ou criar endpoint específico
        val response = client.get("/gamification/students/$studentId/check-ins") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
            parameter("limit", 1) // Para stats, pode usar o histórico
        }
        // ... processar resposta para criar CheckInStats
    }
}
```

---

## 3. GamificationService.kt

### ❌ Problemas Identificados

1. **Endpoint incorreto**: Usa `/gamification/users/$userId` mas o backend usa `/gamification/students/:studentId`
2. **Falta suporte a times**: Não há métodos para buscar métricas ou ranking de times

### ✅ Soluções

#### 3.1 Corrigir Endpoint de Gamificação

```kotlin
override suspend fun getGamificationData(userId: String): Result<GamificationData> {
    return try {
        // ✅ Endpoint correto: /gamification/students/:studentId
        val response = client.get("/gamification/students/$userId") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
        }
        // ... resto do código
    }
}
```

#### 3.2 Adicionar Suporte a Times

```kotlin
interface GamificationService {
    suspend fun getGamificationData(userId: String): Result<GamificationData>
    suspend fun getRanking(unitId: String, limit: Int = 50): Result<List<RankingPosition>>
    suspend fun shareProgress(userId: String): Result<ShareableProgress>
    
    // ✅ NOVOS MÉTODOS PARA TIMES
    suspend fun getTeamMetrics(teamId: String): Result<TeamMetrics>
    suspend fun getTeamsRanking(unitId: String): Result<List<TeamRankingPosition>>
}
```

---

## 4. TrainingPlanService.kt

### ✅ Status Atual
O `TrainingPlanService` está **compatível** com o backend atual. Os endpoints usados estão corretos:
- `GET /training-plans?studentId=xxx` ✅
- `GET /training-plans/:id` ✅
- `PATCH /training-plans/:id/exercises/:exerciseId` ✅

### 📝 Observação
O backend já calcula métricas de times baseadas nos planos de treino dos alunos, então não é necessário alterar este serviço diretamente. As métricas de times são calculadas agregando dados dos alunos.

---

## 5. Novos Modelos Necessários

### 5.1 TeamModels.kt (NOVO ARQUIVO)

```kotlin
package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: String,
    val unitId: String,
    val name: String,
    val description: String? = null,
    val studentIds: List<String> = emptyList(),
    val students: List<Student>? = null,
    val metrics: TeamMetrics? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class TeamMetrics(
    val totalStudents: Int,
    val totalCheckIns: Int,
    val completedTrainings: Int,
    val plannedTrainings: Int,
    val completionRate: Double,
    val averagePoints: Double,
    val currentStreak: Int
)

@Serializable
data class TeamRankingPosition(
    val position: Int,
    val teamId: String,
    val teamName: String,
    val totalCheckIns: Int,
    val completionRate: Double,
    val averagePoints: Double,
    val totalStudents: Int
)
```

### 5.2 Atualizar GamificationModels.kt

Adicionar suporte a ranking de times:

```kotlin
// Adicionar ao arquivo existente
@Serializable
data class TeamsRankingResponse(
    val teams: List<TeamRankingPosition>
)
```

---

## 6. Novo Serviço: TeamService.kt (NOVO ARQUIVO)

```kotlin
package com.tadevolta.gym.data.remote

import com.tadevolta.gym.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

interface TeamService {
    suspend fun getTeams(): Result<List<Team>>
    suspend fun getTeam(id: String): Result<Team>
    suspend fun getTeamMetrics(teamId: String): Result<TeamMetrics>
}

class TeamServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) : TeamService {
    
    override suspend fun getTeams(): Result<List<Team>> {
        return try {
            val response = client.get("/teams") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<List<Team>> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar times"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTeam(id: String): Result<Team> {
        return try {
            val response = client.get("/teams/$id") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<Team> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar time"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getTeamMetrics(teamId: String): Result<TeamMetrics> {
        return try {
            val response = client.get("/teams/$teamId/metrics") {
                headers {
                    tokenProvider()?.let { append("Authorization", "Bearer $it") }
                }
            }
            val json = Json { ignoreUnknownKeys = true }
            val apiResponse: ApiResponse<TeamMetrics> = json.decodeFromString(response.bodyAsText())
            
            if (apiResponse.success && apiResponse.data != null) {
                Result.Success(apiResponse.data)
            } else {
                Result.Error(Exception(apiResponse.error ?: "Erro ao buscar métricas do time"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

---

## 7. Atualizar GamificationService.kt - Métodos de Times

```kotlin
// Adicionar ao GamificationServiceImpl

override suspend fun getTeamMetrics(teamId: String): Result<TeamMetrics> {
    return try {
        val response = client.get("/gamification/teams/$teamId/metrics") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
        }
        val json = Json { ignoreUnknownKeys = true }
        val apiResponse: ApiResponse<TeamMetrics> = json.decodeFromString(response.bodyAsText())
        
        if (apiResponse.success && apiResponse.data != null) {
            Result.Success(apiResponse.data)
        } else {
            Result.Error(Exception(apiResponse.error ?: "Erro ao buscar métricas do time"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}

override suspend fun getTeamsRanking(unitId: String): Result<List<TeamRankingPosition>> {
    return try {
        val response = client.get("/gamification/teams/ranking") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
            parameter("unitId", unitId)
        }
        val json = Json { ignoreUnknownKeys = true }
        val apiResponse: ApiResponse<List<TeamRankingPosition>> = json.decodeFromString(response.bodyAsText())
        
        if (apiResponse.success && apiResponse.data != null) {
            Result.Success(apiResponse.data)
        } else {
            Result.Error(Exception(apiResponse.error ?: "Erro ao buscar ranking de times"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

---

## 8. Resumo de Endpoints - Backend vs App Mobile

| Funcionalidade | Backend (NestJS) | App Mobile (Atual) | Status |
|----------------|-----------------|-------------------|--------|
| Check-in | ❌ **ENDPOINT NÃO ENCONTRADO** | `POST /students/$studentId/check-in` | ❌ **CRÍTICO - Endpoint não existe** |
| Histórico Check-ins | `GET /gamification/students/:id/check-ins` | `GET /students/$studentId/check-in/history` | ❌ Incorreto |
| Stats Check-ins | Via histórico (usar check-ins) | `GET /students/$studentId/check-in/stats` | ❌ Endpoint não existe |
| Gamificação | `GET /gamification/students/:id` | `GET /gamification/users/$userId` | ❌ Endpoint incorreto |
| Ranking | `GET /gamification/ranking?unitId=xxx` | `GET /gamification/ranking?unitId=xxx` | ✅ Correto |
| Times | `GET /teams` | ❌ Não existe | ❌ Não implementado |
| Métricas Time | `GET /teams/:id/metrics` | ❌ Não existe | ❌ Não implementado |
| Ranking Times | `GET /gamification/teams/ranking` | ❌ Não existe | ❌ Não implementado |
| Planos Treino | `GET /training-plans?studentId=xxx` | `GET /training-plans?studentId=xxx` | ✅ Correto |

---

## 9. Checklist de Implementação

### Prioridade CRÍTICA (Bloqueia Funcionalidade)
- [ ] **CRIAR endpoint POST para check-in no backend** ou encontrar alternativa
- [ ] Corrigir endpoint de histórico de check-ins em `CheckInService.kt`
- [ ] Corrigir endpoint de gamificação em `GamificationService.kt`

### Prioridade Alta (Compatibilidade)
- [ ] Adicionar campo `teamId` em `StudentModels.kt`

### Prioridade Média (Novas Funcionalidades)
- [ ] Criar `TeamModels.kt` com modelos de dados
- [ ] Criar `TeamService.kt` para comunicação com API
- [ ] Adicionar métodos de times em `GamificationService.kt`
- [ ] Atualizar ViewModels para suportar times (opcional)

### Prioridade Baixa (Melhorias)
- [ ] Criar UI para exibir times no app
- [ ] Adicionar ranking de times na tela de ranking
- [ ] Mostrar time do aluno no perfil

---

## 10. Observações Importantes

1. **⚠️ CRÍTICO - Endpoint de Check-in**: O endpoint `POST /students/:id/check-in` **NÃO foi encontrado no backend**. O app mobile **NÃO conseguirá fazer check-in** até que este endpoint seja criado ou uma alternativa seja implementada. O check-in parece ser registrado via `PointTransaction` com `sourceType: 'CHECK_IN'`, mas não há endpoint público para isso.

2. **Compatibilidade Retroativa**: O campo `teamId` é opcional, então o app continuará funcionando mesmo sem atualizar imediatamente. Porém, os endpoints de check-in precisam ser corrigidos urgentemente.

3. **Times são Opcionais**: Alunos podem não ter time associado, então todas as funcionalidades devem funcionar mesmo sem times.

4. **Métricas de Times**: As métricas são calculadas no backend agregando dados dos alunos, então não é necessário fazer múltiplas chamadas no app mobile.

5. **TrainingPlanService está OK**: O serviço de planos de treino está usando os endpoints corretos e não precisa de alterações para suportar times. As métricas de times são calculadas agregando dados dos alunos que já estão sendo buscados.

---

## Conclusão

O app mobile **NÃO está aderente** às novas implementações de times e também possui **problemas críticos com endpoints existentes**:

### Status por Serviço:

1. **CheckInService.kt** ❌ **CRÍTICO**
   - Endpoint de criação de check-in não existe no backend
   - Endpoint de histórico está incorreto
   - Endpoint de stats não existe

2. **GamificationService.kt** ❌ **ALTO**
   - Endpoint de dados de gamificação está incorreto (`/users/` vs `/students/`)
   - Falta suporte a times (métricas e ranking)

3. **TrainingPlanService.kt** ✅ **OK**
   - Endpoints corretos
   - Não precisa de alterações para suportar times
   - Métricas de times são calculadas no backend agregando dados dos alunos

4. **StudentModels.kt** ⚠️ **MÉDIO**
   - Falta campo `teamId` (mas é opcional, não quebra funcionalidade)

### Recomendações de Prioridade:

1. **🚨 CRÍTICO**: Criar endpoint POST para check-in no backend OU corrigir o app para usar alternativa existente
2. **🔴 URGENTE**: Corrigir endpoints de check-in e gamificação no app mobile
3. **🟡 IMPORTANTE**: Adicionar suporte básico a times (modelos e serviços)
4. **🟢 OPCIONAL**: Criar UI para times (pode ser feito depois)
