package com.example.dripdropdigital

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.series.DataPoint
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttConnection(context: Context) : AppCompatActivity() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("mqtt_settings", MODE_PRIVATE)

    private val ipAddress: String = sharedPreferences.getString("ip_address", "") ?: "0.0.0.0"
    private val clientId: String = sharedPreferences.getString("client_id", "") ?: "123"
    private val username: String = sharedPreferences.getString("username", "") ?: "test"
    private val password: String = sharedPreferences.getString("password", "") ?: "123"
    private val lastWillTopic: String = sharedPreferences.getString("last_will_topic", "") ?: "ltt"
    private val lastWillPayload: String = sharedPreferences.getString("last_will_payload", "") ?: "ltt"

    var temp: String = "0"
    var hAr: String = "0"
    var hSol: String = "0"
    var tempCpu: String = "0"
    var waterState: String = "Unknown"
    var isConected: Boolean = false

    // Create a variable to hold the mqtt client
    lateinit var mqttClient: MqttAndroidClient

    fun newMessage(topic: String): String {
        if (topic == "temperatura"){
            return temp
        }else if (topic == "humidadeAr"){
            return hAr
        }else if (topic == "humidadeSolo"){
            return hSol
        }else if (topic == "temperaturaCpu"){
            return tempCpu
        }else if (topic == "sistemaRega"){
            return waterState
        }
        return "Error"
    }

    // Create a function to connect to the mqtt broker
    fun connect(context: Context, onConnect: (String) -> Unit = {}) {
        val serverURI = "tcp://$ipAddress"
        mqttClient = MqttAndroidClient(context, serverURI, clientId)
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                connect(context)
            }
            override fun messageArrived(topic: String, message: MqttMessage) {
                //Toast.makeText(context, "Message received", Toast.LENGTH_SHORT).show()
                if (topic == "temperatura"){
                    temp = message.toString()
                }else if (topic == "humidadeAr"){
                    hAr = message.toString()
                }else if (topic == "humidadeSolo"){
                    hSol = message.toString()
                }else if (topic == "temperaturaCpu"){
                    tempCpu = message.toString()
                }else if (topic == "sistemaRega"){
                    waterState = message.toString()
                }
                onConnect("Message")
            }
            override fun deliveryComplete(token: IMqttDeliveryToken) {
                //Toast.makeText(context, "Message delivered", Toast.LENGTH_SHORT).show()
            }
        })
        val options = MqttConnectOptions()
        options.userName = username
        options.password = password.toCharArray()
        options.isAutomaticReconnect = true
        options.setWill(lastWillTopic, lastWillPayload.toByteArray(), 0, false)
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    //Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
                    isConected = true
                    mqttClient.subscribe("temperatura",1)
                    mqttClient.subscribe("humidadeAr",1)
                    mqttClient.subscribe("humidadeSolo",1)
                    mqttClient.subscribe("temperaturaCpu",1)
                    mqttClient.subscribe("sistemaRega",1)
                    onConnect("Success")

                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    isConected = false
                    onConnect("Failed")
                }
            })
        } catch (e: Exception) {
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
                    Log.d(MainActivity.TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(MainActivity.TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect(context: Context) {
        if(!mqttClient.isConnected){
            Toast.makeText(this, "No conection bruh \uD83E\uDD28", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(MainActivity.TAG, "Disconnected")
                    Toast.makeText(context, "Bro killed the connection \uD83D\uDD2A", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(MainActivity.TAG, "Failed to disconnect")
                    Toast.makeText(context, "Bro can't run \uD83D\uDE28 Failed to Disconnect", Toast.LENGTH_SHORT).show()
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
                    Log.d(MainActivity.TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(MainActivity.TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}