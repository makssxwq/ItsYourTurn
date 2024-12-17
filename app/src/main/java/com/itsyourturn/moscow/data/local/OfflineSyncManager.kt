package com.itsyourturn.moscow.data.local

import android.content.Context
import androidx.room.Room
import androidx.work.*
import com.google.firebase.firestore.GeoPoint
import com.itsyourturn.moscow.data.model.Route
import com.itsyourturn.moscow.data.model.Waypoint
import java.util.concurrent.TimeUnit

class OfflineSyncManager(private val context: Context) {
    private val database: RouteDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            RouteDatabase::class.java,
            "routes_db"
        ).build()
    }

    suspend fun downloadRoute(route: Route) {
        val localRoute = LocalRoute(
            id = route.id,
            name = route.name,
            createdBy = route.createdBy,
            startLatitude = route.startPoint?.latitude ?: 0.0,
            startLongitude = route.startPoint?.longitude ?: 0.0,
            distance = route.distance,
            duration = route.duration,
            difficulty = route.difficulty,
            rating = route.rating,
            numberOfRatings = route.numberOfRatings,
            createdAt = route.createdAt,
            placeTypes = route.placeTypes,
            isIlluminated = route.isIlluminated,
            weatherCondition = route.weatherCondition,
            isDownloaded = true
        )

        val localWaypoints = route.waypoints.map { waypoint ->
            LocalWaypoint(
                routeId = route.id,
                latitude = waypoint.location.latitude,
                longitude = waypoint.location.longitude,
                name = waypoint.name,
                description = waypoint.description,
                photoUrl = waypoint.photoUrl,
                placeType = waypoint.placeType
            )
        }

        database.routeDao().insertRoute(localRoute)
        database.waypointDao().insertWaypoints(localWaypoints)

        // Download map tiles for offline use
        downloadMapTiles(route)
    }

    suspend fun getOfflineRoute(routeId: String): Route? {
        val localRoute = database.routeDao().getRouteById(routeId) ?: return null
        val waypoints = database.waypointDao().getWaypointsForRoute(routeId)

        return Route(
            id = localRoute.id,
            name = localRoute.name,
            createdBy = localRoute.createdBy,
            startPoint = GeoPoint(localRoute.startLatitude, localRoute.startLongitude),
            waypoints = waypoints.map { waypoint ->
                Waypoint(
                    location = GeoPoint(waypoint.latitude, waypoint.longitude),
                    name = waypoint.name,
                    description = waypoint.description,
                    photoUrl = waypoint.photoUrl,
                    placeType = waypoint.placeType
                )
            },
            distance = localRoute.distance,
            duration = localRoute.duration,
            difficulty = localRoute.difficulty,
            rating = localRoute.rating,
            numberOfRatings = localRoute.numberOfRatings,
            createdAt = localRoute.createdAt,
            placeTypes = localRoute.placeTypes,
            isIlluminated = localRoute.isIlluminated,
            weatherCondition = localRoute.weatherCondition
        )
    }

    private fun downloadMapTiles(route: Route) {
        // Calculate the bounding box for the route
        val points = mutableListOf<GeoPoint>()
        route.startPoint?.let { points.add(it) }
        points.addAll(route.waypoints.map { it.location })

        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLng = points.minOf { it.longitude }
        val maxLng = points.maxOf { it.longitude }

        // Download map tiles for offline use using Google Maps SDK
        val offlineRegion = com.google.android.gms.maps.model.LatLngBounds(
            com.google.android.gms.maps.model.LatLng(minLat, minLng),
            com.google.android.gms.maps.model.LatLng(maxLat, maxLng)
        )

        // TODO: Implement map tiles download using Google Maps SDK
    }

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<RouteSyncWorker>(
            1, TimeUnit.DAYS
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "route_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
    }

    class RouteSyncWorker(
        context: Context,
        params: WorkerParameters
    ) : CoroutineWorker(context, params) {

        override suspend fun doWork(): Result {
            val database = Room.databaseBuilder(
                applicationContext,
                RouteDatabase::class.java,
                "routes_db"
            ).build()

            try {
                val outdatedRoutes = database.routeDao()
                    .getOutdatedRoutes(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))

                // TODO: Sync outdated routes with Firebase

                return Result.success()
            } catch (e: Exception) {
                return Result.retry()
            }
        }
    }
}
