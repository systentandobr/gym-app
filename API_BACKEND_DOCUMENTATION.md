# Documentação de APIs para Integração Frontend - Gym App

Este documento descreve todas as APIs necessárias para integrar o frontend Android com o backend, incluindo check-ins, atividade semanal, planos de treino e ranking.

## 📋 Índice

1. [Histórico de Check-ins](#1-histórico-de-check-ins)
2. [Atividade Semanal (Últimos 7 Dias)](#2-atividade-semanal-últimos-7-dias)
3. [Planos de Treino do Usuário](#3-planos-de-treino-do-usuário)
4. [Ranking de Usuários](#4-ranking-de-usuários)
5. [Captura de Leads Públicos](#5-captura-de-leads-públicos)

---

## 1. Histórico de Check-ins

### 1.1 Endpoint: GET `/gamification/students/{studentId}/check-ins`

**Descrição:** Retorna o histórico de check-ins do aluno ordenado por data (mais recente primeiro).

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `studentId` (string, obrigatório): ID do aluno (relacionado ao schema de students)

**Query Parameters:**
- `limit` (number, opcional): Número máximo de resultados. Default: 50
- `startDate` (string, opcional): Data inicial (ISO 8601). Ex: `2023-10-01T00:00:00Z`
- `endDate` (string, opcional): Data final (ISO 8601). Ex: `2023-10-31T23:59:59Z`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
        "checkIns": [
      {
        "id": "string",
        "studentId": "string",
        "date": "2023-10-15T08:30:00Z",
        "points": 10,
        "unitId": "string",
        "metadata": {
          "location": {
            "lat": -5.7793,
            "lng": -35.2009
          },
          "device": "Android"
        }
      }
    ],
    "total": 45,
    "currentStreak": 5,
    "longestStreak": 12
  },
  "error": null
}
```

**Campos da Resposta:**
- `checkIns`: Array de check-ins ordenados por data (mais recente primeiro)
- `total`: Total de check-ins no período
- `currentStreak`: Sequência atual de dias consecutivos com check-in
- `longestStreak`: Maior sequência de dias consecutivos já alcançada

**Erros Possíveis:**
- `400 Bad Request`: Parâmetros inválidos
- `401 Unauthorized`: Token inválido ou expirado
- `404 Not Found`: Aluno não encontrado

**Notas:**
- Os check-ins são baseados em transações de pontos com `sourceType: 'CHECK_IN'`
- A data é extraída do campo `createdAt` da transação
- O streak é calculado verificando dias consecutivos com check-ins
- O `studentId` deve corresponder a um registro válido no schema de `students`

---

## 2. Atividade Semanal (Últimos 7 Dias)

### 2.1 Endpoint: GET `/gamification/students/{studentId}/weekly-activity`

**Descrição:** Retorna a atividade do aluno agrupada por dia dos últimos 7 dias, incluindo check-ins, treinos completados e exercícios realizados.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `studentId` (string, obrigatório): ID do aluno (relacionado ao schema de students)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "period": {
      "startDate": "2023-10-09T00:00:00Z",
      "endDate": "2023-10-15T23:59:59Z"
    },
    "dailyActivity": [
      {
        "date": "2023-10-15",
        "dayOfWeek": "DOM",
        "checkIns": 1,
        "workoutsCompleted": 1,
        "exercisesCompleted": 8,
        "totalPoints": 85,
        "activities": [
          {
            "type": "CHECK_IN",
            "time": "08:30",
            "points": 10,
            "description": "Check-in realizado"
          },
          {
            "type": "WORKOUT_COMPLETION",
            "time": "09:15",
            "points": 50,
            "description": "Treino completo realizado"
          },
          {
            "type": "EXERCISE_COMPLETION",
            "time": "09:20",
            "points": 5,
            "description": "Exercício: Supino reto"
          }
        ]
      },
      {
        "date": "2023-10-14",
        "dayOfWeek": "SAB",
        "checkIns": 1,
        "workoutsCompleted": 0,
        "exercisesCompleted": 0,
        "totalPoints": 10,
        "activities": [
          {
            "type": "CHECK_IN",
            "time": "07:45",
            "points": 10,
            "description": "Check-in realizado"
          }
        ]
      }
    ],
    "summary": {
      "totalCheckIns": 7,
      "totalWorkouts": 4,
      "totalExercises": 32,
      "totalPoints": 420,
      "averagePointsPerDay": 60
    }
    "error": null
  }
}
```

**Campos da Resposta:**
- `period`: Período analisado (últimos 7 dias)
- `dailyActivity`: Array com atividade de cada dia (ordenado do mais recente para o mais antigo)
  - `date`: Data no formato YYYY-MM-DD
  - `dayOfWeek`: Dia da semana abreviado (DOM, SEG, TER, QUA, QUI, SEX, SAB)
  - `checkIns`: Número de check-ins no dia
  - `workoutsCompleted`: Número de treinos completados
  - `exercisesCompleted`: Número de exercícios completados
  - `totalPoints`: Total de pontos ganhos no dia
  - `activities`: Lista detalhada de atividades do dia ordenadas por horário
- `summary`: Resumo do período
  - `totalCheckIns`: Total de check-ins nos 7 dias
  - `totalWorkouts`: Total de treinos completados
  - `totalExercises`: Total de exercícios completados
  - `totalPoints`: Total de pontos ganhos
  - `averagePointsPerDay`: Média de pontos por dia

**Erros Possíveis:**
- `401 Unauthorized`: Token inválido ou expirado
- `404 Not Found`: Aluno não encontrado

**Notas:**
- Os dados são calculados a partir de transações de pontos (`PointTransaction`)
- Tipos de atividade: `CHECK_IN`, `WORKOUT_COMPLETION`, `EXERCISE_COMPLETION`
- Dias sem atividade aparecem com valores zerados
- Horários são formatados em HH:mm (24h)
- O `studentId` deve corresponder a um registro válido no schema de `students`

---

## 3. Planos de Treino do Usuário

### 3.1 Endpoint: GET `/training-plans`

**Descrição:** Lista os planos de treino do usuário autenticado ou de um aluno específico.

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `studentId` (string, opcional): ID do aluno. Se não fornecido, retorna planos do usuário autenticado
- `status` (string, opcional): Filtrar por status. Valores: `active`, `paused`, `completed`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "plans": [
      {
        "id": "string",
        "unitId": "string",
        "studentId": "string",
        "name": "Hipertrofia - Iniciante",
        "description": "Plano de treino focado em ganho de massa muscular",
        "objectives": [
          "Ganho de massa muscular",
          "Força",
          "Resistência"
        ],
        "weeklySchedule": [
          {
            "dayOfWeek": 1,
            "timeSlots": [
              {
                "startTime": "08:00",
                "endTime": "09:30",
                "activity": "Treino de Peito e Tríceps"
              }
            ],
            "exercises": [
              {
                "exerciseId": "string",
                "name": "Supino reto",
                "sets": 4,
                "reps": "8-10",
                "weight": 60,
                "restTime": 90,
                "notes": "Foco na execução"
              }
            ]
          }
        ],
        "exercises": [],
        "startDate": "2023-10-01T00:00:00Z",
        "endDate": "2023-11-30T23:59:59Z",
        "status": "active",
        "progress": {
          "completedObjectives": ["Força"],
          "lastUpdate": "2023-10-15T00:00:00Z",
          "notes": "Bom progresso na força"
        },
        "isTemplate": false,
        "targetGender": "male",
        "createdAt": "2023-10-01T00:00:00Z",
        "updatedAt": "2023-10-15T00:00:00Z"
      }
    ],
    "total": 3,
    "page": 1,
    "limit": 50
  },
  "error": null
}
```

**Campos da Resposta:**
- `plans`: Array de planos de treino
  - `id`: ID do plano
  - `unitId`: ID da unidade
  - `studentId`: ID do aluno
  - `name`: Nome do plano
  - `description`: Descrição do plano
  - `objectives`: Array de objetivos
  - `weeklySchedule`: Cronograma semanal
    - `dayOfWeek`: Dia da semana (0=Domingo, 1=Segunda, ..., 6=Sábado)
    - `timeSlots`: Horários de treino
    - `exercises`: Exercícios do dia
  - `exercises`: Exercícios gerais (compatibilidade retroativa)
  - `startDate`: Data de início
  - `endDate`: Data de término (opcional)
  - `status`: Status do plano (`active`, `paused`, `completed`)
  - `progress`: Progresso do plano
  - `isTemplate`: Indica se é um template
  - `targetGender`: Gênero alvo (`male`, `female`, `other`)
  - `createdAt`: Data de criação
  - `updatedAt`: Data de atualização

**Erros Possíveis:**
- `400 Bad Request`: Dados inválidos
- `401 Unauthorized`: Token inválido ou expirado

**Notas:**
- Se `studentId` não for fornecido, retorna planos do usuário autenticado
- Planos são ordenados por data de criação (mais recente primeiro)
- Apenas planos da mesma unidade (`unitId`) do usuário são retornados

---

## 4. Ranking de Usuários

### 4.1 Endpoint: GET `/gamification/ranking`

**Descrição:** Retorna o ranking de usuários por unidade, ordenado por pontos totais e nível.

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `unitId` (string, obrigatório): ID da unidade
- `limit` (number, opcional): Número máximo de resultados. Default: 50

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "position": 1,
      "totalPoints": 4200,
      "level": 15,
      "unitId": "FR-001",
      "unitName": "Unidade Centro",
      "userId": "string",
      "userName": "Beatriz L."
    },
    {
      "position": 2,
      "totalPoints": 3800,
      "level": 14,
      "unitId": "FR-001",
      "unitName": "Unidade Centro",
      "userId": "string",
      "userName": "Lucas R."
    },
    {
      "position": 3,
      "totalPoints": 3100,
      "level": 12,
      "unitId": "FR-001",
      "unitName": "Unidade Centro",
      "userId": "string",
      "userName": "Alex"
    }
  ],
  "error": null
}
```

**Campos da Resposta:**
- `position`: Posição no ranking (1 = primeiro lugar)
- `totalPoints`: Total de pontos do usuário
- `level`: Nível atual do usuário
- `unitId`: ID da unidade
- `unitName`: Nome da unidade
- `userId`: ID do usuário
- `userName`: Nome do usuário

**Ordenação:**
1. Por `totalPoints` descendente (maior pontuação primeiro)
2. Por `level` descendente (maior nível primeiro)

**Erros Possíveis:**
- `400 Bad Request`: `unitId` não fornecido
- `401 Unauthorized`: Token inválido ou expirado

**Notas:**
- O ranking é calculado em tempo real
- Apenas usuários ativos são incluídos
- A posição é calculada dinamicamente baseada nos pontos totais

---

## 5. Captura de Leads Públicos

### 5.1 Endpoint: POST `/leads/public`

**Descrição:** Cria um novo lead publicamente (sem autenticação), permitindo capturar interesse de novas academias interessadas em se cadastrar ou alunos interessados em se matricular. Permite especificar a unidade selecionada e o tipo de segmento de mercado para demonstrar interesse genuíno.

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "João Silva",
  "email": "joao.silva@example.com",
  "phone": "+5511999999999",
  "city": "Natal",
  "state": "RN",
  "unitId": "FR-001",
  "marketSegment": "gym",
  "userType": "student",
  "objectives": {
    "primary": "Quero me matricular na academia",
    "secondary": ["Melhorar condicionamento físico", "Perder peso"],
    "interestedInFranchise": false
  },
  "metadata": {
    "selectedUnitName": "Unidade Centro",
    "preferredContactTime": "manhã",
    "howDidYouKnow": "Instagram"
  }
}
```

**Campos do Request:**
- `name` (string, obrigatório): Nome completo do lead
- `email` (string, obrigatório): Email válido
- `phone` (string, obrigatório): Telefone com DDD
- `city` (string, opcional): Cidade
- `state` (string, opcional): Estado (UF)
- `unitId` (string, opcional): ID da unidade selecionada. Se não fornecido, usa unidade padrão do sistema
- `marketSegment` (string, opcional): Tipo de segmento de mercado. Valores: `gym`, `restaurant`, `delivery`, `retail`, `ecommerce`, `hybrid`, `solar_plant`
- `userType` (string, obrigatório): Tipo de usuário. Valores: `student` (aluno interessado) ou `franchise` (nova academia interessada em se cadastrar)
- `objectives` (object, opcional): Objetivos do lead
  - `primary` (string): Objetivo principal
  - `secondary` (string[]): Objetivos secundários
  - `interestedInFranchise` (boolean): Se é `student`, indica se tem interesse em se tornar franqueado no futuro
- `metadata` (object, opcional): Metadados adicionais
  - `selectedUnitName` (string): Nome da unidade selecionada (para referência)
  - `preferredContactTime` (string): Horário preferido para contato
  - `howDidYouKnow` (string): Como conheceu nosso aplicativo?
  - `[key: string]`: Outros campos personalizados

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "string",
    "unitId": "FR-001",
    "name": "João Silva",
    "email": "joao.silva@example.com",
    "phone": "+5511999999999",
    "city": "Natal",
    "state": "RN",
    "source": "landing-page",
    "status": "new",
    "score": 75,
    "userType": "student",
    "marketSegment": "gym",
    "objectives": {
      "primary": "Quero me matricular na academia",
      "secondary": ["Melhorar condicionamento físico", "Perder peso"],
      "interestedInFranchise": false
    },
    "metadata": {
      "selectedUnitName": "Unidade Centro",
      "preferredContactTime": "manhã",
      "howDidYouKnow": "Instagram"
    },
    "createdAt": "2023-10-15T10:30:00Z",
    "updatedAt": "2023-10-15T10:30:00Z"
  },
  "error": null
}
```

