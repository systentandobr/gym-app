# Guia de Build - Tadevolta Gym App

## Pré-requisitos

1. ✅ Gradle Wrapper configurado (já está pronto)
2. ✅ Android SDK instalado (verificado em `local.properties`)
3. ✅ Dependências corrigidas (SQLDelight atualizado)

## Comandos para Gerar Build

### 1. Build Debug (Para Testes)

O build debug é mais rápido e não requer assinatura. Ideal para testes e desenvolvimento.

```bash
# Gerar APK Debug
./gradlew :androidApp:assembleDebug

# Localização do APK gerado:
# androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

**Características do Build Debug:**
- Não é otimizado (maior tamanho)
- Não é ofuscado
- Inclui ferramentas de debug
- Assinado automaticamente com chave de debug

### 2. Build Release (Para Produção)

O build release é otimizado e requer assinatura. Necessário para publicar na Play Store.

#### 2.1. Criar Chave de Assinatura (Primeira Vez)

```bash
# Criar diretório para chaves (se não existir)
mkdir -p androidApp/keystore

# Gerar chave de assinatura
keytool -genkey -v -keystore androidApp/keystore/release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias tadevolta-gym-release

# Você será solicitado a informar:
# - Senha da keystore
# - Senha da chave
# - Nome completo
# - Unidade organizacional
# - Organização
# - Cidade
# - Estado
# - Código do país (ex: BR)
```

#### 2.2. Configurar Assinatura no Gradle

Crie o arquivo `androidApp/keystore.properties` (NÃO commitar no Git):

```properties
storePassword=sua_senha_da_keystore
keyPassword=sua_senha_da_chave
keyAlias=tadevolta-gym-release
storeFile=keystore/release-key.jks
```

Adicione ao `.gitignore`:
```
androidApp/keystore.properties
androidApp/keystore/*.jks
```

#### 2.3. Configurar build.gradle.kts para Release

Adicione ao `androidApp/build.gradle.kts` (antes do bloco `android { ... }`):

```kotlin
// Carregar propriedades da keystore
val keystorePropertiesFile = rootProject.file("androidApp/keystore.properties")
val keystoreProperties = java.util.Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
}
```

E dentro do bloco `android { ... }`, adicione:

```kotlin
signingConfigs {
    create("release") {
        if (keystorePropertiesFile.exists()) {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
}

buildTypes {
    getByName("release") {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
        buildConfigField("String", "API_BASE_URL", "\"https://api.tadevolta.com\"")
        buildConfigField("String", "SYS_SEGURANCA_BASE_URL", "\"https://auth.systentando.com\"")
    }
}
```

#### 2.4. Gerar APK Release

```bash
./gradlew :androidApp:assembleRelease

# Localização do APK gerado:
# androidApp/build/outputs/apk/release/androidApp-release.apk
```

#### 2.5. Gerar AAB (Android App Bundle) - Para Play Store

O formato AAB é o recomendado pela Google Play Store:

```bash
./gradlew :androidApp:bundleRelease

# Localização do AAB gerado:
# androidApp/build/outputs/bundle/release/androidApp-release.aab
```

**Características do Build Release:**
- Otimizado e ofuscado (menor tamanho)
- Assinado com sua chave de produção
- Pronto para publicação

## Comandos Úteis

### Limpar Build Anterior
```bash
./gradlew clean
```

### Verificar Dependências
```bash
./gradlew :androidApp:dependencies
```

### Verificar Tarefas Disponíveis
```bash
./gradlew tasks
```

### Build Completo (Debug + Release)
```bash
./gradlew build
```

### Instalar no Dispositivo Conectado (Debug)
```bash
./gradlew :androidApp:installDebug
```

## Estrutura de Arquivos Gerados

Após o build, os arquivos estarão em:

```
androidApp/build/outputs/
├── apk/
│   ├── debug/
│   │   └── androidApp-debug.apk
│   └── release/
│       └── androidApp-release.apk
└── bundle/
    └── release/
        └── androidApp-release.aab
```

## Troubleshooting

### Erro: "Could not resolve dependencies"
- Verifique sua conexão com a internet
- Execute: `./gradlew --refresh-dependencies`

### Erro: "SDK location not found"
- Verifique se `local.properties` contém: `sdk.dir=/caminho/para/android/sdk`

### Erro: "Keystore file not found"
- Verifique se o caminho em `keystore.properties` está correto
- Use caminho relativo: `keystore/release-key.jks`

### Build muito lento
- Use o daemon do Gradle (padrão): `./gradlew build` (sem `--no-daemon`)
- Primeira compilação sempre é mais lenta

## Próximos Passos Após Build

1. **Testar o APK Debug** em um dispositivo ou emulador
2. **Configurar assinatura** para build release
3. **Testar o APK Release** antes de publicar
4. **Gerar AAB** para upload na Play Store
5. **Configurar CI/CD** (opcional) para builds automáticos

## Notas Importantes

- ⚠️ **NUNCA** commite arquivos de keystore ou senhas no Git
- ✅ Sempre teste o build release antes de publicar
- ✅ Mantenha backup seguro da sua keystore de release
- ✅ Use diferentes keystores para desenvolvimento e produção
