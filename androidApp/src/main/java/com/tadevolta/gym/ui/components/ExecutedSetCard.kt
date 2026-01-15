package com.tadevolta.gym.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tadevolta.gym.data.models.ExecutedSet

@Composable
fun ExecutedSetCard(
    setNumber: Int,
    plannedReps: String,
    plannedWeight: Double?,
    executedSet: ExecutedSet?,
    onSetComplete: (ExecutedSet) -> Unit,
    onWeightChange: (ExecutedSet, Double) -> Unit,
    onRepsChange: (ExecutedSet, Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Série $setNumber",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Planejado: $plannedReps")
            plannedWeight?.let {
                Text("Peso: ${it}kg")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Inputs para execução
            OutlinedTextField(
                value = executedSet?.executedReps?.toString() ?: "",
                onValueChange = { value ->
                    value.toIntOrNull()?.let { reps ->
                        executedSet?.let { set ->
                            onRepsChange(set, reps)
                        }
                    }
                },
                label = { Text("Repetições executadas") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = executedSet?.executedWeight?.toString() ?: "",
                onValueChange = { value ->
                    value.toDoubleOrNull()?.let { weight ->
                        executedSet?.let { set ->
                            onWeightChange(set, weight)
                        }
                    }
                },
                label = { Text("Peso executado (kg)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Checkbox(
                checked = executedSet?.completed ?: false,
                onCheckedChange = { checked ->
                    executedSet?.let { set ->
                        onSetComplete(set.copy(completed = checked))
                    }
                }
            )
        }
    }
}
