package com.example.dripdropdigital

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainDashboard : AppCompatActivity() {

    lateinit var temp: TextView
    lateinit var hSol: TextView
    lateinit var hAir: TextView
    lateinit var tempCpu: TextView
    lateinit var cityT: TextView
    lateinit var cityTemp: TextView
    var cityTemperature = "0°C"

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard)
        supportActionBar?.hide()

        val loadingLayout = findViewById<FrameLayout>(R.id.loadingLayout)
        var loadingGif = findViewById<LottieAnimationView>(R.id.lottie)
        loadingLayout.visibility = View.VISIBLE

        var mqttTest = MqttConnection(this)
        temp = findViewById(R.id.temperature)
        hSol = findViewById(R.id.humiditySoil)
        hAir = findViewById(R.id.humidityAir)
        tempCpu = findViewById(R.id.temperatureCPU)
        cityT = findViewById(R.id.city)
        cityTemp = findViewById(R.id.cityTemp)
        mqttTest.connect(this){ callBack ->
            runOnUiThread {
                if (callBack == "Message") {
                    temp.text = mqttTest.newMessage("temperatura")
                    hSol.text = mqttTest.newMessage("humidadeSolo")
                    hAir.text = mqttTest.newMessage("humidadeAr")
                    tempCpu.text = mqttTest.newMessage("temperaturaCpu")
                }else if (callBack == "Failed"){
                    //Set layout width to match parent
                    loadingGif.setAnimation(R.raw.failed)
                    loadingGif.playAnimation()
                    loadingGif.loop(false)
                    Toast.makeText(this, "Failed to connect to MQTT Broker", Toast.LENGTH_SHORT).show()
                    Thread {
                        Thread.sleep(2750)
                        finish()
                    }.start()
                }else if(callBack == "Success"){
                    loadingGif.setAnimation(R.raw.success)
                    loadingGif.playAnimation()
                    loadingGif.loop(false)
                    Toast.makeText(this, "Connected to MQTT Broker", Toast.LENGTH_SHORT).show()
                    Thread {
                        Thread.sleep(2750)
                        runOnUiThread {
                            loadingLayout.visibility = View.GONE
                        }
                    }.start()
                }
            }
        }
        val apiKey = "9ec6b8792977153a9db9dc83d2134ec5"
        var city = "Tomar"
        getCurrentWeather(apiKey, city) { weatherDescription ->
            runOnUiThread {
                cityT.text = "$weatherDescription in $city"
                cityTemp.text = "$cityTemperature°C"
            }
        }
    }

    private fun getCurrentWeather(apiKey: String, city: String, callback: (String) -> Unit){
        val client = OkHttpClient()
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val jsonObject = JSONObject(body)
                val weatherArray = jsonObject.getJSONArray("weather")
                val weatherObject = weatherArray.getJSONObject(0)
                val weatherDescription = weatherObject.getString("description")
                cityTemperature = jsonObject.getJSONObject("main").getString("temp")
                callback(weatherDescription)
            }
        })

    }

}