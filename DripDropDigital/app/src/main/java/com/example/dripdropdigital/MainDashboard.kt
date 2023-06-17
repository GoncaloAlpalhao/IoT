package com.example.dripdropdigital

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.airbnb.lottie.LottieAnimationView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.Random

class MainDashboard : AppCompatActivity() {

    lateinit var temp: TextView
    lateinit var hSol: TextView
    lateinit var hAir: TextView
    lateinit var tempCpu: TextView
    lateinit var cityT: TextView
    lateinit var ledState: TextView
    lateinit var weatherIcon: ImageView
    var weatherIconSet = ""
    var isRain = false
    lateinit var rainState: String

    private lateinit var series1: LineGraphSeries<DataPoint>
    private lateinit var series2: LineGraphSeries<DataPoint>
    private lateinit var series3: LineGraphSeries<DataPoint>
    private lateinit var series4: LineGraphSeries<DataPoint>
    private lateinit var graph: GraphView // The graph is the graph itself
    private lateinit var graph2: GraphView
    private lateinit var graph3: GraphView
    private lateinit var graph4: GraphView
    private lateinit var viewport: Viewport // The viewport is used to set the graph's view
    val x = mutableListOf<Double>() // The x axis of the graph

    @SuppressLint("MissingInflatedId", "SetTextI18n", "DiscouragedApi")
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
        ledState = findViewById(R.id.ledState)
        weatherIcon = findViewById(R.id.weatherIcon)

        // Initialize the graphs
        graph = findViewById(R.id.graph1)
        graph2 = findViewById(R.id.graph2)
        graph3 = findViewById(R.id.graph3)
        graph4 = findViewById(R.id.graph4)
        // Initialize the series for each graph
        series1 = LineGraphSeries()
        series2 = LineGraphSeries()
        series3 = LineGraphSeries()
        series4 = LineGraphSeries()
        // Add the series to the respective graphs
        graph.addSeries(series1)
        graph2.addSeries(series2)
        graph3.addSeries(series3)
        graph4.addSeries(series4)

