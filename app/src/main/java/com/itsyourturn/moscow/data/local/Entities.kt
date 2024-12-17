package com.itsyourturn.moscow.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

@Entity(tableName = "routes")
data class LocalRoute(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdBy: String,
    val startLatitude: Double,
    val startLongitude: Double,
    val distance: Double,
    val duration: Int,
    val difficulty: String,
    val rating: Double,
    val numberOfRatings: Int,
    val createdAt: Date,
    val placeTypes: List<String>,
    val isIlluminated: Boolean,
    val weatherCondition: String,
    val isDownloaded: Boolean = false,
    val lastSyncTime: Date = Date()
)

@Entity(
    tableName = "waypoints",
    foreignKeys = [
        ForeignKey(
            entity = LocalRoute::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LocalWaypoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routeId: String,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val description: String,
    val photoUrl: String,
    val placeType: String
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromGeoPoint(geoPoint: GeoPoint?): String? {
        return geoPoint?.let { "${it.latitude},${it.longitude}" }
    }

    @TypeConverter
    fun toGeoPoint(value: String?): GeoPoint? {
        return value?.split(",")?.let {
            if (it.size == 2) {
                GeoPoint(it[0].toDouble(), it[1].toDouble())
            } else null
        }
    }
}