**Validações Necessárias:**
- Email deve ser válido e único (por unidade)
- Telefone deve estar em formato válido
- `userType` deve ser `student` ou `franchise`
- `marketSegment` deve ser um dos valores permitidos
- Se `unitId` for fornecido, deve ser válido

**Erros Possíveis:**
- `400 Bad Request`: Dados inválidos ou campos obrigatórios faltando
- `409 Conflict`: Email já cadastrado como lead nesta unidade (atualiza lead existente)
- `500 Internal Server Error`: Erro no servidor

**Notas:**
- Este endpoint é **público** (não requer autenticação)
- Se um lead com o mesmo email já existe na unidade, ele é **atualizado** ao invés de criar duplicado
- O `score` é calculado automaticamente baseado nos dados fornecidos
- O campo `source` é automaticamente definido como `landing-page` se não fornecido
- O `unitId` pode ser obtido através do endpoint `/franchises/nearby` antes de criar o lead
- O campo `userType` é obrigatório e deve ser `student` (aluno interessado) ou `franchise` (nova academia)
- O campo `marketSegment` ajuda a identificar o tipo de negócio (ex: `gym` para academias)
- Os `objectives` ajudam a demonstrar interesse genuíno e objetivos do lead
- Para leads do tipo `franchise`, é recomendado incluir informações adicionais no `metadata` como `franchiseType`, `experience`, `budget`, `timeToStart`

