package com.itsyourturn.moscow.data.model

import java.util.Date

data class RouteReview(
    val id: String = "",
    val routeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val photos: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val likes: Int = 0,
    val likedBy: List<String> = emptyList()
)

data class RoutePhoto(
    val id: String = "",
    val routeId: String = "",
    val userId: String = "",
    val photoUrl: String = "",
    val description: String = "",
    val location: com.google.firebase.firestore.GeoPoint? = null,
    val createdAt: Date = Date(),
    val likes: Int = 0,
    val likedBy: List<String> = emptyList()
)

data class SocialActivity(
    val id: String = "",
    val type: ActivityType = ActivityType.ROUTE_COMPLETED,
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val createdAt: Date = Date(),
    val metadata: Map<String, Any> = emptyMap()
)

enum class ActivityType {
    ROUTE_COMPLETED,
    ACHIEVEMENT_UNLOCKED,
    ROUTE_REVIEWED,
    PHOTO_UPLOADED,
    ROUTE_LIKED,
    REVIEW_LIKED
}

data class UserStats(
    val totalRoutesCompleted: Int = 0,
    val totalDistance: Double = 0.0,
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val totalPhotos: Int = 0,
    val achievements: Int = 0,
    val contributionScore: Int = 0
)
