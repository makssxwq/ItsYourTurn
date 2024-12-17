package com.itsyourturn.moscow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.itsyourturn.moscow.data.model.UserProfile
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun createUserProfile(userId: String, profile: UserProfile) {
        usersCollection.document(userId).set(profile).await()
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return usersCollection.document(userId).get().await().toObject(UserProfile::class.java)
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        usersCollection.document(userId).update(updates).await()
    }

    suspend fun addFavoriteRoute(userId: String, routeId: String) {
        usersCollection.document(userId).update(
            "favoriteRoutes", com.google.firebase.firestore.FieldValue.arrayUnion(routeId)
        ).await()
    }

    suspend fun removeFavoriteRoute(userId: String, routeId: String) {
        usersCollection.document(userId).update(
            "favoriteRoutes", com.google.firebase.firestore.FieldValue.arrayRemove(routeId)
        ).await()
    }

    suspend fun updateStatistics(userId: String, distance: Double, timeSpent: Long) {
        val profile = getUserProfile(userId)
        profile?.let {
            val updates = mapOf(
                "totalDistance" to (it.totalDistance + distance),
                "completedRoutes" to (it.completedRoutes + 1),
                "statistics.totalTimeSpent" to (it.statistics.totalTimeSpent + timeSpent)
            )
            updateUserProfile(userId, updates)
        }
    }
}
