package com.itsyourturn.moscow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [LocalRoute::class, LocalWaypoint::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RouteDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun waypointDao(): WaypointDao
}
