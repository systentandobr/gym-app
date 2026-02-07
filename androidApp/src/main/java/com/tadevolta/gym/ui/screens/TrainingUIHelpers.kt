package com.tadevolta.gym.ui.screens

import java.util.Calendar

// Função helper para obter o dia atual da semana (0-6, onde 0=Domingo)
fun getCurrentDayOfWeek(): Int {
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    return (dayOfWeek - 1) % 7
}

// Função helper para obter o nome do dia da semana
fun getDayName(dayOfWeek: Int): String {
    val dayNames = listOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
    return dayNames.getOrElse(dayOfWeek) { "Dia $dayOfWeek" }
}

// Função helper para obter o sufixo do plano (A, B, C...)
fun getDaySuffix(dayOfWeek: Int): String {
    return when(dayOfWeek) {
        1 -> "A" // Segunda
        2 -> "B" // Terça
        3 -> "C" // Quarta
        4 -> "D" // Quinta
        5 -> "E" // Sexta
        else -> ""
    }
}
