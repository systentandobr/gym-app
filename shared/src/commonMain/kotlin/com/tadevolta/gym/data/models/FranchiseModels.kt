package com.tadevolta.gym.data.models

import kotlinx.serialization.Serializable

@Serializable
data class NearbyFranchise(
    val id: String,
    val unitId: String,
    val name: String,
    val owner: FranchiseOwner,
    val location: FranchiseLocation,
    val status: String,
    val type: String,
    val marketSegments: List<String>,
    val distance: Double, // em km
    val metrics: FranchiseMetrics? = null,
    val territory: FranchiseTerritory? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class FranchiseOwner(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null
)

@Serializable
data class FranchiseLocation(
    val lat: Double,
    val lng: Double,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val type: String // "physical" ou "digital"
)

@Serializable
data class FranchiseTerritory(
    val city: String,
    val state: String,
    val exclusive: Boolean,
    val radius: Double? = null
)

@Serializable
data class FranchiseMetrics(
    val totalOrders: Int? = null,
    val totalSales: Double? = null,
    val totalLeads: Int? = null,
    val conversionRate: Double? = null,
    val averageTicket: Double? = null,
    val customerCount: Int? = null,
    val growthRate: Double? = null,
    val lastMonthSales: Double? = null,
    val lastMonthOrders: Int? = null,
    val lastMonthLeads: Int? = null
)

// Modelo simplificado para uso na UI
data class UnitItem(
    val id: String,
    val unitId: String,
    val name: String,
    val distance: String, // Formatado como "5.2 km"
    val address: String,
    val city: String,
    val state: String,
    val tags: List<String> = emptyList()
) {
    companion object {
        fun fromNearbyFranchise(franchise: NearbyFranchise): UnitItem {
            return UnitItem(
                id = franchise.id,
                unitId = franchise.unitId,
                name = franchise.name,
                distance = formatDistance(franchise.distance),
                address = franchise.location.address,
                city = franchise.location.city,
                state = franchise.location.state,
                tags = franchise.marketSegments
            )
        }
        
        private fun formatDistance(km: Double): String {
            return when {
                km < 1 -> "${(km * 1000).toInt()} m"
                else -> String.format("%.1f km", km)
            }
        }
    }
}
