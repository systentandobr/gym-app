package com.tadevolta.gym.utils.gamification

object LevelSystem {
    /**
     * Calcula o nível baseado na XP total
     * Baseado no sistema web:
     * Nível 1: 0-100 XP
     * Nível 2: 101-300 XP
     * Nível 3: 301-600 XP
     * Nível 4: 601-1000 XP
     * Nível 5: 1001-1500 XP
     * Nível 6: 1501-2100 XP
     * Nível 7: 2101-2800 XP
     * Nível 8: 2801-3600 XP
     * Nível 9: 3601-4500 XP
     * Nível 10: 4501+ XP
     */
    fun getLevel(xp: Int): Int {
        return when {
            xp < 100 -> 1
            xp < 300 -> 2
            xp < 600 -> 3
            xp < 1000 -> 4
            xp < 1500 -> 5
            xp < 2100 -> 6
            xp < 2800 -> 7
            xp < 3600 -> 8
            xp < 4500 -> 9
            else -> 10
        }
    }
    
    /**
     * Retorna a XP necessária para um nível específico
     */
    fun getXpForLevel(level: Int): Int {
        return when (level) {
            1 -> 0
            2 -> 100
            3 -> 300
            4 -> 600
            5 -> 1000
            6 -> 1500
            7 -> 2100
            8 -> 2800
            9 -> 3600
            10 -> 4500
            else -> if (level > 10) 4500 + (level - 10) * 500 else 0
        }
    }
    
    /**
     * Calcula a XP necessária para o próximo nível
     */
    fun getXpToNextLevel(currentXp: Int): Int {
        val currentLevel = getLevel(currentXp)
        val nextLevelXp = getXpForLevel(currentLevel + 1)
        return nextLevelXp - currentXp
    }
    
    /**
     * Calcula o progresso percentual para o próximo nível
     */
    fun getProgressToNextLevel(currentXp: Int): Float {
        val currentLevel = getLevel(currentXp)
        val currentLevelXp = getXpForLevel(currentLevel)
        val nextLevelXp = getXpForLevel(currentLevel + 1)
        val xpInCurrentLevel = currentXp - currentLevelXp
        val xpNeededForNextLevel = nextLevelXp - currentLevelXp
        
        return if (xpNeededForNextLevel > 0) {
            (xpInCurrentLevel.toFloat() / xpNeededForNextLevel).coerceIn(0f, 1f)
        } else {
            1f
        }
    }
}
