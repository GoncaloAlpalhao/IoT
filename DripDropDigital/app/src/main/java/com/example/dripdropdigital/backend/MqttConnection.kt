package com.example.dripdropdigital.backend

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.dripdropdigital.R
import com.example.dripdropdigital.systems.SystemItem
import com.example.dripdropdigital.template.MainActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * Handles the connection to the MQTT broker
 */
class MqttConnection(context: Context, system: SystemItem) : AppCompatActivity() {

    private val ipAddress: String = system.ipAddress.toString()
    private val clientId: String = system.clientId.toString()
    private val username: String = system.username.toString()
    private val password: String = system.password.toString()
    private var lastWillTopic: String = system.lastWillTopic.toString()
    private var lastWillPayload: String = system.lastWillPayload.toString()
    private val minHumidity: String = system.minHumidity.toString()

    var temp: String = "0"
    var hAr: String = "0"
    var hSol: String = "0"
    var tempCpu: String = "0"
    var waterState: String = "Unknown"
    var isConected: Boolean = false
    var manMode = "Unknown"
    var notifSent = false

    // Create a variable to hold the mqtt client
    lateinit var mqttClient: MqttAndroidClient

    /**
     * Retrieves the last message received from the mqtt broker for a given topic
     * @param topic The topic to get the message from
     * @return The last message received from the mqtt broker
     */
    fun newMessage(topic: String): String {
        when (topic) {
            "temperatura" -> {
                return temp
            }

            "humidadeAr" -> {
                return hAr
            }

            "humidadeSolo" -> {
                return hSol
            }

            "temperaturaCpu" -> {
                return tempCpu
            }

            "sistemaRega" -> {
                return waterState
            }

            "manMode" -> {
                return manMode
            }

            else -> return "Error"
        }
    }

    /**
     * Connects to the mqtt broker
     * @param context The context of the activity
     * @param onConnect The function to run when the connection is successful
     */
    fun connect(context: Context, onConnect: (String) -> Unit = {}) {

        val serverURI = "tcp://$ipAddress"
        mqttClient = MqttAndroidClient(context, serverURI, clientId)
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                connect(context)
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                //Toast.makeText(context, "Message received", Toast.LENGTH_SHORT).show()
                if (topic == "temperatura") {
                    temp = message.toString()
                } else if (topic == "humidadeAr") {
                    hAr = message.toString()
                } else if (topic == "humidadeSolo") {
                    hSol = message.toString().trim()
                    if (hSol.toFloat().toInt() < minHumidity.toInt()) {
                        if (!notifSent) {
                            enviarNotif(context)
                            notifSent = true
                            Thread {
                                Thread.sleep(60000)
                                notifSent = false
                            }.start()
                        }

                    }
                } else if (topic == "temperaturaCpu") {
                    tempCpu = message.toString()
                } else if (topic == "sistemaRega") {
                    waterState = message.toString()
                } else if (topic == "manMode") {
                    manMode = message.toString()
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
        lastWillTopic = "LED"
        options.setWill(lastWillTopic, lastWillPayload.toByteArray(), 0, false)

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    //Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
                    isConected = true
                    mqttClient.subscribe("temperatura", 1)
                    mqttClient.subscribe("humidadeAr", 1)
                    mqttClient.subscribe("humidadeSolo", 1)
                    mqttClient.subscribe("temperaturaCpu", 1)
                    mqttClient.subscribe("sistemaRega", 1)
                    mqttClient.subscribe("manMode", 1)
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

    /**
     * Publishes a message to the mqtt broker
     * @param topic The topic to publish the message to
     * @param msg The message to publish
     * @param qos The quality of service to use
     * @param retained Whether the message should be retained
     */
    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        if (!mqttClient.isConnected) {
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

    /**
     * Disconnects from the mqtt broker
     * @param context The context of the activity
     */
    fun disconnect(context: Context) {
        if (!mqttClient.isConnected) {
            Toast.makeText(this, "No conection bruh \uD83E\uDD28", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(MainActivity.TAG, "Disconnected")
                    Toast.makeText(
                        context,
                        "Bro killed the connection \uD83D\uDD2A",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(MainActivity.TAG, "Failed to disconnect")
                    Toast.makeText(
                        context,
                        "Bro can't run \uD83D\uDE28 Failed to Disconnect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /**
     * Subscribes to a topic on the mqtt broker
     * @param topic The topic to subscribe to
     * @param qos The quality of service to use
     */
    fun subscribe(topic: String, qos: Int = 1) {
        if (!mqttClient.isConnected) {
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

    /**
     * Sends a notification to the user
     * @param context The context of the activity
     */
    private fun enviarNotif(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "Notificação Humidade"
            val channelName = "Notificações Acerca da Humidade do Solo"
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(context, "Notificação Humidade")
            .setSmallIcon(R.drawable.dripdropdigitalsmol)
            .setContentTitle("Alerta da Humidade do Solo \uD83D\uDCA7")
            .setContentText("A humidade do solo está abaixo do limite definido. Verifique o estado do sistema de rega.")
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }

}