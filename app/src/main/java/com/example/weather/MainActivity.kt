package com.example.weather

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.databinding.ActivityMainBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("jaipur") // Fetch default city weather on app load
        searchCity() // Set up the search functionality
    }

    private fun searchCity() {
        binding.searchview.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        fetchWeatherData(it) // Fetch weather data for the entered query
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Handle real-time text changes
                return false
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
        val apiInterface = retrofit.create(ApiInterface::class.java)

        // Make API call
        val response = apiInterface.getWeatherData(
            city = cityName,
            appid = "f20cda5afb17ee1724ec97e1af6d7dd3",
            units = "metric"
        )

        response.enqueue(object : retrofit2.Callback<weatherApp> {
            override fun onResponse(call: Call<weatherApp>, response: Response<weatherApp>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val temperature = it.main?.temp?.toString() ?: "N/A"
                        val humidity = it.main?.humidity ?: "N/A"
                        val windspeed = it.wind?.speed ?: "N/A"
                        val sunrise = it.sys?.sunrise?.toLong()?.let { convertUnixToTime(it) } ?: "N/A"
                        val sunset = it.sys?.sunset?.toLong()?.let { convertUnixToTime(it) } ?: "N/A"

                        val seaLevel = it.main?.pressure ?: "N/A"
                        val condition = it.weather?.firstOrNull()?.main ?: "Unknown"
                        val maxTemp = it.main?.tempMax ?: "N/A"
                        val minTemp = it.main?.tempMin ?: "N/A"

                        binding.temp.text = "$temperature °C"
                        binding.weather.text = condition
                        binding.max.text = "Max Temp: $maxTemp °C"
                        binding.min.text = "Min Temp: $minTemp °C"
                        binding.humidity.text = "$humidity %"
                        binding.windspeed.text = "$windspeed m/s"
                        binding.sunrise.text = "Sunrise: $sunrise"
                        binding.sunset.text = "Sunset: $sunset"
                        binding.condition.text = condition
                        binding.day.text = dayName(System.currentTimeMillis())
                        binding.date.text = date()
                        binding.cityname.text = cityName
                        binding.sea.text = "$seaLevel hPa"
                        chageImageAccordingToWeaterCondtion(condition)


                        Log.d("onResponse", "Temperature: $temperature, Humidity: $humidity")
                    } ?: Log.e("onResponse", "Response body is null")
                } else {
                    Log.e("onResponse", "Response failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<weatherApp>, t: Throwable) {
                Log.e("onFailure", "API call failed: ${t.message}")
            }
        })
    }

    private fun convertUnixToTime(unixTime: Long): String {
        val date = Date(unixTime * 1000) // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Format as 6:00 AM, 5:30 PM, etc.
        return sdf.format(date)
    }
    private fun chageImageAccordingToWeaterCondtion(condition: String) {
        val conditionNormalized = condition.lowercase(Locale.getDefault()) // Normalize the condition string
        when {
            conditionNormalized.contains("clear") || conditionNormalized.contains("sunny") -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            conditionNormalized.contains("cloud") || conditionNormalized.contains("mist") || conditionNormalized.contains("fog") -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            conditionNormalized.contains("rain") || conditionNormalized.contains("drizzle") || conditionNormalized.contains("showers") -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            conditionNormalized.contains("snow") || conditionNormalized.contains("blizzard") -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                // Default fallback for unknown conditions
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }


    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
