package com.example.dripdropdigital

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MainActivity : AppCompatActivity() {

    lateinit var listView: ListView
    var list: ArrayList<String> = ArrayList()
    lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var ipAddress: String
    private lateinit var clientId: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var lastWillTopic: String
    private lateinit var lastWillPayload: String

    // on below line we are creating
    // variables for our graph view
    lateinit var lineGraphView: GraphView
    var latestX: Double = 0.0
    lateinit var series: LineGraphSeries<DataPoint>

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize sharedPreferences
        sharedPreferences = getSharedPreferences("mqtt_settings", MODE_PRIVATE)

        // Retrieve values from sharedPreferences
        ipAddress = sharedPreferences.getString("ip_address", "") ?: "0.0.0.0"
        if(ipAddress.isEmpty())
            ipAddress = "0.0.0.0"
        clientId = sharedPreferences.getString("client_id", "") ?: "123"
        if(clientId.isEmpty())
            clientId = "123"
        username = sharedPreferences.getString("username", "") ?: "test"
        if(username.isEmpty())
            username = "test"
        password = sharedPreferences.getString("password", "") ?: "123"
        if(password.isEmpty())
            password = "123"
        lastWillTopic = sharedPreferences.getString("last_will_topic", "") ?: "ltt"
        if(lastWillTopic.isEmpty())
            lastWillTopic = "ltt"
        lastWillPayload = sharedPreferences.getString("last_will_payload", "") ?: "ltt"
        if(lastWillPayload.isEmpty())
            lastWillPayload = "ltt"

        // on below line we are initializing
        // our variable with their ids.
        lineGraphView = findViewById(R.id.idGraphView)

        // on below line we are adding data to our graph view.
        series = LineGraphSeries(
            arrayOf(
                // on below line we are adding
                // each point on our x and y axis.
                DataPoint(latestX++, 30.0),
                DataPoint(latestX++, 29.0)
            )
        )

        // on below line adding animation
        lineGraphView.animate()

        // on below line we are setting scrollable
        // for point graph view
        lineGraphView.viewport.isScrollable = false

        // on below line we are setting scalable.
        lineGraphView.viewport.isScalable = true

        // on below line we are setting scalable y
        lineGraphView.viewport.setScalableY(true)

        // on below line we are setting scrollable y
        lineGraphView.viewport.setScrollableY(false)

        lineGraphView.viewport.setMaxX(20.0)

        lineGraphView.viewport.setMinX(0.0)

        // on below line we are setting color for series. I need #FF0000 (RED)
        series.color = getColor(R.color.light_organge)

        // on below line we are adding
        // data series to our graph view.
        lineGraphView.addSeries(series)

        supportActionBar?.hide()
        listView = findViewById(R.id.lista)
        arrayAdapter = ArrayAdapter(this, R.layout.list_item, R.id.text1, list)
        list.add("Listing test")
        arrayAdapter.notifyDataSetChanged()
        listView.adapter = arrayAdapter
        mqttClient = MqttAndroidClient(null, null, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Geral"
            val descriptionText = "Notificações geral da aplicação"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("dripdrop", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        var builder = NotificationCompat.Builder(this, "dripdrop")
            .setSmallIcon(R.drawable.dripdropdigitalsmol)
            .setContentTitle("Plant Dehydrated \uD83D\uDCA7")
            .setContentText("Your soil moisture level is too low, please check the watering system")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        val connectBtn: Button = findViewById(R.id.connect)
        connectBtn.setOnClickListener{
            connect(this)
        }

        val publishBtn: Button = findViewById(R.id.publish)
        publishBtn.setOnClickListener{
            publish("temperatura","teste",1,false)
        }

        val disconnectBtn: Button = findViewById(R.id.disconnect)
        disconnectBtn.setOnClickListener{
            disconnect()
        }

        val onBtn: Button = findViewById(R.id.on)
        onBtn.setOnClickListener{
            if(mqttClient.isConnected){
                publish("LED","on",1,true)
            }else{
                Toast.makeText(this, "No conection bruh \uD83E\uDD28", Toast.LENGTH_SHORT).show()
                with(NotificationManagerCompat.from(this)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(0, builder.build())
                }
            }
        }

        val offBtn: Button = findViewById(R.id.off)
        offBtn.setOnClickListener{
            if(mqttClient.isConnected){
                publish("LED","off",1,true)
            }else{
                Toast.makeText(this, "No conection bruh \uD83E\uDD28", Toast.LENGTH_SHORT).show()
            }
        }

        val settingsBtn: Button = findViewById(R.id.subscribe)
        settingsBtn.setOnClickListener{
            /*listView = findViewById(R.id.lista)
            arrayAdapter = ArrayAdapter(this, R.layout.list_item, R.id.text1, list)
            list.add("Random shit go!")
            arrayAdapter.notifyDataSetChanged()
            listView.adapter = arrayAdapter*/
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }

    private lateinit var mqttClient: MqttAndroidClient
    // TAG
    companion object {
        const val TAG = "AndroidMqttClient"
    }

    fun newMessage(message: String){
        val regex = Regex("""\d+(\.\d+)?""") // matches any decimal number

        val matchResult = regex.find(message)
        val numberString = matchResult?.value // the matched number string, e.g. "238.56"

        if(!numberString.isNullOrEmpty()){
            val number = numberString.toDouble() // parse the number string to a Double or return null
            // Create a new data point with x = current X value + 1 and y = new Y value.
            val newDataPoint = DataPoint(latestX++, number)
            // Append the new data point to the series.
            series.appendData(newDataPoint, false, 20)
        }
        listView = findViewById(R.id.lista)
        arrayAdapter = ArrayAdapter(this, R.layout.list_item, R.id.text1, list)
        list.add(1, message)
        arrayAdapter.notifyDataSetChanged()
        listView.adapter = arrayAdapter
    }

    fun connect(context: Context) {
        //val serverURI = "tcp://192.168.1.119:1883"
        val serverURI = "tcp://$ipAddress"
        if(mqttClient.isConnected){
            Toast.makeText(this, "Already Connected?? \uD83D\uDE11", Toast.LENGTH_SHORT).show()
            return
        }
        mqttClient = MqttAndroidClient(context, serverURI, clientId)
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic")
                newMessage(message.toString())
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
                Toast.makeText(this@MainActivity, "Connection died for a reason \uD83D\uDE1E", Toast.LENGTH_SHORT).show()
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        options.userName = username
        options.password = password.toCharArray()
        options.setWill(lastWillTopic, lastWillPayload.toByteArray(), 0, false)
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    mqttClient.subscribe("temperatura",1)
                    Toast.makeText(this@MainActivity, "Bro got the connection successfully \uD83D\uDC4D", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure")
                    Toast.makeText(this@MainActivity, "Bro did not get the connection \uD83D\uDE2B", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        if(!mqttClient.isConnected){
            Toast.makeText(this, "No conection bruh \uD83E\uDD28", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        if(!mqttClient.isConnected){
            Toast.makeText(this, "No conection bruh \uD83E\uDD28", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                    Toast.makeText(this@MainActivity, "Bro killed the connection \uD83D\uDD2A", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                    Toast.makeText(this@MainActivity, "Bro can't run \uD83D\uDE28 Failed to Disconnect", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        if(!mqttClient.isConnected){
            Toast.makeText(this, "No conection bruh \uD83E\uDD28", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


}