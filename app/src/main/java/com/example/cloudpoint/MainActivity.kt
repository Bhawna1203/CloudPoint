package com.example.cloudpoint

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.cloudpoint.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.sql.Connection
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?){
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

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
                Toast.makeText(
                    this,
                    "Your location provider is already turned on",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }
     private fun isLocationEnabled(): Boolean{
         val locationManager: LocationManager =
             getSystemService(Context.LOCATION_SERVICE) as LocationManager
         return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                 || locationManager.isProviderEnabled(
             LocationManager.NETWORK_PROVIDER

         )
     }


}