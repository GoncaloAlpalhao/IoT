package com.example.dripdropdigital.userinterface

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dripdropdigital.systems.PlantDataUtil
import com.example.dripdropdigital.R

/**
 * This class is responsible for displaying the settings page of the application
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // Initialize the SharedPreferences instance
        sharedPreferences = getSharedPreferences("mqtt_settings", MODE_PRIVATE)

        // Load the saved values into the corresponding views
        val ipAddress = sharedPreferences.getString("ip_address", "0.0.0.0")
        val ipAddressEditText = findViewById<EditText>(R.id.ip_address_edit_text)
        ipAddressEditText.setText(ipAddress)

        val clientId = sharedPreferences.getString("client_id", "123")
        val clientIdEditText = findViewById<EditText>(R.id.client_id_edit_text)
        clientIdEditText.setText(clientId)

        val username = sharedPreferences.getString("username", "test")
        val usernameEditText = findViewById<EditText>(R.id.username_edit_text)
        usernameEditText.setText(username)

        val password = sharedPreferences.getString("password", "123")
        val passwordEditText = findViewById<EditText>(R.id.password_edit_text)
        passwordEditText.setText(password)

        val lastWillTopic = sharedPreferences.getString("last_will_topic", "ltt")
        val lastWillTopicEditText = findViewById<EditText>(R.id.last_will_topic_edit_text)
        lastWillTopicEditText.setText(lastWillTopic)

        val lastWillPayload = sharedPreferences.getString("last_will_payload", "ltt")
        val lastWillPayloadEditText = findViewById<EditText>(R.id.last_will_message_edit_text)
        lastWillPayloadEditText.setText(lastWillPayload)

        val minHumidity = sharedPreferences.getString("min_humidity", "55")
        val minHumidityEditText = findViewById<EditText>(R.id.min_humidity_edit_text)
        minHumidityEditText.setText(minHumidity)

        val maxHumidity = sharedPreferences.getString("max_humidity", "75")
        val maxHumidityEditText = findViewById<EditText>(R.id.max_humidity_edit_text)
        maxHumidityEditText.setText(maxHumidity)

        val spinner = findViewById<Spinner>(R.id.spinner)

        spinner.post {
            val plantType = sharedPreferences.getString("plant_type", null)

            val plantTypeIndex = PlantDataUtil.plantTypes.indexOfFirst { it.name == plantType }
            if (plantType != null && plantTypeIndex >= 0) {
                spinner.setSelection(plantTypeIndex)
            }
        }

        supportActionBar?.hide()

        // Set up the "Apply" button click listener to save the updated settings
        val applyButton = findViewById<Button>(R.id.save_button)
        applyButton.setOnClickListener {
            // Verify that the min humidity is less than the max humidity
            if (minHumidityEditText.text.toString()
                    .toDouble() >= maxHumidityEditText.text.toString().toDouble()
            ) {
                Toast.makeText(
                    this,
                    "Min humidade deve ser menor que a max humidade",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            saveSettings()
        }

        // set upt the types of plants
        val plantTypes = PlantDataUtil.plantTypes

        // set up the spinner for the plant types showing the name of the plant
        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                R.layout.list_item, plantTypes.map { it.name })
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {

                    // change the min humidity value to the selected plant
                    minHumidityEditText.setText(plantTypes[position].minHumidity.toString())

                    // change the max humidity value to the selected plant
                    maxHumidityEditText.setText(plantTypes[position].maxHumidity.toString())

                    // if the plant type selected is other than "Personalizado" then the min and max humidity values are disabled
                    if (plantTypes[position].name != "Personalizado") {
                        minHumidityEditText.isEnabled = false
                        maxHumidityEditText.isEnabled = false
                        // change their cards background to gray
                        minHumidityEditText.setBackgroundResource(R.drawable.edit_text_background_disabled)
                        maxHumidityEditText.setBackgroundResource(R.drawable.edit_text_background_disabled)
                    } else {
                        minHumidityEditText.isEnabled = true
                        maxHumidityEditText.isEnabled = true
                        // change their cards background to white
                        minHumidityEditText.setBackgroundResource(R.drawable.edit_text_background)
                        maxHumidityEditText.setBackgroundResource(R.drawable.edit_text_background)
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }

    // ...

    /**
     * Saves the settings to SharedPreferences
     */
    private fun saveSettings() {
        val editor = sharedPreferences.edit()

        val ipAddressEditText = findViewById<EditText>(R.id.ip_address_edit_text)
        editor.putString("ip_address", ipAddressEditText.text.toString())

        val clientIdEditText = findViewById<EditText>(R.id.client_id_edit_text)
        editor.putString("client_id", clientIdEditText.text.toString())

        val usernameEditText = findViewById<EditText>(R.id.username_edit_text)
        editor.putString("username", usernameEditText.text.toString())

        val passwordEditText = findViewById<EditText>(R.id.password_edit_text)
        editor.putString("password", passwordEditText.text.toString())

        val lastWillTopicEditText = findViewById<EditText>(R.id.last_will_topic_edit_text)
        editor.putString("last_will_topic", lastWillTopicEditText.text.toString())

        val lastWillPayloadEditText = findViewById<EditText>(R.id.last_will_message_edit_text)
        editor.putString("last_will_payload", lastWillPayloadEditText.text.toString())

        val minHumidityEditText = findViewById<EditText>(R.id.min_humidity_edit_text)
        editor.putString("min_humidity", minHumidityEditText.text.toString())

        val maxHumidityEditText = findViewById<EditText>(R.id.max_humidity_edit_text)
        editor.putString("max_humidity", maxHumidityEditText.text.toString())

        val spinner = findViewById<Spinner>(R.id.spinner)
        editor.putString("plant_type", spinner.selectedItem.toString())

        editor.apply()

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}