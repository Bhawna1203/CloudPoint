package com.example.cloudpoint

import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
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

        }


        private inner class CallAPILoginAsyncTask(): AsyncTask<Any, Void, String>(){

            private lateinit var customProgressDialog: Dialog

            override fun onPreExecute() {
                super.onPreExecute()
                showProgressDialog()
                CallAPILoginAsyncTask().execute()
            }
            override fun doInBackground(vararg p0: Any?): String {
                var result: String
                var connection: HttpURLConnection ?= null

                try{
                    val url = URL("https://run.mocky.io/v3/223c5dfc-9639-48d2-b37f-2ce1dbe077b3")
                    connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.doOutput = true

                    val httpsResult : Int = connection.responseCode
                    if(httpsResult == HttpsURLConnection.HTTP_OK){
                        val inputStream = connection.inputStream
                        val reader = BufferedReader(
                            InputStreamReader(inputStream)
                        )
                        val stringBuilder = StringBuilder()
                        var line : String?
                        try{
                            while(reader.readLine().also{line = it} != null){
                                stringBuilder.append(line + "\n")
                            }
                        }catch (e: IOException){
                            e.printStackTrace()
                        }finally {
                            try{
                                inputStream.close()
                            }catch (e:IOException){
                                e.printStackTrace()
                            }
                        }
                        result = stringBuilder.toString()
                    }else{
                        result = connection.responseMessage
                    }
                }catch (e: SocketTimeoutException){
                    result = "Connection timeout"
                }catch(e:Exception){
                    result = "Error : " + e.message
                }finally {
                    connection?.disconnect()
                }
                return result
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                cancelProgressDialog()
                Log.i("JSON RESPONSE RESULT", result.toString())
            }
            private fun showProgressDialog(){
                customProgressDialog = Dialog(this@MainActivity)
                customProgressDialog.setContentView(R.layout.dialog_custom_progress)
                customProgressDialog.show()
            }

            private fun cancelProgressDialog(){
                customProgressDialog.dismiss()
            }

        }










}