**Exemplo de Uso - Aluno Interessado:**
```json
{
  "name": "Maria Santos",
  "email": "maria@example.com",
  "phone": "+5511988888888",
  "city": "Natal",
  "state": "RN",
  "unitId": "FR-001",
  "marketSegment": "gym",
  "userType": "student",
  "objectives": {
    "primary": "Quero me matricular e começar a treinar",
    "secondary": ["Ganhar massa muscular", "Melhorar saúde"],
    "interestedInFranchise": false
  },
  "metadata": {
    "selectedUnitName": "Unidade Centro",
    "preferredContactTime": "tarde",
    "howDidYouKnow": "Indicação de amigo"
  }
}
```

**Exemplo de Uso - Nova Academia Interessada:**
```json
{
  "name": "Carlos Oliveira",
  "email": "carlos@academiaexemplo.com",
  "phone": "+5511977777777",
  "city": "Recife",
  "state": "PE",
  "unitId": null,
  "marketSegment": "gym",
  "userType": "franchise",
  "objectives": {
    "primary": "Quero abrir uma nova unidade",
    "secondary": ["Expandir negócio", "Investir em fitness"],
    "interestedInFranchise": true
  },
  "metadata": {
    "selectedUnitName": null,
    "preferredContactTime": "qualquer horário",
    "howDidYouKnow": "Google",
    "franchiseType": "premium",
    "experience": "Tenho experiência em gestão de academias",
    "budget": "R$ 200.000 - R$ 500.000",
    "timeToStart": "3-6 meses"
  }
}
```

