package com.example.dripdropdigital

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.Locale


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
    var flagSwitch = true
    var flagSwitch2 = true
    var cityTemp = ""
    lateinit var rainState: String
    lateinit var mainLayout: LinearLayout
    lateinit var manState: TextView
    lateinit var switchRega: Switch
    lateinit var switchModo: Switch

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

        // Get the extra from the intent
        val system = intent.getSerializableExtra("system") as SystemItem

        val loadingLayout = findViewById<FrameLayout>(R.id.loadingLayout)
        var loadingGif = findViewById<LottieAnimationView>(R.id.lottie)
        loadingLayout.visibility = View.VISIBLE
        var mqttTest = MqttConnection(this, system)
        temp = findViewById(R.id.temperature)
        hSol = findViewById(R.id.humiditySoil)
        hAir = findViewById(R.id.humidityAir)
        tempCpu = findViewById(R.id.temperatureCPU)
        cityT = findViewById(R.id.city)
        ledState = findViewById(R.id.ledState)
        weatherIcon = findViewById(R.id.weatherIcon)

        mainLayout = findViewById(R.id.mainLayout)
        manState = findViewById(R.id.manState)
        switchRega = findViewById(R.id.regaSwitch)
        switchModo = findViewById(R.id.manSwitch)

        switchRega.setOnCheckedChangeListener { _, isChecked ->
            if (flagSwitch){
                if (isChecked) {
                    mqttTest.publish("LED", "on")
                } else {
                    mqttTest.publish("LED", "off")
                }
            }
        }

        switchModo.setOnCheckedChangeListener { _, isChecked ->
            if (flagSwitch2){
                if (isChecked) {
                    mqttTest.publish("LED", "manual", retained = true)
                } else {
                    mqttTest.publish("LED", "automatic", retained = true)
                }
            }
        }

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
                    if (humidadeSolo.toFloat() < 20) {
                        hSol.setTextColor(Color.parseColor("#800020"))
                    } else if (humidadeSolo.toFloat() > 70) {
                        hSol.setTextColor(Color.parseColor("#00C6AF"))
                    } else {
                        hSol.setTextColor(Color.BLACK)
                    }
                    hSol.text = humidadeSolo + "%"

                    var temperaturaCPU = mqttTest.newMessage("temperaturaCpu")
                    makeGraph4(temperaturaCPU)
                    tempCpu.text = temperaturaCPU + "°C"

                    var sistemaRega = mqttTest.newMessage("sistemaRega")
                    if(sistemaRega == "on"){
                        flagSwitch = false
                        ledState.text = "Ligado"
                        switchRega.isChecked = true
                        ledState.setTextColor(Color.parseColor("#00C6AF"))
                    }else if(sistemaRega == "off"){
                        flagSwitch = false
                        ledState.text = "Desligado"
                        switchRega.isChecked = false
                        ledState.setTextColor(Color.parseColor("#800020"))
                    }else{
                        ledState.text = sistemaRega
                        switchRega.isChecked = false
                    }
                    Thread {
                        Thread.sleep(500)
                        flagSwitch = true
                    }.start()

                    var modoRega = mqttTest.newMessage("manMode")
                    if(modoRega == "manual"){
                        flagSwitch2 = false
                        manState.text = "Manual"
                        switchModo.isChecked = true
                    }else if(modoRega == "automatico"){
                        flagSwitch2 = false
                        manState.text = "Automático"
                        switchModo.isChecked = false
                    }else{
                        manState.text = modoRega
                        switchModo.isChecked = false
                    }
                    Thread {
                        Thread.sleep(500)
                        flagSwitch2 = true
                    }.start()

                }else if (callBack == "Failed"){
                    //Set layout width to match parent
                    loadingGif.setAnimation(R.raw.failed)
                    loadingGif.playAnimation()
                    loadingGif.loop(false)
                    Toast.makeText(this, "Failed to connect to MQTT Broker", Toast.LENGTH_SHORT).show()
                    Thread {
                        Thread.sleep(1500)
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

                    //Toast.makeText(this, "Connected to MQTT Broker", Toast.LENGTH_SHORT).show()
                    Thread {
                        Thread.sleep(1500)
                        runOnUiThread {
                            TransitionManager.beginDelayedTransition(mainLayout, AutoTransition())
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
        val apiKey = "3328730c2c8279b83f61086f771f48f4"
        val geocoder = Geocoder(this, Locale.getDefault())
        val lat = system.location?.split(",")?.get(0)?.toDouble()
        val long = system.location?.split(",")?.get(1)?.toDouble()
        val addresses: MutableList<Address>? = lat?.let { long?.let { it1 ->
            geocoder.getFromLocation(it,
                it1, 1)
        } }
        val adress: Address? = addresses?.getOrNull(0)
        val city = adress?.locality
        if (city != null) {
            getCurrentWeather(apiKey, city) { weatherDescription ->
                runOnUiThread {
                    cityT.text = "$city\t\t$cityTemp\n$weatherDescription"
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
                cityTemp = jsonObject.getJSONObject("main").getString("temp").substring(0,2) + "°C"
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
        val minY = -10.0 // Minimum value for Y-axis
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