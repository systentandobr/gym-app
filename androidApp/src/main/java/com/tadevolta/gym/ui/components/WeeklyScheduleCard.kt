package com.tadevolta.gym.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tadevolta.gym.data.models.WeeklySchedule

@Composable
fun WeeklyScheduleCard(
    schedule: WeeklySchedule,
    onExerciseClick: (com.tadevolta.gym.data.models.Exercise) -> Unit
) {
    val dayNames = listOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dayNames.getOrElse(schedule.dayOfWeek) { "Dia ${schedule.dayOfWeek}" },
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            schedule.exercises.forEach { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
