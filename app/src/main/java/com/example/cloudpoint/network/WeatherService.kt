package com.example.cloudpoint.network



import com.example.cloudpoint.models.weatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


// TODO (Create a WeatherService interface)
interface WeatherService {

    @GET("2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("appid") appid: String?
    ): Call<weatherResponse>
}
// END