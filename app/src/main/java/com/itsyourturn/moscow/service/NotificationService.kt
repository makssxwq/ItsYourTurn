package com.itsyourturn.moscow.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.itsyourturn.moscow.R
import com.itsyourturn.moscow.ui.MainActivity

class NotificationService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_WEATHER = "weather_updates"
        private const val CHANNEL_ACHIEVEMENTS = "achievements"
        private const val CHANNEL_ROUTES = "saved_routes"
        private const val CHANNEL_SOCIAL = "social_updates"

        private const val NOTIFICATION_WEATHER_ID = 1
        private const val NOTIFICATION_ACHIEVEMENT_ID = 2
        private const val NOTIFICATION_ROUTE_ID = 3
        private const val NOTIFICATION_SOCIAL_ID = 4
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_WEATHER,
                    getString(R.string.channel_weather_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = getString(R.string.channel_weather_description)
                },
                NotificationChannel(
                    CHANNEL_ACHIEVEMENTS,
                    getString(R.string.channel_achievements_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.channel_achievements_description)
                },
                NotificationChannel(
                    CHANNEL_ROUTES,
                    getString(R.string.channel_routes_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = getString(R.string.channel_routes_description)
                },
                NotificationChannel(
                    CHANNEL_SOCIAL,
                    getString(R.string.channel_social_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = getString(R.string.channel_social_description)
                }
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data["type"]?.let { type ->
            when (type) {
                "weather" -> handleWeatherNotification(remoteMessage)
                "achievement" -> handleAchievementNotification(remoteMessage)
                "route" -> handleRouteNotification(remoteMessage)
                "social" -> handleSocialNotification(remoteMessage)
            }
        }
    }

    private fun handleWeatherNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: getString(R.string.weather_update)
        val message = remoteMessage.data["message"] ?: ""
        
        showNotification(
            channelId = CHANNEL_WEATHER,
            notificationId = NOTIFICATION_WEATHER_ID,
            title = title,
            message = message,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    private fun handleAchievementNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: getString(R.string.achievement_unlocked)
        val message = remoteMessage.data["message"] ?: ""
        
        showNotification(
            channelId = CHANNEL_ACHIEVEMENTS,
            notificationId = NOTIFICATION_ACHIEVEMENT_ID,
            title = title,
            message = message,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    private fun handleRouteNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: getString(R.string.route_reminder)
        val message = remoteMessage.data["message"] ?: ""
        val routeId = remoteMessage.data["routeId"]
        
        showNotification(
            channelId = CHANNEL_ROUTES,
            notificationId = NOTIFICATION_ROUTE_ID,
            title = title,
            message = message,
            priority = NotificationCompat.PRIORITY_DEFAULT,
            extras = routeId?.let { mapOf("route_id" to it) }
        )
    }

    private fun handleSocialNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: getString(R.string.social_update)
        val message = remoteMessage.data["message"] ?: ""
        
        showNotification(
            channelId = CHANNEL_SOCIAL,
            notificationId = NOTIFICATION_SOCIAL_ID,
            title = title,
            message = message,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    private fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        priority: Int,
        extras: Map<String, String>? = null
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            extras?.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    override fun onNewToken(token: String) {
        // TODO: Send token to server
    }
}