---

## 📊 Modelos de Dados

> **⚠️ Importante:** Os endpoints de check-ins e atividades semanais (`/gamification/students/{studentId}/...`) utilizam `studentId` porque a relação é com o schema de `students`, não com `users`. O `studentId` deve corresponder a um registro válido na coleção `students`.

### CheckIn
```typescript
interface CheckIn {
  id: string;
  studentId: string; // ID do aluno (relacionado ao schema de students)
  date: string; // ISO 8601
  points: number;
  unitId: string;
  metadata?: {
    location?: {
      lat: number;
      lng: number;
    };
    device?: string;
    [key: string]: any;
  };
}
```

### DailyActivity
```typescript
interface DailyActivity {
  date: string; // YYYY-MM-DD
  dayOfWeek: string; // DOM, SEG, TER, QUA, QUI, SEX, SAB
  checkIns: number;
  workoutsCompleted: number;
  exercisesCompleted: number;
  totalPoints: number;
  activities: Activity[];
}

interface Activity {
  type: 'CHECK_IN' | 'WORKOUT_COMPLETION' | 'EXERCISE_COMPLETION';
  time: string; // HH:mm
  points: number;
  description: string;
}
```

### TrainingPlan
```typescript
interface TrainingPlan {
  id: string;
  unitId: string;
  studentId: string;
  name: string;
  description?: string;
  objectives: string[];
  weeklySchedule: WeeklySchedule[];
  exercises?: Exercise[];
  startDate: string; // ISO 8601
  endDate?: string; // ISO 8601
  status: 'active' | 'paused' | 'completed';
  progress?: {
    completedObjectives: string[];
    lastUpdate: string; // ISO 8601
    notes?: string;
  };
  isTemplate?: boolean;
  targetGender?: 'male' | 'female' | 'other';
  createdAt?: string; // ISO 8601
  updatedAt?: string; // ISO 8601
}

interface WeeklySchedule {
  dayOfWeek: number; // 0-6 (0=Domingo)
  timeSlots: TimeSlot[];
  exercises?: Exercise[];
}

interface TimeSlot {
  startTime: string; // HH:mm
  endTime: string; // HH:mm
  activity: string;
}

interface Exercise {
  exerciseId?: string;
  name: string;
  sets: number;
  reps: string;
  weight?: number;
  restTime?: number;
  notes?: string;
}
```

