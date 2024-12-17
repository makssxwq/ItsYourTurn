package com.itsyourturn.moscow.data.repository

import com.itsyourturn.moscow.data.api.WeatherService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    private val weatherService: WeatherService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }

    private val apiKey = "YOUR_OPENWEATHERMAP_API_KEY" // TODO: Move to secure storage

    suspend fun getWeatherForLocation(lat: Double, lon: Double): WeatherResult {
        return try {
            val response = weatherService.getCurrentWeather(lat, lon, apiKey)
            WeatherResult.Success(
                WeatherInfo(
                    temperature = response.main.temp,
                    condition = response.weather.firstOrNull()?.main ?: "",
                    description = response.weather.firstOrNull()?.description ?: "",
                    isDay = response.sys.let { 
                        val currentTime = System.currentTimeMillis() / 1000
                        currentTime in it.sunrise..it.sunset 
                    }
                )
            )
        } catch (e: Exception) {
            WeatherResult.Error(e.message ?: "Failed to fetch weather")
        }
    }
}

data class WeatherInfo(
    val temperature: Double,
    val condition: String,
    val description: String,
    val isDay: Boolean
)

sealed class WeatherResult {
    data class Success(val weatherInfo: WeatherInfo) : WeatherResult()
    data class Error(val message: String) : WeatherResult()
}
