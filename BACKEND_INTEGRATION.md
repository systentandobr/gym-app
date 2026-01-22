# Documentação de Integração Backend - Novos Recursos

Este documento descreve os novos recursos implementados no frontend que precisam ser desenvolvidos no backend para integração completa.

## 📋 Índice

1. [Autenticação - Cadastro de Usuário](#1-autenticação---cadastro-de-usuário)
2. [Bioimpedância](#2-bioimpedância)
3. [Ranking e Gamificação](#3-ranking-e-gamificação)
4. [Perfil do Usuário](#4-perfil-do-usuário)

---

## 1. Autenticação - Cadastro de Usuário

### 1.1 Endpoint: POST `https://auth.systentando.com/auth/register`

**Descrição:** Registra um novo usuário no sistema.

**Request Body:**
```json
{
  "name": "string",
  "email": "string",
  "password": "string",
  "confirmPassword": "string",
  "domain": "tadevolta-gym-app"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "string",
      "name": "string",
      "email": "string",
      "role": "string",
      "unitId": "string | null",
      "avatar": "string | null",
      "phone": "string | null",
      "status": "ACTIVE | INACTIVE | PENDING | SUSPENDED",
      "emailVerified": false,
      "createdAt": "string",
      "updatedAt": "string"
    },
    "tokens": {
      "token": "string",
      "refreshToken": "string",
      "expiresAt": 1234567890
    }
  },
  "error": null
}
```

**Validações Necessárias:**
- Email deve ser válido e único
- Senha deve ter no mínimo 6 caracteres
- `password` e `confirmPassword` devem ser iguais
- Nome não pode estar vazio

**Erros Possíveis:**
- `400 Bad Request`: Dados inválidos ou senhas não coincidem
- `409 Conflict`: Email já cadastrado
- `500 Internal Server Error`: Erro no servidor

**Notas:**
- O endpoint deve criar o usuário e retornar os tokens de autenticação automaticamente (login automático após cadastro)
- Se integrado com SYS-SEGURANÇA, usar o endpoint `${SYS_SEGURANCA_BASE_URL}/auth/register` com header `X-API-Key`

---

## 2. Bioimpedância

### 2.1 Endpoint: GET `/students/{studentId}/bioimpedance/history`

**Descrição:** Retorna o histórico de avaliações de bioimpedância do aluno.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "measurements": [
      {
        "id": "string",
        "studentId": "string",
        "date": "2023-10-15T00:00:00Z",
        "weight": 78.5,
        "bodyFat": 14.2,
        "muscle": 42.1,
        "isBestRecord": true
      }
    ]
  },
  "error": null
}
```

**Ordenação:** Mais recente primeiro

**Notas:**
- O campo `isBestRecord` deve ser calculado baseado no menor percentual de gordura corporal
- Apenas uma avaliação pode ter `isBestRecord: true` por aluno

---

### 2.2 Endpoint: GET `/students/{studentId}/bioimpedance/progress`

**Descrição:** Retorna os dados de progresso para o gráfico de evolução.

**Query Parameters:**
- `period` (string, opcional): Período de análise. Valores: "6 meses", "1 ano", "todo período". Default: "6 meses"

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "period": "6 meses",
    "title": "Progresso Galáctico",
    "weightData": [
      {
        "month": "MAI",
        "value": 83.4
      },
      {
        "month": "JUN",
        "value": 81.2
      },
      {
        "month": "JUL",
        "value": 80.5
      },
      {
        "month": "AGO",
        "value": 79.8
      },
      {
        "month": "SET",
        "value": 79.2
      },
      {
        "month": "OUT",
        "value": 78.5
      }
    ],
    "bodyFatData": [
      {
        "month": "MAI",
        "value": 18.5
      },
      {
        "month": "JUN",
        "value": 17.2
      },
      {
        "month": "JUL",
        "value": 16.8
      },
      {
        "month": "AGO",
        "value": 15.5
      },
      {
        "month": "SET",
        "value": 14.8
      },
      {
        "month": "OUT",
        "value": 14.2
      }
    ]
  },
  "error": null
}
```

**Notas:**
- Os meses devem ser abreviados em português: JAN, FEV, MAR, ABR, MAI, JUN, JUL, AGO, SET, OUT, NOV, DEZ
- Os valores devem ser calculados como média mensal quando houver múltiplas avaliações no mesmo mês
- Se não houver dados suficientes, retornar array vazio

---

### 2.3 Endpoint: POST `/students/{studentId}/bioimpedance`

**Descrição:** Cria uma nova avaliação de bioimpedância.

**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "id": "string (opcional, gerado pelo backend)",
  "studentId": "string",
  "date": "2023-10-15T00:00:00Z",
  "weight": 78.5,
  "bodyFat": 14.2,
  "muscle": 42.1,
  "isBestRecord": false
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "string",
    "studentId": "string",
    "date": "2023-10-15T00:00:00Z",
    "weight": 78.5,
    "bodyFat": 14.2,
    "muscle": 42.1,
    "isBestRecord": true
  },
  "error": null
}
```

**Validações Necessárias:**
- `weight` > 0
- `bodyFat` entre 0 e 100
- `muscle` > 0
- `date` não pode ser no futuro
- Após criar, verificar se esta avaliação é a melhor marca e atualizar `isBestRecord` em todas as avaliações do aluno

**Erros Possíveis:**
- `400 Bad Request`: Dados inválidos
- `401 Unauthorized`: Token inválido ou expirado
- `404 Not Found`: Aluno não encontrado
- `500 Internal Server Error`: Erro no servidor

---

## 3. Ranking e Gamificação

### 3.1 Endpoint: GET `/gamification/ranking`

**Descrição:** Retorna o ranking de usuários por unidade.

**Query Parameters:**
- `unitId` (string, obrigatório): ID da unidade
- `limit` (number, opcional): Número máximo de resultados. Default: 50

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "position": 1,
      "totalPoints": 4200,
      "level": 15,
      "unitId": "string",
      "unitName": "Unidade Centro",
      "userId": "string",
      "userName": "Beatriz L."
    },
    {
      "position": 2,
      "totalPoints": 3800,
      "level": 14,
      "unitId": "string",
      "unitName": "Unidade Centro",
      "userId": "string",
      "userName": "Lucas R."
    }
  ],
  "error": null
}
```

