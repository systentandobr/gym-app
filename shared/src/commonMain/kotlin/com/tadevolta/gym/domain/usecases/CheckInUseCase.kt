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
                // O backend já calcula e adiciona os pontos automaticamente no check-in
                // O objeto CheckIn retornado já contém o campo 'points' com os pontos calculados
                // incluindo bônus de streak, então não é necessário adicionar pontos separadamente
                
                result
            }
            else -> result
        }
    }
}
