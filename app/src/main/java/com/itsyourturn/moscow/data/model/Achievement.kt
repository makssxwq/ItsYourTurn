package com.itsyourturn.moscow.data.model

enum class AchievementType {
    DISTANCE,
    ROUTES_COMPLETED,
    UNIQUE_LOCATIONS,
    WEATHER_WARRIOR,
    NIGHT_WALKER,
    STREAK,
    SOCIAL
}

data class AchievementDefinition(
    val id: String,
    val type: AchievementType,
    val name: String,
    val description: String,
    val iconResId: Int,
    val threshold: Int,
    val points: Int
)

data class UserAchievement(
    val definitionId: String,
    val progress: Int,
    val isUnlocked: Boolean,
    val unlockedAt: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
