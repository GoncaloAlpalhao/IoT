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
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainDashboard : AppCompatActivity() {

    lateinit var temp: TextView
    lateinit var hSol: TextView
    lateinit var hAir: TextView
    lateinit var tempCpu: TextView
    lateinit var cityT: TextView
    lateinit var weatherIcon: ImageView
    var weatherIconSet = ""

    private lateinit var series: LineGraphSeries<DataPoint>
    private lateinit var graph: GraphView
    private lateinit var graph2: GraphView
    private lateinit var graph3: GraphView
    private lateinit var graph4: GraphView
    private lateinit var viewport: Viewport
    val x = mutableListOf(0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0)
    val y = mutableListOf(0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0)

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
        weatherIcon = findViewById(R.id.weatherIcon)
        graph = findViewById(R.id.graph1)
        graph2 = findViewById(R.id.graph2)
        graph3 = findViewById(R.id.graph3)
        graph4 = findViewById(R.id.graph4)
        series = LineGraphSeries()
        graph.addSeries(series)
        graph2.addSeries(series)
        graph3.addSeries(series)
        graph4.addSeries(series)
        makeGraph()
        makeGraph2()
        makeGraph3()
        makeGraph4()
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
                            series.resetData(arrayOf(DataPoint(0.0, 0.0)))
                            makeGraph()
                        }
                    }.start()
                }
            }
        }
        val apiKey = "9ec6b8792977153a9db9dc83d2134ec5"
        var city = "Tomar"
        getCurrentWeather(apiKey, city) { weatherDescription ->
            runOnUiThread {
                cityT.text = weatherDescription
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

    fun makeGraph() {
        // Give x and y axises their range
        viewport = graph.viewport
        graph.gridLabelRenderer.setHumanRounding(false)
        viewport.isYAxisBoundsManual = true
        viewport.setMinX(0.0)
        viewport.setMaxX(100.0)
        viewport.setMinY(0.0)
        viewport.setMaxY(100.0)
        viewport.isScrollable = true

        val staticLabelsFormatter   = StaticLabelsFormatter(graph)
        staticLabelsFormatter.setHorizontalLabels(
            arrayOf(
                "0",
                "10",
                "20",
                "30",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
            )
        )
        staticLabelsFormatter.setVerticalLabels(
            arrayOf(
                "0",
                "10",
                "20",
                "30",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
            )
        )


        for (i in 0..x.size - 1) {
            series.appendData(
                DataPoint(
                    x[i],
                    y.shuffled()[i]
                ),
                true,
                100
            )
        }


    }

    fun makeGraph2(){
        // Give x and y axises their range
        viewport = graph2.viewport
        graph2.gridLabelRenderer.setHumanRounding(false)
        viewport.isYAxisBoundsManual = true
        viewport.setMinX(0.0)
        viewport.setMaxX(100.0)
        viewport.setMinY(0.0)
        viewport.setMaxY(100.0)
        viewport.isScrollable = true

        val staticLabelsFormatter   = StaticLabelsFormatter(graph2)
        staticLabelsFormatter.setHorizontalLabels(
            arrayOf(
                "0",
                "10",
                "20",
                "30",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
            )
        )
        staticLabelsFormatter.setVerticalLabels(
            arrayOf(
                "0",
                "10",
                "20",
                "30",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
            )
        )
    }

    fun makeGraph3(){
        // Give x and y axises their range
        viewport = graph3.viewport
        graph3.gridLabelRenderer.setHumanRounding(false)
        viewport.isYAxisBoundsManual = true
        viewport.setMinX(0.0)
        viewport.setMaxX(100.0)
        viewport.setMinY(0.0)
        viewport.setMaxY(100.0)
        viewport.isScrollable = true

        val staticLabelsFormatter   = StaticLabelsFormatter(graph3)
        staticLabelsFormatter.setHorizontalLabels(
            arrayOf(
                "0",
                "10",
                "20",
                "30",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
            )
        )
        staticLabelsFormatter.setVerticalLabels(
            arrayOf(
                "0",
                "10",
                "20",
                "30",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
            )
        )
    }

    fun makeGraph4(){
        // Give x and y axises their range
        viewport = graph4.viewport
        graph4.gridLabelRenderer.setHumanRounding(false)
        viewport.isYAxisBoundsManual = true
        viewport.setMinX(0.0)
        viewport.setMaxX(100.0)
        viewport.setMinY(0.0)
        viewport.setMaxY(100.0)
        viewport.isScrollable = true

        val staticLabelsFormatter   = StaticLabelsFormatter(graph4)
        staticLabelsFormatter.setHorizontalLabels(
            arrayOf(
                "0",
                "10",
                "20",
                "30",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
            )
        )
        staticLabelsFormatter.setVerticalLabels(
            arrayOf(
                "0",
                "10",
                "20",
                "30",
                "40",
                "50",
                "60",
                "70",
                "80",
                "90",
                "100"
            )
        )
    }

}