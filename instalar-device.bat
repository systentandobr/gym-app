@echo off
REM Script para build e instalação no dispositivo USB (Windows)
REM Uso: instalar-device.bat

setlocal enabledelayedexpansion

REM Verificar se o Gradle está disponível
where gradlew.bat >nul 2>&1
if errorlevel 1 (
    echo Erro: gradlew.bat nao encontrado!
    echo Certifique-se de estar no diretorio do projeto.
    exit /b 1
)

REM Tentar encontrar o ADB
set ADB_PATH=
if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
) else if exist "%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
    set ADB_PATH=%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe
) else if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
    set ADB_PATH=C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe
) else (
    REM Tentar usar ADB do PATH
    where adb >nul 2>&1
    if errorlevel 1 (
        echo Aviso: ADB nao encontrado nos locais padrao.
        echo Tentando usar ADB do PATH...
        set ADB_PATH=adb
    ) else (
        set ADB_PATH=adb
    )
)

echo Verificando dispositivos conectados...
"%ADB_PATH%" devices | findstr /R "device$" >nul
if errorlevel 1 (
    echo.
    echo ERRO: Nenhum dispositivo encontrado!
    echo.
    echo Por favor:
    echo   1. Conecte o dispositivo via USB
    echo   2. Ative a depuracao USB no dispositivo
    echo   3. Execute: "%ADB_PATH%" devices
    echo.
    pause
    exit /b 1
)

echo Dispositivo encontrado!
echo.

echo Compilando modulo shared...
call gradlew.bat :shared:build
if errorlevel 1 (
    echo.
    echo ERRO: Falha ao compilar modulo shared!
    pause
    exit /b 1
)

echo.
echo Compilando e instalando app no dispositivo...
call gradlew.bat :androidApp:installDebug
if errorlevel 1 (
    echo.
    echo ERRO: Falha ao instalar o app!
    pause
    exit /b 1
)

echo.
echo Instalacao concluida!
echo.
echo Comandos uteis:
echo   Ver logs: "%ADB_PATH%" logcat ^| findstr /i tadevolta
echo   Abrir app: "%ADB_PATH%" shell am start -n com.tadevolta.gym/.MainActivity
echo   Desinstalar: "%ADB_PATH%" uninstall com.tadevolta.gym
echo.
pause
