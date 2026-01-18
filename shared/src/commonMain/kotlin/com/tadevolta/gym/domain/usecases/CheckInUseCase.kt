package com.tadevolta.gym.domain.usecases

import com.tadevolta.gym.data.models.CheckIn
import com.tadevolta.gym.data.models.Location
import com.tadevolta.gym.data.models.Result
import com.tadevolta.gym.data.remote.CheckInService
import com.tadevolta.gym.data.remote.GamificationService
import com.tadevolta.gym.utils.gamification.PointsCalculator

class CheckInUseCase(
    private val checkInService: CheckInService,
    private val gamificationService: GamificationService,
    private val getCurrentStreak: suspend (String) -> Int
) {
    suspend operator fun invoke(
        studentId: String,
        location: Location?
    ): Result<CheckIn> {
        return when (val result = checkInService.checkIn(studentId, location)) {
            is Result.Success -> {
                // Atualizar gamificação (pontos por check-in)
                val streak = getCurrentStreak(studentId)
                val points = PointsCalculator.calculateCheckInPoints(streak)
                // TODO: Adicionar pontos via gamificationService
                
                result
            }
            else -> result
        }
    }
}
