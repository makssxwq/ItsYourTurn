package com.itsyourturn.moscow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.itsyourturn.moscow.data.model.*
import kotlinx.coroutines.tasks.await
import java.util.*

class SocialRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private val reviewsCollection = db.collection("reviews")
    private val photosCollection = db.collection("photos")
    private val activitiesCollection = db.collection("activities")
    private val statsCollection = db.collection("user_stats")

    suspend fun addRouteReview(review: RouteReview) {
        val reviewId = reviewsCollection.document().id
        val reviewWithId = review.copy(id = reviewId)
        
        reviewsCollection.document(reviewId).set(reviewWithId).await()
        
        // Update route rating
        updateRouteRating(review.routeId)
        
        // Create activity
        addActivity(
            SocialActivity(
                type = ActivityType.ROUTE_REVIEWED,
                userId = review.userId,
                userName = review.userName,
                userPhotoUrl = review.userPhotoUrl,
                routeId = review.routeId,
                metadata = mapOf(
                    "rating" to review.rating,
                    "comment" to review.comment
                )
            )
        )
    }

    suspend fun addRoutePhoto(photo: RoutePhoto) {
        val photoId = photosCollection.document().id
        val photoWithId = photo.copy(id = photoId)
        
        photosCollection.document(photoId).set(photoWithId).await()
        
        // Create activity
        addActivity(
            SocialActivity(
                type = ActivityType.PHOTO_UPLOADED,
                userId = photo.userId,
                routeId = photo.routeId,
                metadata = mapOf(
                    "photoUrl" to photo.photoUrl,
                    "description" to photo.description
                )
            )
        )
    }

    suspend fun getRouteReviews(routeId: String): List<RouteReview> {
        return reviewsCollection
            .whereEqualTo("routeId", routeId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(RouteReview::class.java)
    }

    suspend fun getRoutePhotos(routeId: String): List<RoutePhoto> {
        return photosCollection
            .whereEqualTo("routeId", routeId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(RoutePhoto::class.java)
    }

    suspend fun getUserActivities(userId: String): List<SocialActivity> {
        return activitiesCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(SocialActivity::class.java)
    }

    suspend fun getFeedActivities(): List<SocialActivity> {
        return activitiesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
            .toObjects(SocialActivity::class.java)
    }

    suspend fun likeReview(reviewId: String, userId: String) {
        val review = reviewsCollection.document(reviewId).get().await()
            .toObject(RouteReview::class.java) ?: return

        val likedBy = review.likedBy.toMutableList()
        if (userId !in likedBy) {
            likedBy.add(userId)
            reviewsCollection.document(reviewId).update(
                mapOf(
                    "likes" to review.likes + 1,
                    "likedBy" to likedBy
                )
            ).await()

            addActivity(
                SocialActivity(
                    type = ActivityType.REVIEW_LIKED,
                    userId = userId,
                    routeId = review.routeId,
                    metadata = mapOf(
                        "reviewId" to reviewId,
                        "reviewUserId" to review.userId
                    )
                )
            )
        }
    }

    suspend fun likePhoto(photoId: String, userId: String) {
        val photo = photosCollection.document(photoId).get().await()
            .toObject(RoutePhoto::class.java) ?: return

        val likedBy = photo.likedBy.toMutableList()
        if (userId !in likedBy) {
            likedBy.add(userId)
            photosCollection.document(photoId).update(
                mapOf(
                    "likes" to photo.likes + 1,
                    "likedBy" to likedBy
                )
            ).await()
        }
    }

    private suspend fun updateRouteRating(routeId: String) {
        val reviews = getRouteReviews(routeId)
        if (reviews.isNotEmpty()) {
            val averageRating = reviews.map { it.rating }.average()
            // Update route rating in RouteRepository
            // TODO: Implement route rating update
        }
    }

    private suspend fun addActivity(activity: SocialActivity) {
        val activityId = activitiesCollection.document().id
        val activityWithId = activity.copy(
            id = activityId,
            createdAt = Date()
        )
        activitiesCollection.document(activityId).set(activityWithId).await()
    }

    suspend fun updateUserStats(userId: String, stats: UserStats) {
        statsCollection.document(userId).set(stats).await()
    }

    suspend fun getUserStats(userId: String): UserStats {
        return statsCollection.document(userId).get().await()
            .toObject(UserStats::class.java) ?: UserStats()
    }

    suspend fun uploadPhoto(photoBytes: ByteArray): String {
        val photoRef = storage.reference.child("photos/${UUID.randomUUID()}.jpg")
        photoRef.putBytes(photoBytes).await()
        return photoRef.downloadUrl.await().toString()
    }
}
