package com.tadevolta.gym.utils.gamification

/**
 * Calcula pontos baseado em diferentes ações do usuário
 */
object PointsCalculator {
    // Pontos base por ação
    const val CHECK_IN_POINTS = 10
    const val EXERCISE_COMPLETE_BASE = 5
    const val EXERCISE_COMPLETE_MAX = 20
    const val TRAINING_PLAN_COMPLETE = 50
    const val WEEK_STREAK_BONUS = 100
    const val MONTH_STREAK_BONUS = 500
    
    /**
     * Calcula pontos por check-in
     */
    fun calculateCheckInPoints(streak: Int): Int {
        var points = CHECK_IN_POINTS
        
        // Bônus por streak
        when {
            streak >= 30 -> points += MONTH_STREAK_BONUS
            streak >= 7 -> points += WEEK_STREAK_BONUS
        }
        
        return points
    }
    
    /**
     * Calcula pontos por completar um exercício
     */
    fun calculateExercisePoints(difficulty: String?): Int {
        return when (difficulty?.lowercase()) {
            "beginner" -> EXERCISE_COMPLETE_BASE
            "intermediate" -> EXERCISE_COMPLETE_BASE + 5
            "advanced" -> EXERCISE_COMPLETE_MAX
            else -> EXERCISE_COMPLETE_BASE
        }
    }
    
    /**
     * Calcula pontos por completar um plano de treino
     */
    fun calculateTrainingPlanPoints(): Int {
        return TRAINING_PLAN_COMPLETE
    }
}
