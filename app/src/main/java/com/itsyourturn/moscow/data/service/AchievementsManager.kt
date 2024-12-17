package com.itsyourturn.moscow.data.service

import com.itsyourturn.moscow.R
import com.itsyourturn.moscow.data.model.*
import com.itsyourturn.moscow.data.repository.UserRepository
import java.util.concurrent.TimeUnit

class AchievementsManager(
    private val userRepository: UserRepository
) {
    private val achievements = listOf(
        // Distance achievements
        AchievementDefinition(
            id = "distance_10km",
            type = AchievementType.DISTANCE,
            name = "Moscow Beginner",
            description = "Walk 10 kilometers in total",
            iconResId = R.drawable.ic_achievement_distance_1,
            threshold = 10000,
            points = 10
        ),
        AchievementDefinition(
            id = "distance_50km",
            type = AchievementType.DISTANCE,
            name = "Moscow Explorer",
            description = "Walk 50 kilometers in total",
            iconResId = R.drawable.ic_achievement_distance_2,
            threshold = 50000,
            points = 50
        ),
        AchievementDefinition(
            id = "distance_100km",
            type = AchievementType.DISTANCE,
            name = "Moscow Master",
            description = "Walk 100 kilometers in total",
            iconResId = R.drawable.ic_achievement_distance_3,
            threshold = 100000,
            points = 100
        ),

        // Routes completed achievements
        AchievementDefinition(
            id = "routes_10",
            type = AchievementType.ROUTES_COMPLETED,
            name = "Route Enthusiast",
            description = "Complete 10 different routes",
            iconResId = R.drawable.ic_achievement_routes_1,
            threshold = 10,
            points = 20
        ),
        AchievementDefinition(
            id = "routes_50",
            type = AchievementType.ROUTES_COMPLETED,
            name = "Route Master",
            description = "Complete 50 different routes",
            iconResId = R.drawable.ic_achievement_routes_2,
            threshold = 50,
            points = 100
        ),

        // Unique locations achievements
        AchievementDefinition(
            id = "locations_20",
            type = AchievementType.UNIQUE_LOCATIONS,
            name = "Moscow Tourist",
            description = "Visit 20 unique locations",
            iconResId = R.drawable.ic_achievement_locations_1,
            threshold = 20,
            points = 30
        ),
        AchievementDefinition(
            id = "locations_50",
            type = AchievementType.UNIQUE_LOCATIONS,
            name = "Moscow Expert",
            description = "Visit 50 unique locations",
            iconResId = R.drawable.ic_achievement_locations_2,
            threshold = 50,
            points = 75
        ),

        // Weather achievements
        AchievementDefinition(
            id = "weather_snow",
            type = AchievementType.WEATHER_WARRIOR,
            name = "Snow Walker",
            description = "Complete a route in snowy weather",
            iconResId = R.drawable.ic_achievement_weather_1,
            threshold = 1,
            points = 25
        ),
        AchievementDefinition(
            id = "weather_rain",
            type = AchievementType.WEATHER_WARRIOR,
            name = "Rain Walker",
            description = "Complete a route in rainy weather",
            iconResId = R.drawable.ic_achievement_weather_2,
            threshold = 1,
            points = 25
        ),

        // Night walker achievements
        AchievementDefinition(
            id = "night_walker",
            type = AchievementType.NIGHT_WALKER,
            name = "Night Explorer",
            description = "Complete 5 routes at night",
            iconResId = R.drawable.ic_achievement_night,
            threshold = 5,
            points = 50
        ),

        // Streak achievements
        AchievementDefinition(
            id = "streak_7",
            type = AchievementType.STREAK,
            name = "Weekly Warrior",
            description = "Complete routes 7 days in a row",
            iconResId = R.drawable.ic_achievement_streak_1,
            threshold = 7,
            points = 70
        ),
        AchievementDefinition(
            id = "streak_30",
            type = AchievementType.STREAK,
            name = "Monthly Master",
            description = "Complete routes 30 days in a row",
            iconResId = R.drawable.ic_achievement_streak_2,
            threshold = 30,
            points = 300
        )
    )

    suspend fun checkAchievements(userId: String, route: Route) {
        val userProfile = userRepository.getUserProfile(userId) ?: return
        val userStats = userProfile.statistics
        val currentAchievements = userProfile.achievements.associateBy { it.id }

        val newAchievements = mutableListOf<Achievement>()

        // Check distance achievements
        achievements.filter { it.type == AchievementType.DISTANCE }.forEach { achievement ->
            if (!currentAchievements.containsKey(achievement.id) &&
                userStats.totalDistance >= achievement.threshold
            ) {
                newAchievements.add(
                    Achievement(
                        id = achievement.id,
                        name = achievement.name,
                        description = achievement.description,
                        iconUrl = "achievement_${achievement.id}",
                        unlockedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        // Check routes completed achievements
        achievements.filter { it.type == AchievementType.ROUTES_COMPLETED }.forEach { achievement ->
            if (!currentAchievements.containsKey(achievement.id) &&
                userProfile.completedRoutes >= achievement.threshold
            ) {
                newAchievements.add(
                    Achievement(
                        id = achievement.id,
                        name = achievement.name,
                        description = achievement.description,
                        iconUrl = "achievement_${achievement.id}",
                        unlockedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        // Check unique locations achievements
        achievements.filter { it.type == AchievementType.UNIQUE_LOCATIONS }.forEach { achievement ->
            if (!currentAchievements.containsKey(achievement.id) &&
                userStats.uniqueLocationsVisited >= achievement.threshold
            ) {
                newAchievements.add(
                    Achievement(
                        id = achievement.id,
                        name = achievement.name,
                        description = achievement.description,
                        iconUrl = "achievement_${achievement.id}",
                        unlockedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        // Check weather achievements
        if (route.weatherCondition.lowercase().contains("snow") &&
            !currentAchievements.containsKey("weather_snow")
        ) {
            achievements.find { it.id == "weather_snow" }?.let { achievement ->
                newAchievements.add(
                    Achievement(
                        id = achievement.id,
                        name = achievement.name,
                        description = achievement.description,
                        iconUrl = "achievement_${achievement.id}",
                        unlockedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        if (route.weatherCondition.lowercase().contains("rain") &&
            !currentAchievements.containsKey("weather_rain")
        ) {
            achievements.find { it.id == "weather_rain" }?.let { achievement ->
                newAchievements.add(
                    Achievement(
                        id = achievement.id,
                        name = achievement.name,
                        description = achievement.description,
                        iconUrl = "achievement_${achievement.id}",
                        unlockedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        // Update user profile with new achievements
        if (newAchievements.isNotEmpty()) {
            val updatedAchievements = userProfile.achievements + newAchievements
            userRepository.updateUserProfile(userId, mapOf("achievements" to updatedAchievements))
        }
    }

    suspend fun checkStreak(userId: String) {
        val userProfile = userRepository.getUserProfile(userId) ?: return
        val currentAchievements = userProfile.achievements.associateBy { it.id }

        // Calculate current streak
        val routeDates = userProfile.statistics.monthlyDistance.keys
            .map { it.toLong() }
            .sortedDescending()

        var currentStreak = 0
        var previousDate = System.currentTimeMillis()

        for (date in routeDates) {
            if (previousDate - date <= TimeUnit.DAYS.toMillis(1)) {
                currentStreak++
                previousDate = date
            } else {
                break
            }
        }

        // Check streak achievements
        achievements.filter { it.type == AchievementType.STREAK }.forEach { achievement ->
            if (!currentAchievements.containsKey(achievement.id) &&
                currentStreak >= achievement.threshold
            ) {
                val newAchievement = Achievement(
                    id = achievement.id,
                    name = achievement.name,
                    description = achievement.description,
                    iconUrl = "achievement_${achievement.id}",
                    unlockedAt = System.currentTimeMillis()
                )

                val updatedAchievements = userProfile.achievements + newAchievement
                userRepository.updateUserProfile(userId, mapOf("achievements" to updatedAchievements))
            }
        }
    }
}
