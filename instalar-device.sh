#!/bin/bash

# Script para build e instalação no dispositivo USB
# Uso: ./instalar-device.sh

set -e

# Verificar se o dispositivo está conectado
ADB_PATH="/home/marcelio/android/platform-tools/adb"

if [ ! -f "$ADB_PATH" ]; then
    echo "⚠️  ADB não encontrado em $ADB_PATH"
    echo "Verificando se está no PATH..."
    if ! command -v adb &> /dev/null; then
        echo "❌ ADB não encontrado. Por favor, instale o Android SDK Platform Tools."
        exit 1
    fi
    ADB_PATH="adb"
fi

echo "📱 Verificando dispositivos conectados..."
DEVICES=$($ADB_PATH devices | grep -v "List" | grep "device" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ Nenhum dispositivo encontrado!"
    echo "Por favor:"
    echo "  1. Conecte o dispositivo via USB"
    echo "  2. Ative a depuração USB no dispositivo"
    echo "  3. Execute: $ADB_PATH devices"
    exit 1
fi

echo "✅ Dispositivo encontrado!"
echo ""
echo "🔨 Compilando módulo shared..."
./gradlew :shared:build

echo "📱 Compilando e instalando app no dispositivo..."
./gradlew :androidApp:installDebug

echo ""
echo "✅ Instalação concluída!"
echo ""
echo "📋 Comandos úteis:"
echo "  Ver logs: $ADB_PATH logcat | grep -i tadevolta"
echo "  Abrir app: $ADB_PATH shell am start -n com.tadevolta.gym/.MainActivity"
echo "  Desinstalar: $ADB_PATH uninstall com.tadevolta.gym"
