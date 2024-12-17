package com.itsyourturn.moscow.service

import android.content.Context
import androidx.work.*
import com.itsyourturn.moscow.data.repository.RouteRepository
import com.itsyourturn.moscow.data.repository.WeatherRepository
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleWeatherUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val weatherWorkRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(
            1, TimeUnit.HOURS
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "weather_updates",
                ExistingPeriodicWorkPolicy.KEEP,
                weatherWorkRequest
            )
    }

    fun scheduleRouteReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val routeReminderRequest = PeriodicWorkRequestBuilder<RouteReminderWorker>(
            1, TimeUnit.DAYS
        ).setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "route_reminders",
                ExistingPeriodicWorkPolicy.KEEP,
                routeReminderRequest
            )
    }

    private fun calculateInitialDelay(): Long {
        // Schedule for 9:00 AM next day
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = currentTime
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }
        return calendar.timeInMillis - currentTime
    }
}

class WeatherNotificationWorker(
    context: Context,
    params: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            // Check weather for saved routes
            val moscowWeather = weatherRepository.getWeatherForLocation(55.7558, 37.6173)
            
            if (moscowWeather is WeatherRepository.WeatherResult.Success) {
                // Send notification if weather is suitable for walking
                if (isWeatherSuitableForWalking(moscowWeather.weatherInfo)) {
                    sendWeatherNotification(
                        "Great Weather for Walking!",
                        "It's ${moscowWeather.weatherInfo.temperature}°C and ${moscowWeather.weatherInfo.description}. Perfect for a walk!"
                    )
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun isWeatherSuitableForWalking(weatherInfo: WeatherInfo): Boolean {
        return weatherInfo.temperature in 10.0..25.0 &&
                !weatherInfo.condition.lowercase().contains("rain") &&
                !weatherInfo.condition.lowercase().contains("snow")
    }

    private fun sendWeatherNotification(title: String, message: String) {
        // TODO: Implement notification sending
    }
}

class RouteReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val routeRepository: RouteRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            // Get user's saved routes and send reminders
            // TODO: Implement route reminder logic

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
