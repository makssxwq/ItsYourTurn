package com.itsyourturn.moscow.data.service

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.itsyourturn.moscow.data.model.Route
import com.itsyourturn.moscow.data.model.Waypoint
import com.itsyourturn.moscow.data.repository.WeatherRepository
import kotlin.math.*
import kotlin.random.Random

class RouteGenerationService(
    private val weatherRepository: WeatherRepository
) {
    companion object {
        // Moscow bounds
        private const val MOSCOW_NORTH = 55.9578
        private const val MOSCOW_SOUTH = 55.5741
        private const val MOSCOW_EAST = 37.9465
        private const val MOSCOW_WEST = 37.2824
        
        // Average walking speed in meters per minute
        private const val AVG_WALKING_SPEED = 83.0
        
        // Points of interest in Moscow
        private val MOSCOW_LANDMARKS = listOf(
            POI("Red Square", 55.7539, 37.6208, "landmark"),
            POI("Gorky Park", 55.7298, 37.6012, "park"),
            POI("VDNKh", 55.8263, 37.6377, "park"),
            POI("Arbat Street", 55.7495, 37.5946, "street"),
            POI("Tretyakov Gallery", 55.7415, 37.6208, "museum"),
            POI("Moscow State University", 55.7025, 37.5302, "landmark"),
            POI("Zaryadye Park", 55.7514, 37.6278, "park"),
            POI("Tsaritsyno", 55.6156, 37.6868, "park"),
            POI("Kolomenskoye", 55.6698, 37.6698, "park"),
            POI("Patriarch Ponds", 55.7631, 37.5931, "park")
        )
    }

    suspend fun generateRoute(
        startPoint: GeoPoint,
        duration: Int,
        preferredPlaceTypes: List<String>,
        currentTime: Long = System.currentTimeMillis()
    ): RouteGenerationResult {
        try {
            // Check weather conditions
            val weather = weatherRepository.getWeatherForLocation(startPoint.latitude, startPoint.longitude)
            if (weather is WeatherRepository.WeatherResult.Success) {
                // Don't generate route in severe weather conditions
                if (!isWeatherSuitableForWalking(weather.weatherInfo)) {
                    return RouteGenerationResult.Error("Weather conditions are not suitable for walking")
                }
            }

            // Calculate maximum distance based on duration
            val maxDistance = duration * AVG_WALKING_SPEED

            // Generate waypoints
            val waypoints = generateWaypoints(
                startPoint = startPoint,
                maxDistance = maxDistance,
                preferredPlaceTypes = preferredPlaceTypes,
                isNightTime = isNightTime(currentTime)
            )

            // Create route
            val route = Route(
                id = generateRouteId(),
                startPoint = startPoint,
                waypoints = waypoints,
                distance = calculateTotalDistance(startPoint, waypoints),
                duration = duration,
                placeTypes = preferredPlaceTypes,
                isIlluminated = isNightTime(currentTime)
            )

            return RouteGenerationResult.Success(route)
        } catch (e: Exception) {
            return RouteGenerationResult.Error(e.message ?: "Failed to generate route")
        }
    }

    private fun generateWaypoints(
        startPoint: GeoPoint,
        maxDistance: Double,
        preferredPlaceTypes: List<String>,
        isNightTime: Boolean
    ): List<Waypoint> {
        val waypoints = mutableListOf<Waypoint>()
        var remainingDistance = maxDistance
        var currentPoint = startPoint

        // Filter POIs based on preferences and time of day
        val availablePOIs = MOSCOW_LANDMARKS.filter { poi ->
            (preferredPlaceTypes.isEmpty() || poi.type in preferredPlaceTypes) &&
            (!isNightTime || isLocationSafeAtNight(poi))
        }

        while (remainingDistance > 0 && availablePOIs.isNotEmpty()) {
            // Find nearest POI
            val nextPOI = findNextPOI(currentPoint, availablePOIs)
            val distance = calculateDistance(
                currentPoint.latitude, currentPoint.longitude,
                nextPOI.lat, nextPOI.lon
            )

            if (distance <= remainingDistance) {
                waypoints.add(
                    Waypoint(
                        location = GeoPoint(nextPOI.lat, nextPOI.lon),
                        name = nextPOI.name,
                        placeType = nextPOI.type
                    )
                )
                currentPoint = GeoPoint(nextPOI.lat, nextPOI.lon)
                remainingDistance -= distance
            } else {
                break
            }
        }

        return waypoints
    }

    private fun findNextPOI(currentPoint: GeoPoint, pois: List<POI>): POI {
        return pois.minByOrNull { poi ->
            calculateDistance(
                currentPoint.latitude, currentPoint.longitude,
                poi.lat, poi.lon
            )
        } ?: pois.random()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // Earth's radius in meters
        val φ1 = lat1 * Math.PI / 180
        val φ2 = lat2 * Math.PI / 180
        val Δφ = (lat2 - lat1) * Math.PI / 180
        val Δλ = (lon2 - lon1) * Math.PI / 180

        val a = sin(Δφ / 2) * sin(Δφ / 2) +
                cos(φ1) * cos(φ2) *
                sin(Δλ / 2) * sin(Δλ / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    private fun calculateTotalDistance(startPoint: GeoPoint, waypoints: List<Waypoint>): Double {
        var totalDistance = 0.0
        var previousPoint = startPoint

        waypoints.forEach { waypoint ->
            totalDistance += calculateDistance(
                previousPoint.latitude, previousPoint.longitude,
                waypoint.location.latitude, waypoint.location.longitude
            )
            previousPoint = waypoint.location
        }

        return totalDistance
    }

    private fun isWeatherSuitableForWalking(weatherInfo: WeatherInfo): Boolean {
        return when {
            weatherInfo.temperature < -15 -> false // Too cold
            weatherInfo.temperature > 35 -> false  // Too hot
            weatherInfo.condition.lowercase() in listOf("thunderstorm", "snow", "rain") -> false
            else -> true
        }
    }

    private fun isNightTime(currentTime: Long): Boolean {
        // Simple check - consider night time between 22:00 and 6:00
        val hour = java.time.LocalDateTime
            .ofInstant(java.time.Instant.ofEpochMilli(currentTime), java.time.ZoneId.of("Europe/Moscow"))
            .hour
        return hour !in 6..21
    }

    private fun isLocationSafeAtNight(poi: POI): Boolean {
        // Consider parks and remote locations unsafe at night
        return poi.type !in listOf("park", "forest")
    }

    private fun generateRouteId(): String {
        return System.currentTimeMillis().toString() + Random.nextInt(1000, 9999)
    }
}

data class POI(
    val name: String,
    val lat: Double,
    val lon: Double,
    val type: String
)

sealed class RouteGenerationResult {
    data class Success(val route: Route) : RouteGenerationResult()
    data class Error(val message: String) : RouteGenerationResult()
}