**Ordenação:** Por `totalPoints` descendente, depois por `level` descendente

**Notas:**
- O ranking deve ser calculado em tempo real ou atualizado periodicamente
- A posição deve ser calculada dinamicamente baseada nos pontos totais
- Incluir apenas usuários ativos

---

### 3.2 Endpoint: GET `/gamification/users/{userId}`

**Descrição:** Retorna os dados de gamificação do usuário.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "userId": "string",
    "totalPoints": 3100,
    "level": 12,
    "xp": 850,
    "xpToNextLevel": 1000,
    "achievements": [
      {
        "id": "string",
        "name": "PRIMEIRA ÓRBITA",
        "description": "Complete seu primeiro treino",
        "icon": "star",
        "rarity": "COMMON",
        "unlockedAt": "2023-10-01T00:00:00Z"
      }
    ],
    "completedTasks": [],
    "ranking": {
      "position": 3,
      "totalPoints": 3100,
      "level": 12,
      "unitId": "string",
      "unitName": "Unidade Centro",
      "userId": "string",
      "userName": "Alex"
    }
  },
  "error": null
}
```

**Notas:**
- O campo `ranking` deve conter a posição atual do usuário no ranking da sua unidade
- `xp` é a experiência atual do nível atual
- `xpToNextLevel` é a experiência necessária para o próximo nível

---

### 3.3 Endpoint: POST `/gamification/users/{userId}/share`

**Descrição:** Gera uma imagem compartilhável do progresso do usuário.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "imageUrl": "https://api.example.com/shared/progress/user123.png",
    "text": "Estou no nível 12 com 3100 pontos! 🚀",
    "stats": {
      "totalCheckIns": 45,
      "currentStreak": 5,
      "level": 12,
      "totalPoints": 3100,
      "completedWorkouts": 28,
      "completedExercises": 156
    }
  },
  "error": null
}
```

