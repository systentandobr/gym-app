# Tadevolta Gym App - Android KMP

Aplicativo Android nativo usando Kotlin Multiplatform para alunos de academia.

## Estrutura do Projeto

```
tadevolta-gym-app/
├── shared/              # Código compartilhado KMP
│   ├── commonMain/      # Código comum (data, domain, presentation)
│   └── androidMain/     # Implementações Android específicas
├── androidApp/          # Aplicativo Android
└── build.gradle.kts    # Configuração do projeto
```

## Configuração

1. Copie `local.properties.example` para `local.properties` e configure as variáveis:
```properties
API_BASE_URL=https://api.tadevolta.com
SYS_SEGURANCA_API_KEY=sk_your_api_key_here
SYS_SEGURANCA_BASE_URL=https://auth.systentando.com
ENVIRONMENT=development
```

2. Execute o projeto:
```bash
./gradlew :androidApp:assembleDebug
```

## Arquitetura

- **Clean Architecture**: Separação em camadas (data, domain, presentation)
- **MVVM**: ViewModels compartilhados
- **KMP**: Código compartilhado entre plataformas
- **SQLDelight**: Banco de dados local
- **Ktor**: Cliente HTTP
- **Jetpack Compose**: UI moderna

## Funcionalidades

- Autenticação via SYS-SEGURANÇA
- Visualização de planos de treino
- Execução de exercícios com registro de séries
- Check-in na academia (10/365)
- Gamificação e ranking
- Compartilhamento de progresso
- Suporte offline