        mqttTest.connect(this){ callBack ->
            runOnUiThread {
                if (callBack == "Message") {
                    var temperaturaAmbient = mqttTest.newMessage("temperatura")
                    makeGraph1(temperaturaAmbient)
                    temp.text = temperaturaAmbient + "°C"

                    var humidadeAr = mqttTest.newMessage("humidadeAr")
                    makeGraph2(humidadeAr)
                    hAir.text = humidadeAr + "%"

                    var humidadeSolo = mqttTest.newMessage("humidadeSolo")
                    makeGraph3(humidadeSolo)
                    hSol.text = humidadeSolo + "%"

                    var temperaturaCPU = mqttTest.newMessage("temperaturaCpu")
                    makeGraph4(temperaturaCPU)
                    tempCpu.text = temperaturaCPU + "°C"

                    var sistemaRega = mqttTest.newMessage("sistemaRega")
                    if(sistemaRega == "1"){
                        ledState.text = "Ligado"
                    }else if(sistemaRega == "0"){
                        ledState.text = "Desligado"
                    }else{
                        ledState.text = "Erro"
                    }

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
                    if(isRain){
                        mqttTest.publish("LED", "rain", retained = true)
                    }else{
                        mqttTest.publish("LED", "norain", retained = true)
                    }
                    Toast.makeText(this, "Connected to MQTT Broker", Toast.LENGTH_SHORT).show()
                    Thread {
                        Thread.sleep(2750)
                        runOnUiThread {
                            loadingLayout.visibility = View.GONE
                            series1.resetData(arrayOf(DataPoint(0.0, 0.0)))
                            series2.resetData(arrayOf(DataPoint(0.0, 0.0)))
                            series3.resetData(arrayOf(DataPoint(0.0, 0.0)))
                            series4.resetData(arrayOf(DataPoint(0.0, 0.0)))
                        }
                        /*TEST CODE FOR THE GRAPHS
                        val random = Random()
                        while (true) {
                            val randomValue = random.nextInt(61) + 20 // Generate random value between 20 and 80
                            val randomValue2 = random.nextInt(61) + 20 // Generate random value between 20 and 80
                            val randomValue3 = random.nextInt(61) + 20 // Generate random value between 20 and 80
                            val randomValue4 = random.nextInt(61) + 20 // Generate random value between 20 and 80
                            val stringValue = randomValue.toString()
                            val stringValue2 = randomValue2.toString()
                            val stringValue3 = randomValue3.toString()
                            val stringValue4 = randomValue4.toString()
                            runOnUiThread {
                                temp.text = stringValue + "°C"
                                hSol.text = stringValue2 + "%"
                                hAir.text = stringValue3 + "%"
                                tempCpu.text = stringValue4 + "°C"
                                makeGraph1(stringValue) // Update the graph with the random value
                                makeGraph2(stringValue2)
                                makeGraph3(stringValue3)
                                makeGraph4(stringValue4)
                            }
                            Thread.sleep(1000) // Sleep for 1 second
                        }*/
                    }.start()
                }
            }
        }
        val apiKey = "9ec6b8792977153a9db9dc83d2134ec5"
        var city = "Tomar"
        getCurrentWeather(apiKey, city) { weatherDescription ->
            runOnUiThread {
                cityT.text = weatherDescription
                //If the weather all decapitalized contains "Rain" set isRaining = rain
                if (isRain){
                    rainState = "rain"
                }else{
                    rainState = "norain"
                }
                weatherIcon.setImageResource(resources.getIdentifier(weatherIconSet, "drawable", packageName))

            }
        }
    }

    private fun getCurrentWeather(apiKey: String, city: String, callback: (String) -> Unit){
        val client = OkHttpClient()
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&lang=pt&appid=$apiKey"

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
                var weatherDescription = weatherObject.getString("description")
                isRain = weatherObject.getString("id").startsWith("5")
                weatherDescription = capitalize(weatherDescription)
                weatherIconSet = "d" + weatherObject.getString("icon").substring(0,2) + "d"
                callback(weatherDescription)
            }
        })

    }


    fun capitalize(str: String): String {
        return str.trim().split("\\s+".toRegex())
            .map { it.capitalize() }.joinToString(" ")
    }

    fun makeGraph1(temp: String) {
        val yValue = temp.toDouble()
        val newX = x.size.toDouble()
        viewport = graph.viewport
        series1.appendData(DataPoint(newX, yValue), true, 100)

        x.add(System.currentTimeMillis().toDouble())

        // Set the fixed interval for the Y-axis
        val minY = 0.0 // Minimum value for Y-axis
        val maxY = 100.0 // Maximum value for Y-axis
        viewport.setMinY(minY)
        viewport.setMaxY(maxY)

        viewport.setMinX(0.0)
        viewport.setMaxX(newX + 10)
        viewport.isXAxisBoundsManual = true

        graph.onDataChanged(false, false)
    }

    fun makeGraph2(humid: String) {
        val yValue = humid.toDouble()
        val newX = x.size.toDouble()
        viewport = graph2.viewport
        series2.appendData(DataPoint(newX, yValue), true, 100)

        x.add(System.currentTimeMillis().toDouble())

        // Set the fixed interval for the Y-axis
        val minY = 0.0 // Minimum value for Y-axis
        val maxY = 100.0 // Maximum value for Y-axis
        viewport.setMinY(minY)
        viewport.setMaxY(maxY)

        viewport.setMinX(0.0)
        viewport.setMaxX(newX + 10)
        viewport.isXAxisBoundsManual = true

        graph.onDataChanged(false, false)
    }

    fun makeGraph3(humid: String) {
        val yValue = humid.toDouble()
        val newX = x.size.toDouble()
        viewport = graph3.viewport
        series3.appendData(DataPoint(newX, yValue), true, 100)

        x.add(System.currentTimeMillis().toDouble())

        // Set the fixed interval for the Y-axis
        val minY = 0.0 // Minimum value for Y-axis
        val maxY = 100.0 // Maximum value for Y-axis
        viewport.setMinY(minY)
        viewport.setMaxY(maxY)

        viewport.setMinX(0.0)
        viewport.setMaxX(newX + 10)
        viewport.isXAxisBoundsManual = true

        graph.onDataChanged(false, false)
    }

    fun makeGraph4(temp: String) {
        val yValue = temp.toDouble()
        val newX = x.size.toDouble()
        viewport = graph4.viewport
        series4.appendData(DataPoint(newX, yValue), true, 100)

        x.add(System.currentTimeMillis().toDouble())

        // Set the fixed interval for the Y-axis
        val minY = 0.0 // Minimum value for Y-axis
        val maxY = 100.0 // Maximum value for Y-axis
        viewport.setMinY(minY)
        viewport.setMaxY(maxY)

        viewport.setMinX(0.0)
        viewport.setMaxX(newX + 10)
        viewport.isXAxisBoundsManual = true

        graph.onDataChanged(false, false)
    }

}