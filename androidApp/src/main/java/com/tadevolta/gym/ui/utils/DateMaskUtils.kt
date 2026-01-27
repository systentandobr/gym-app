package com.tadevolta.gym.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

fun formatDateInput(input: String): String {
    // Extrair apenas dígitos, limitando a 8 dígitos (DDMMAAAA)
    val digits = input.filter { it.isDigit() }.take(8)
    
    return when {
        digits.isEmpty() -> ""
        digits.length <= 2 -> digits
        digits.length <= 4 -> {
            val day = digits.substring(0, 2)
            val month = digits.substring(2)
            "$day/$month"
        }
        else -> {
            val day = digits.substring(0, 2)
            val month = digits.substring(2, 4)
            val year = digits.substring(4)
            "$day/$month/$year"
        }
    }
}

class DateMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text
        // Extrair apenas dígitos do input (ignorar qualquer formatação existente)
        val digits = input.filter { it.isDigit() }.take(8)
        val formatted = formatDateInput(input)
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Contar quantos dígitos existem até a posição offset no texto original
                val clampedOffset = offset.coerceIn(0, input.length)
                var digitCount = 0
                
                for (i in 0 until clampedOffset) {
                    if (input[i].isDigit()) {
                        digitCount++
                    }
                }
                
                // Mapear para a posição no texto formatado
                // Adicionar barras conforme necessário
                return when {
                    digitCount == 0 -> 0
                    digitCount <= 2 -> digitCount
                    digitCount <= 4 -> digitCount + 1 // +1 para a barra após DD (posição 2)
                    else -> digitCount + 2 // +2 para as barras após DD (posição 2) e MM (posição 4)
                }
            }
            
            override fun transformedToOriginal(offset: Int): Int {
                // Contar quantos dígitos existem até a posição offset no texto formatado
                val clampedOffset = offset.coerceIn(0, formatted.length)
                var digitCount = 0
                
                for (i in 0 until clampedOffset) {
                    if (formatted[i].isDigit()) {
                        digitCount++
                    }
                }
                
                // Se não há dígitos, retornar 0
                if (digitCount == 0) {
                    return 0
                }
                
                // Encontrar a posição no texto original que corresponde ao dígito número 'digitCount'
                // Percorrer o texto original e contar os dígitos até encontrar o dígito desejado
                var digitsFound = 0
                for (i in 0 until input.length) {
                    if (input[i].isDigit()) {
                        digitsFound++
                        // Quando encontramos o dígito número 'digitCount', retornar a posição após ele
                        if (digitsFound == digitCount) {
                            return i + 1
                        }
                    }
                }
                
                // Se não encontrou o dígito (cursor está após todos os dígitos),
                // retornar o comprimento do texto original
                return input.length
            }
        }
        
        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}