### RankingPosition
```typescript
interface RankingPosition {
  position: number;
  totalPoints: number;
  level: number;
  unitId: string;
  unitName: string;
  userId: string;
  userName: string;
}
```

### Lead
```typescript
interface Lead {
  id: string;
  unitId: string;
  name: string;
  email: string;
  phone: string;
  city?: string;
  state?: string;
  source: 'chatbot' | 'website' | 'whatsapp' | 'form' | 'referral';
  status: 'new' | 'contacted' | 'qualified' | 'converted' | 'customer' | 'lost';
  score: number; // 0-100
  userType: 'student' | 'franchise';
  marketSegment?: string;
  objectives?: {
    primary: string;
    secondary: string[];
    interestedInFranchise: boolean;
  };
  metadata?: {
    selectedUnitName?: string;
    preferredContactTime?: string;
    howDidYouKnow?: string;
    franchiseType?: string;
    experience?: string;
    budget?: string;
    timeToStart?: string;
    [key: string]: any;
  };
  tags?: string[];
  createdAt: string; // ISO 8601
  updatedAt: string; // ISO 8601
}

interface CreateLeadRequest {
  name: string;
  email: string;
  phone: string;
  city?: string;
  state?: string;
  unitId?: string;
  marketSegment?: 'gym' | 'restaurant' | 'delivery' | 'retail' | 'ecommerce' | 'hybrid' | 'solar_plant';
  userType: 'student' | 'franchise';
  objectives?: {
    primary: string;
    secondary?: string[];
    interestedInFranchise?: boolean;
  };
  metadata?: {
    selectedUnitName?: string;
    preferredContactTime?: string;
    howDidYouKnow?: string;
    [key: string]: any;
  };
}
```

