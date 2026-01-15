# Como Instalar o App no Dispositivo USB

## Pré-requisitos

1. **Habilitar Depuração USB no dispositivo Android:**
   - Vá em **Configurações** > **Sobre o telefone**
   - Toque 7 vezes em **Número da compilação** para ativar o modo desenvolvedor
   - Volte para **Configurações** > **Opções do desenvolvedor**
   - Ative **Depuração USB**

2. **Conectar o dispositivo via USB:**
   - Conecte o cabo USB ao computador
   - No dispositivo, aceite a solicitação de depuração USB (se aparecer)

3. **Verificar se o dispositivo está conectado:**
   ```bash
   adb devices
   ```
   Você deve ver algo como:
   ```
   List of devices attached
   ABC123XYZ    device
   ```

## Instalação

### Opção 1: Usando o script automatizado (Recomendado)

**Linux/WSL:**
```bash
./instalar-device.sh
```

**Windows:**
```cmd
instalar-device.bat
```

Este script:
- Verifica se há dispositivo conectado
- Compila o módulo `shared`
- Compila o módulo `androidApp`
- Gera o APK debug
- Instala automaticamente no dispositivo conectado

### Opção 2: Build e Instalação em um comando

**Linux/WSL:**
```bash
./gradlew :shared:build :androidApp:installDebug
```

**Windows:**
```cmd
gradlew.bat :shared:build :androidApp:installDebug
```

Este comando:
- Compila o módulo `shared`
- Compila o módulo `androidApp`
- Gera o APK debug
- Instala automaticamente no dispositivo conectado

### Opção 3: Build separado e instalação

**Linux/WSL:**
```bash
# 1. Gerar o APK
./gradlew :shared:build :androidApp:assembleDebug

# 2. Instalar no dispositivo
./gradlew :androidApp:installDebug
```

**Windows:**
```cmd
REM 1. Gerar o APK
gradlew.bat :shared:build :androidApp:assembleDebug

REM 2. Instalar no dispositivo
gradlew.bat :androidApp:installDebug
```

### Opção 4: Instalação manual do APK

**Linux/WSL:**
```bash
# 1. Gerar o APK
./gradlew :shared:build :androidApp:assembleDebug

# 2. Localizar o APK gerado
# O APK estará em: androidApp/build/outputs/apk/debug/androidApp-debug.apk

# 3. Instalar via ADB
adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Ou instalar com substituição (se já estiver instalado)
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

**Windows:**
```cmd
REM 1. Gerar o APK
gradlew.bat :shared:build :androidApp:assembleDebug

REM 2. Localizar o APK gerado
REM O APK estará em: androidApp\build\outputs\apk\debug\androidApp-debug.apk

REM 3. Instalar via ADB
adb install androidApp\build\outputs\apk\debug\androidApp-debug.apk

REM Ou instalar com substituição (se já estiver instalado)
adb install -r androidApp\build\outputs\apk\debug\androidApp-debug.apk
```

## Comandos Úteis

### Verificar dispositivos conectados

**Linux/WSL:**
```bash
# Baseado no seu local.properties (sdk.dir=/home/marcelio/android)
/home/marcelio/android/platform-tools/adb devices

# Ou adicione ao PATH temporariamente:
export PATH=$PATH:/home/marcelio/android/platform-tools
adb devices

# Ou permanentemente no ~/.zshrc ou ~/.bashrc:
echo 'export PATH=$PATH:/home/marcelio/android/platform-tools' >> ~/.zshrc
source ~/.zshrc
```

**Windows:**
```cmd
REM Caminhos comuns do ADB no Windows:
REM %LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
REM %USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe

REM Verificar dispositivos:
adb devices

REM Se não estiver no PATH, use o caminho completo:
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" devices
```

**O ADB geralmente está em:**
- Linux: `~/Android/Sdk/platform-tools/adb` ou `~/android/platform-tools/adb`
- macOS: `~/Library/Android/sdk/platform-tools/adb`
- Windows: `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`

### Ver logs do aplicativo em tempo real

**Linux/WSL:**
```bash
adb logcat | grep -i "tadevolta\|gym"
```

**Windows:**
```cmd
adb logcat | findstr /i "tadevolta gym"
```

### Ver logs filtrados por tag

**Linux/WSL:**
```bash
adb logcat -s TadevoltaGym:D *:S
```

**Windows:**
```cmd
adb logcat -s TadevoltaGym:D *:S
```

### Desinstalar o app
```bash
# Linux/WSL/Windows (comando é o mesmo)
adb uninstall com.tadevolta.gym
```

### Limpar dados do app
```bash
# Linux/WSL/Windows (comando é o mesmo)
adb shell pm clear com.tadevolta.gym
```

### Reiniciar o app
```bash
# Linux/WSL/Windows (comando é o mesmo)
adb shell am force-stop com.tadevolta.gym
adb shell am start -n com.tadevolta.gym/.MainActivity
```

## Troubleshooting

### Dispositivo não aparece no `adb devices`

1. **Verificar se o USB está conectado:**

   **Linux/WSL:**
   ```bash
   lsusb
   ```
   Deve aparecer o dispositivo Android

   **Windows:**
   ```cmd
   REM Verificar no Gerenciador de Dispositivos
   REM Win + X > Gerenciador de Dispositivos
   ```

2. **Reiniciar o servidor ADB:**
   ```bash
   # Linux/WSL/Windows (comando é o mesmo)
   adb kill-server
   adb start-server
   adb devices
   ```

3. **Verificar permissões USB (Linux/WSL):**
   ```bash
   # Adicionar regra udev (se necessário)
   sudo nano /etc/udev/rules.d/51-android.rules
   # Adicionar: SUBSYSTEM=="usb", ATTR{idVendor}=="XXXX", MODE="0666", GROUP="plugdev"
   sudo udevadm control --reload-rules
   ```

4. **Verificar drivers USB (Windows):**
   - Instalar drivers USB do fabricante do dispositivo
   - Ou usar drivers genéricos do Google: https://developer.android.com/studio/run/oem-usb

5. **Verificar se a depuração USB está ativada no dispositivo**

### Erro "INSTALL_FAILED_INSUFFICIENT_STORAGE"

- Libere espaço no dispositivo
- Ou instale em um dispositivo com mais espaço

### Erro "INSTALL_FAILED_UPDATE_INCOMPATIBLE"

- Desinstale a versão anterior primeiro:
  ```bash
  adb uninstall com.tadevolta.gym
  ```

### App não abre após instalação

- Verifique os logs:
  ```bash
  adb logcat | grep -i "androidruntime\|fatal"
  ```

## Desenvolvimento Contínuo

Para desenvolvimento ativo, você pode usar:

```bash
# Watch mode - recompila automaticamente quando há mudanças
./gradlew :androidApp:installDebug --continuous
```

Ou configure o Android Studio para:
1. **Run** > **Edit Configurations**
2. Marque **Deploy APK from app bundle** ou use **USB debugging**
3. Selecione o dispositivo na lista

## Build Release (Para Produção)

Quando estiver pronto para gerar um build de produção:

```bash
# Gerar APK Release (requer assinatura configurada)
./gradlew :androidApp:assembleRelease

# Gerar AAB (Android App Bundle) para Play Store
./gradlew :androidApp:bundleRelease
```

**Nota:** Builds release requerem configuração de assinatura. Veja `GUIA_BUILD.md` para mais detalhes.
