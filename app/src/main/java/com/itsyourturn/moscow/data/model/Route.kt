package com.itsyourturn.moscow.data.model

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Route(
    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val startPoint: GeoPoint? = null,
    val waypoints: List<Waypoint> = emptyList(),
    val distance: Double = 0.0,
    val duration: Int = 0, // in minutes
    val difficulty: String = "",
    val rating: Double = 0.0,
    val numberOfRatings: Int = 0,
    val createdAt: Date = Date(),
    val placeTypes: List<String> = emptyList(),
    val isIlluminated: Boolean = false,
    val weatherCondition: String = ""
)

data class Waypoint(
    val location: GeoPoint,
    val name: String,
    val description: String = "",
    val photoUrl: String = "",
    val placeType: String = ""
)
