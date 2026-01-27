package com.tadevolta.gym.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

fun formatCpfInput(input: String): String {
    // Extrair apenas dígitos, limitando a 11 dígitos (CPF)
    val digits = input.filter { it.isDigit() }.take(11)
    
    return when {
        digits.isEmpty() -> ""
        digits.length <= 3 -> digits
        digits.length <= 6 -> {
            val part1 = digits.substring(0, 3)
            val part2 = digits.substring(3)
            "$part1.$part2"
        }
        digits.length <= 9 -> {
            val part1 = digits.substring(0, 3)
            val part2 = digits.substring(3, 6)
            val part3 = digits.substring(6)
            "$part1.$part2.$part3"
        }
        else -> {
            val part1 = digits.substring(0, 3)
            val part2 = digits.substring(3, 6)
            val part3 = digits.substring(6, 9)
            val part4 = digits.substring(9)
            "$part1.$part2.$part3-$part4"
        }
    }
}

class CpfMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text
        // Extrair apenas dígitos do input (ignorar qualquer formatação existente)
        val digits = input.filter { it.isDigit() }.take(11)
        val formatted = formatCpfInput(input)
        
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
                // Adicionar pontos e hífen conforme necessário
                return when {
                    digitCount == 0 -> 0
                    digitCount <= 3 -> digitCount
                    digitCount <= 6 -> digitCount + 1 // +1 para o ponto após os 3 primeiros dígitos
                    digitCount <= 9 -> digitCount + 2 // +2 para os dois pontos
                    else -> digitCount + 3 // +3 para os dois pontos e o hífen
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