---

## 🔐 Autenticação

Todos os endpoints requerem autenticação via Bearer Token:

```
Authorization: Bearer {token}
```

O token deve ser validado e o usuário deve estar autenticado. Em caso de token inválido ou expirado, retornar:

```json
{
  "success": false,
  "data": null,
  "error": "Token inválido ou expirado"
}
```

Com status code `401 Unauthorized`.

---

## 📝 Notas de Implementação

### Status de Implementação

1. **✅ Implementado:**
   - GET `/gamification/ranking` - Ranking de usuários
   - GET `/training-plans` - Listar planos de treino
   - GET `/gamification/students/{studentId}/check-ins` - Histórico de check-ins
   - GET `/gamification/students/{studentId}/weekly-activity` - Atividade semanal

Todos os endpoints estão prontos para uso!

### Considerações Técnicas

- Todos os endpoints seguem o padrão de resposta `ApiResponse<T>`
- Datas devem ser retornadas em formato ISO 8601
- Validações devem ser feitas no backend antes de retornar dados
- Implementar cache para dados de ranking (atualizar a cada X minutos)
- Agregações devem ser otimizadas para performance

### Testes Recomendados

- Testes unitários para validações e lógica de negócio
- Testes de integração para fluxos completos
- Testes de performance para ranking com muitos usuários
- Testes de segurança para endpoints de autenticação

---

## 📱 Exemplos de Integração Frontend

### Exemplo 1: Buscar Histórico de Check-ins