**Notas:**
- A imagem deve ser gerada no backend e armazenada temporariamente
- A URL da imagem deve ser válida por pelo menos 24 horas
- O texto deve ser personalizado com os dados do usuário

---

## 4. Perfil do Usuário

### 4.1 Endpoint: GET `/users/profile`

**Descrição:** Retorna os dados do perfil do usuário autenticado.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "string",
    "name": "Gabriel Silva",
    "email": "gabriel@example.com",
    "role": "STUDENT",
    "unitId": "string",
    "avatar": "https://api.example.com/avatars/user123.jpg",
    "phone": "+5511999999999",
    "status": "ACTIVE",
    "emailVerified": true,
    "createdAt": "2023-01-15T00:00:00Z",
    "updatedAt": "2023-10-15T00:00:00Z"
  },
  "error": null
}
```

**Notas:**
- Este endpoint já deve existir, mas precisa retornar todos os campos listados
- O `avatar` deve ser uma URL completa para a imagem

---

## 📊 Modelos de Dados

### BioimpedanceMeasurement
```typescript
interface BioimpedanceMeasurement {
  id: string;
  studentId: string;
  date: string; // ISO 8601
  weight: number; // kg
  bodyFat: number; // %
  muscle: number; // kg
  isBestRecord: boolean;
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
  userId?: string;
  userName?: string;
}
```

### GamificationData
```typescript
interface GamificationData {
  userId: string;
  totalPoints: number;
  level: number;
  xp: number;
  xpToNextLevel: number;
  achievements: Achievement[];
  completedTasks: string[];
  ranking?: RankingPosition;
}
```

### Achievement
```typescript
interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  rarity: "COMMON" | "RARE" | "EPIC" | "LEGENDARY";
  unlockedAt?: string; // ISO 8601
}
```

---

## 🔐 Autenticação

Todos os endpoints (exceto `/auth/register`) requerem autenticação via Bearer Token:

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

### Prioridades

1. **Alta Prioridade:**
   - POST `/auth/register` - Necessário para cadastro de novos usuários
   - GET `/students/{studentId}/bioimpedance/history` - Histórico de bioimpedância
   - POST `/students/{studentId}/bioimpedance` - Criar nova avaliação

2. **Média Prioridade:**
   - GET `/students/{studentId}/bioimpedance/progress` - Dados para gráfico
   - GET `/gamification/ranking` - Ranking de usuários
   - GET `/gamification/users/{userId}` - Dados de gamificação

3. **Baixa Prioridade:**
   - POST `/gamification/users/{userId}/share` - Compartilhamento de progresso

### Considerações Técnicas

- Todos os endpoints devem seguir o padrão de resposta `ApiResponse<T>`
- Datas devem ser retornadas em formato ISO 8601
- Validações devem ser feitas no backend antes de salvar dados
- Implementar rate limiting para endpoints públicos
- Considerar cache para dados de ranking (atualizar a cada X minutos)

### Testes Recomendados

- Testes unitários para validações
- Testes de integração para fluxos completos
- Testes de performance para ranking com muitos usuários
- Testes de segurança para endpoints de autenticação

---

## 📞 Contato

Para dúvidas sobre a implementação, consulte:
- Código do frontend: `androidApp/src/main/java/com/tadevolta/gym/`
- Modelos de dados: `shared/src/commonMain/kotlin/com/tadevolta/gym/data/models/`
- Serviços: `shared/src/commonMain/kotlin/com/tadevolta/gym/data/remote/`
