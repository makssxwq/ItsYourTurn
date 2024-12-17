package com.itsyourturn.moscow.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes WHERE isDownloaded = 1")
    fun getDownloadedRoutes(): Flow<List<LocalRoute>>

    @Query("SELECT * FROM routes WHERE id = :routeId")
    suspend fun getRouteById(routeId: String): LocalRoute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: LocalRoute)

    @Update
    suspend fun updateRoute(route: LocalRoute)

    @Delete
    suspend fun deleteRoute(route: LocalRoute)

    @Query("DELETE FROM routes WHERE id = :routeId")
    suspend fun deleteRouteById(routeId: String)

    @Query("SELECT * FROM routes WHERE lastSyncTime < :timestamp")
    suspend fun getOutdatedRoutes(timestamp: Long): List<LocalRoute>
}

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints WHERE routeId = :routeId")
    suspend fun getWaypointsForRoute(routeId: String): List<LocalWaypoint>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoints(waypoints: List<LocalWaypoint>)

    @Query("DELETE FROM waypoints WHERE routeId = :routeId")
    suspend fun deleteWaypointsForRoute(routeId: String)

    @Transaction
    suspend fun updateWaypointsForRoute(routeId: String, waypoints: List<LocalWaypoint>) {
        deleteWaypointsForRoute(routeId)
        insertWaypoints(waypoints)
    }
}
