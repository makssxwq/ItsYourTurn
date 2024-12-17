package com.itsyourturn.moscow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.itsyourturn.moscow.data.model.Route
import com.itsyourturn.moscow.data.model.Waypoint
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class RouteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val routesCollection = db.collection("routes")

    suspend fun generateRoute(
        startPoint: GeoPoint,
        duration: Int,
        placeTypes: List<String>,
        isNightTime: Boolean
    ): Route {
        // Moscow bounds
        val moscowBounds = mapOf(
            "north" to 55.9578,
            "south" to 55.5741,
            "east" to 37.9465,
            "west" to 37.2824
        )

        // Generate random waypoints within Moscow bounds
        val waypoints = generateWaypoints(startPoint, duration, placeTypes, moscowBounds)

        val route = Route(
            id = generateRouteId(),
            startPoint = startPoint,
            waypoints = waypoints,
            duration = duration,
            placeTypes = placeTypes,
            isIlluminated = isNightTime
        )

        routesCollection.document(route.id).set(route).await()
        return route
    }

    private fun generateWaypoints(
        startPoint: GeoPoint,
        duration: Int,
        placeTypes: List<String>,
        bounds: Map<String, Double>
    ): List<Waypoint> {
        val waypoints = mutableListOf<Waypoint>()
        
        // Calculate number of waypoints based on duration (roughly 1 waypoint per 15 minutes)
        val numberOfWaypoints = duration / 15
        
        repeat(numberOfWaypoints) {
            val lat = Random.nextDouble(bounds["south"]!!, bounds["north"]!!)
            val lng = Random.nextDouble(bounds["west"]!!, bounds["east"]!!)
            
            waypoints.add(
                Waypoint(
                    location = GeoPoint(lat, lng),
                    name = "Waypoint ${it + 1}",
                    placeType = placeTypes.random()
                )
            )
        }
        
        return waypoints
    }

    private fun generateRouteId(): String {
        return System.currentTimeMillis().toString() + Random.nextInt(1000, 9999)
    }

    suspend fun saveRoute(route: Route) {
        routesCollection.document(route.id).set(route).await()
    }

    suspend fun getRoute(routeId: String): Route? {
        return routesCollection.document(routeId).get().await().toObject(Route::class.java)
    }
}
