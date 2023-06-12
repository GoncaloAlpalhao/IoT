package com.example.dripdropdigital

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttConnection(context: Context) : AppCompatActivity() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("mqtt_settings", MODE_PRIVATE)

    private val ipAddress: String = sharedPreferences.getString("ip_address", "") ?: "0.0.0.0"
    private val clientId: String = sharedPreferences.getString("client_id", "") ?: "123"
    private val username: String = sharedPreferences.getString("username", "") ?: "test"
    private val password: String = sharedPreferences.getString("password", "") ?: "123"
    private val lastWillTopic: String = sharedPreferences.getString("last_will_topic", "") ?: "ltt"
    private val lastWillPayload: String = sharedPreferences.getString("last_will_payload", "") ?: "ltt"

    // Create a variable to hold the mqtt client
    lateinit var mqttClient: MqttAndroidClient

    // Create a function to connect to the mqtt broker
    fun connect(context: Context){
        val serverURI = "tcp://$ipAddress"
        mqttClient = MqttAndroidClient(context, serverURI, clientId)
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                connect(context)
            }
            override fun messageArrived(topic: String, message: MqttMessage) {
                Toast.makeText(context, "Message received", Toast.LENGTH_SHORT).show()
            }
            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Toast.makeText(context, "Message delivered", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}