package com.example.cloudpoint.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.cloudpoint.R
import com.example.cloudpoint.models.weatherResponse
import com.example.cloudpoint.network.WeatherService
import com.example.cloudpoint.utils.Constants
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog ?= null
    private lateinit var mSharedPreferences: SharedPreferences


        @RequiresApi(Build.VERSION_CODES.N)
        override fun onCreate(savedInstanceState: Bundle?){
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

        //    setSupportActionBar(toolbar_fav_place)

            mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME,Context.MODE_PRIVATE)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            if(!isLocationEnabled()){
                Toast.makeText(
                    this,
                    "Your location provider is turned off. PLease turn is on",
                    Toast.LENGTH_SHORT
                ).show()

                //If you directly want to come in the example when screen starts with the page
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }else{
                Dexter.withActivity(this)
                    .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                requestLocationData()
                            }

                            if (report.isAnyPermissionPermanentlyDenied) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "You have denied location permission. Please allow it is mandatory.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread()
                    .check()


                    }

            // TODO Now add the override methods to load the menu file and perform the selection on item click.)

            fun onCreateOptionsMenu(menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.menu_main,menu)
                return super.onCreateOptionsMenu(menu)
            }

            fun onOptionsItemSelected(item: MenuItem): Boolean {
                return when(item.itemId){
                    R.id.action_refresh -> {
                        requestLocationData()
                        true
                    }else -> return super.onOptionsItemSelected(item)
                }

            }


        }



    /**
     * A function which is used to verify that the location or GPS is enable or not of the user's device.
     */
     private fun isLocationEnabled(): Boolean{
         val locationManager: LocationManager =
             getSystemService(LOCATION_SERVICE) as LocationManager
         return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                 || locationManager.isProviderEnabled(
             LocationManager.NETWORK_PROVIDER

         )
     }

    /**
     * A function used to show the alert dialog when the permissions are denied and need to allow it from settings app info.
     */

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) =
        // TODO (Here we will check whether the internet
        if (Constants.isNetworkAvailable(this@MainActivity)) {

            // TODO (Make an api call using retrofit.)
            val retrofit: Retrofit = Retrofit.Builder()
                // API base URL.
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            // TODO (Further step for API call)
            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall: Call<weatherResponse> = service.getWeather(
                latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
            )

            showCustomProgressDialog()
            // Callback methods are executed using the Retrofit callback executor.
            listCall.enqueue(object : Callback<weatherResponse> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onResponse(call: Call<weatherResponse>, response: Response<weatherResponse>) {
                    // Check weather the response is success or not.
                    if (response.isSuccessful) {
                        hideProgressDialog()
                        /** The de-serialized response body of a successful response. */
                        /** The de-serialized response body of a successful response. */
                        val weatherList: weatherResponse? = response.body()
                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA,weatherResponseJsonString)
                        editor.apply()
                        if (weatherList != null) {
                            setupUI(weatherList)
                        }
                        Log.i("Response Result", "$weatherList")
                    } else {
                        // If the response is not success then we check the response code.
                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<weatherResponse>, t: Throwable) {
                    Log.e("Errorrrrr", t.message.toString())
                    hideProgressDialog()
                }

            })




        } else {
            Toast.makeText(
                this@MainActivity,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }

    /**
     * Method is used to show the Custom Progress Dialog.
     */
    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }
    /**
     * This function is used to dismiss the progress dialog if it is visible to user.
     */
    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. " +
                    "It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    // TODO ( Add a function to get the location of the device using the fusedLocationProviderClient.)
    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        Looper.myLooper()?.let {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                it
            )
        }
    }

    // TODO (Register a request location callback to get the location.)
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current Longitude", "$longitude")
            getLocationWeatherDetails(latitude, longitude)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    private fun setupUI(weatherList: weatherResponse) {
        val weatherResponseJsonString = mSharedPreferences
            .getString(Constants.WEATHER_RESPONSE_DATA,
            "Non")

        if(!weatherResponseJsonString.isNullOrEmpty()){
            val weatherList = Gson().fromJson(weatherResponseJsonString, weatherResponse::class.java)

            for(i in weatherList.weather.indices){
                Log.i("Weather Name", weatherList.weather.toString())

                tv_main.text = weatherList.weather[i].main
                tv_main_description.text = weatherList.weather[i].description
                tv_temp.text = weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                tv_humidity.text = weatherList.main.humidity.toString() + " per cent"


                tv_min.text = weatherList.main.temp_min.toString() + " min"
                tv_max.text = weatherList.main.temp_max.toString() + " max"
                tv_speed.text = weatherList.wind.speed.toString()
                tv_name.text = weatherList.name
                tv_country.text = weatherList.sys.country

                //Sunset and sunrise time
                tv_sunrise_time.text = unixTime(weatherList.sys.sunrise.toLong())
                tv_sunset_time.text = unixTime(weatherList.sys.sunset.toLong())


                // Here we update the main icon
                when (weatherList.weather[i].icon) {
                    "01d" -> iv_main.setImageResource(R.drawable.sunny)
                    "02d" -> iv_main.setImageResource(R.drawable.cloud)
                    "03d" -> iv_main.setImageResource(R.drawable.cloud)
                    "04d" -> iv_main.setImageResource(R.drawable.cloud)
                    "04n" -> iv_main.setImageResource(R.drawable.cloud)
                    "10d" -> iv_main.setImageResource(R.drawable.rain)
                    "11d" -> iv_main.setImageResource(R.drawable.storm)
                    "13d" -> iv_main.setImageResource(R.drawable.snowflake)
                    "01n" -> iv_main.setImageResource(R.drawable.cloud)
                    "02n" -> iv_main.setImageResource(R.drawable.cloud)
                    "03n" -> iv_main.setImageResource(R.drawable.cloud)
                    "10n" -> iv_main.setImageResource(R.drawable.cloud)
                    "11n" -> iv_main.setImageResource(R.drawable.rain)
                    "13n" -> iv_main.setImageResource(R.drawable.snowflake)
                }

            }
        }

    }

    /**
     * Function is used to get the temperature unit value.
     */
    private fun getUnit(value: String): String? {
        Log.i("unitttttt", value)
        var value = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }

    /**
     * The function is used to get the formatted time
     * based on the Format and the LOCALE we pass to it.
     */
    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat") val sdf =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SimpleDateFormat("HH:mm",Locale.UK)
            } else {
                TODO("VERSION.SDK_INT < N")
            }
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }


}

