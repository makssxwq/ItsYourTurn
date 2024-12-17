package com.itsyourturn.moscow.data.model

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val totalDistance: Double = 0.0,
    val completedRoutes: Int = 0,
    val favoriteRoutes: List<String> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val statistics: UserStatistics = UserStatistics()
)

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val unlockedAt: Long = 0
)

data class UserStatistics(
    val totalTimeSpent: Long = 0, // in minutes
    val averageSpeed: Double = 0.0,
    val favoriteRouteTypes: Map<String, Int> = emptyMap(),
    val monthlyDistance: Map<String, Double> = emptyMap(),
    val uniqueLocationsVisited: Int = 0
)