```kotlin
// No seu ViewModel ou Service
suspend fun getCheckInHistory(
    studentId: String, // ID do aluno (relacionado ao schema de students)
    startDate: String? = null,
    endDate: String? = null,
    limit: Int = 50
): Result<CheckInHistory> {
    return try {
        val response = client.get("/gamification/students/$studentId/check-ins") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
            parameter("startDate", startDate)
            parameter("endDate", endDate)
            parameter("limit", limit)
        }
        
        val json = Json { ignoreUnknownKeys = true }
        val apiResponse: ApiResponse<CheckInHistory> = 
            json.decodeFromString(response.bodyAsText())
        
        if (apiResponse.success && apiResponse.data != null) {
            Result.Success(apiResponse.data)
        } else {
            Result.Error(Exception(apiResponse.error ?: "Erro ao buscar check-ins"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

### Exemplo 2: Buscar Atividade Semanal

```kotlin
suspend fun getWeeklyActivity(studentId: String): Result<WeeklyActivity> {
    // studentId: ID do aluno (relacionado ao schema de students)
    return try {
        val response = client.get("/gamification/students/$studentId/weekly-activity") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
        }
        
        val json = Json { ignoreUnknownKeys = true }
        val apiResponse: ApiResponse<WeeklyActivity> = 
            json.decodeFromString(response.bodyAsText())
        
        if (apiResponse.success && apiResponse.data != null) {
            Result.Success(apiResponse.data)
        } else {
            Result.Error(Exception(apiResponse.error ?: "Erro ao buscar atividade"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

### Exemplo 3: Buscar Planos de Treino

```kotlin
suspend fun getTrainingPlans(
    studentId: String? = null,
    status: String? = null
): Result<List<TrainingPlan>> {
    return try {
        val response = client.get("/training-plans") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
            parameter("studentId", studentId)
            parameter("status", status)
        }
        
        val json = Json { ignoreUnknownKeys = true }
        val apiResponse: ApiResponse<TrainingPlansResponse> = 
            json.decodeFromString(response.bodyAsText())
        
        if (apiResponse.success && apiResponse.data != null) {
            Result.Success(apiResponse.data.plans)
        } else {
            Result.Error(Exception(apiResponse.error ?: "Erro ao buscar planos"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

### Exemplo 4: Buscar Ranking

```kotlin
suspend fun getRanking(unitId: String, limit: Int = 50): Result<List<RankingPosition>> {
    return try {
        val response = client.get("/gamification/ranking") {
            headers {
                tokenProvider()?.let { append("Authorization", "Bearer $it") }
            }
            parameter("unitId", unitId)
            parameter("limit", limit)
        }
        
        val json = Json { ignoreUnknownKeys = true }
        val apiResponse: ApiResponse<List<RankingPosition>> = 
            json.decodeFromString(response.bodyAsText())
        
        if (apiResponse.success && apiResponse.data != null) {
            Result.Success(apiResponse.data)
        } else {
            Result.Error(Exception(apiResponse.error ?: "Erro ao buscar ranking"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

### Exemplo 5: Criar Lead Público (Aluno Interessado)

```kotlin
suspend fun createLead(
    name: String,
    email: String,
    phone: String,
    city: String? = null,
    state: String? = null,
    unitId: String? = null,
    marketSegment: String = "gym",
    userType: String = "student",
    objectives: LeadObjectives? = null
): Result<Lead> {
    return try {
        val request = CreateLeadRequest(
            name = name,
            email = email,
            phone = phone,
            city = city,
            state = state,
            unitId = unitId,
            marketSegment = marketSegment,
            userType = userType,
            objectives = objectives
        )
        
        val response = client.post("${EnvironmentConfig.API_BASE_URL}/leads/public") {
            setBody(request)
        }
        
        val json = Json { ignoreUnknownKeys = true }
        val apiResponse: ApiResponse<Lead> = 
            json.decodeFromString(response.bodyAsText())
        
        if (apiResponse.success && apiResponse.data != null) {
            Result.Success(apiResponse.data)
        } else {
            Result.Error(Exception(apiResponse.error ?: "Erro ao criar lead"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}

// Exemplo de uso:
val objectives = LeadObjectives(
    primary = "Quero me matricular na academia",
    secondary = listOf("Melhorar condicionamento físico", "Perder peso"),
    interestedInFranchise = false
)

val result = createLead(
    name = "João Silva",
    email = "joao@example.com",
    phone = "+5511999999999",
    city = "Natal",
    state = "RN",
    unitId = "FR-001", // Obtido do endpoint /franchises/nearby
    marketSegment = "gym",
    userType = "student",
    objectives = objectives
)
```

### Exemplo 6: Criar Lead Público (Nova Academia)

```kotlin
// Para nova academia interessada em se cadastrar
val franchiseObjectives = LeadObjectives(
    primary = "Quero abrir uma nova unidade",
    secondary = listOf("Expandir negócio", "Investir em fitness"),
    interestedInFranchise = true
)

val metadata = mapOf(
    "selectedUnitName" to null,
    "preferredContactTime" to "qualquer horário",
    "howDidYouKnow" to "Google",
    "franchiseType" to "premium",
    "experience" to "Tenho experiência em gestão de academias",
    "budget" to "R$ 200.000 - R$ 500.000",
    "timeToStart" to "3-6 meses"
)

val result = createLead(
    name = "Carlos Oliveira",
    email = "carlos@academiaexemplo.com",
    phone = "+5511977777777",
    city = "Recife",
    state = "PE",
    unitId = null, // Não tem unidade ainda
    marketSegment = "gym",
    userType = "franchise",
    objectives = franchiseObjectives,
    metadata = metadata
)
```

## 🔄 Fluxo de Integração - Captura de Leads

### Fluxo Completo: Buscar Unidade → Criar Lead

1. **Buscar Unidades Próximas:**
   ```
   GET /franchises/nearby?lat={lat}&lng={lng}&marketSegment=gym&radius=10
   ```
   - Retorna unidades ordenadas por distância
   - Usuário seleciona uma unidade

2. **Criar Lead com Unidade Selecionada:**
   ```
   POST /leads/public
   {
     "name": "...",
     "email": "...",
     "phone": "...",
     "unitId": "FR-001", // ID da unidade selecionada
     "marketSegment": "gym",
     "userType": "student",
     "objectives": {
       "primary": "Quero me matricular",
       "secondary": ["Objetivo 1", "Objetivo 2"]
     }
   }
   ```

3. **Benefícios:**
   - Demonstra interesse genuíno (usuário selecionou unidade específica)
   - Facilita segmentação por tipo de usuário (aluno vs. franqueado)
   - Permite rastreamento de origem (qual unidade despertou interesse)
   - Melhora score de qualificação do lead

### Casos de Uso

**Caso 1: Aluno Interessado em Se Matricular**
- `userType: "student"`
- `unitId`: ID da unidade selecionada
- `marketSegment: "gym"`
- `objectives.primary`: Objetivo principal do aluno
- `objectives.interestedInFranchise: false`

**Caso 2: Nova Academia Interessada em Se Cadastrar**
- `userType: "franchise"`
- `unitId: null` (ainda não tem unidade)
- `marketSegment: "gym"`
- `objectives.primary`: Objetivo de abrir nova unidade
- `objectives.interestedInFranchise: true`
- `metadata.franchiseType`, `metadata.budget`, `metadata.experience`: Informações adicionais

## 🔄 Melhorias Futuras

- Adicionar filtros por período customizado na atividade semanal
- Implementar paginação no histórico de check-ins
- Adicionar estatísticas adicionais (média de check-ins por semana, etc.)
- Cache de ranking com invalidação automática
- Adicionar gráficos de progresso no histórico de check-ins
- Endpoint para atualizar lead durante onboarding (PATCH `/leads/public/:id`)

---

## 📞 Contato

Para dúvidas sobre a implementação, consulte:
- Código do backend: `backend-monorepo/nodejs/apis/apps/apis-monorepo/src/modules/`
- Documentação Swagger: `https://api-prd.systentando.com/swagger`
- Documentação Stoplight: `https://api-prd.systentando.com/docs`

---

## ✅ Resumo dos Endpoints

| Endpoint | Método | Status | Descrição |
|----------|--------|--------|-----------|
| `/gamification/students/{studentId}/check-ins` | GET | ✅ Implementado | Histórico de check-ins com streaks |
| `/gamification/students/{studentId}/weekly-activity` | GET | ✅ Implementado | Atividade dos últimos 7 dias |
| `/training-plans` | GET | ✅ Implementado | Lista planos de treino do usuário |
| `/gamification/ranking` | GET | ✅ Implementado | Ranking de usuários por unidade |
| `/leads/public` | POST | ✅ Implementado | Captura de leads públicos (alunos ou academias) |

Todos os endpoints estão prontos para integração com o frontend Android!
