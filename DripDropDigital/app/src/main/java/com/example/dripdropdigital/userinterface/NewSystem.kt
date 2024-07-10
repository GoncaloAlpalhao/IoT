package com.example.dripdropdigital.userinterface

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dripdropdigital.backend.LocalStorage
import com.example.dripdropdigital.systems.PlantDataUtil
import com.example.dripdropdigital.R

/**
 * This class is responsible for displaying the new system page of the application
 */
class NewSystem : AppCompatActivity(), LocationListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private lateinit var location: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_system)

        // Get the location
        getLocation()

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

        spinner.post{
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
            if (minHumidityEditText.text.toString().toDouble() >= maxHumidityEditText.text.toString().toDouble()) {
                Toast.makeText(this, "Min humidade deve ser menor que a max humidade", Toast.LENGTH_SHORT).show()
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
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {

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
                }
            }
        }
    }

    // ...

    /**
     * Save the settings of the new system.
     */
    private fun saveSettings() {
        // Get the values from the views
        val title = findViewById<EditText>(R.id.title_text)
        val ipAddressEditText = findViewById<EditText>(R.id.ip_address_edit_text)
        val clientIdEditText = findViewById<EditText>(R.id.client_id_edit_text)
        val usernameEditText = findViewById<EditText>(R.id.username_edit_text)
        val passwordEditText = findViewById<EditText>(R.id.password_edit_text)
        val lastWillTopicEditText = findViewById<EditText>(R.id.last_will_topic_edit_text)
        val lastWillPayloadEditText = findViewById<EditText>(R.id.last_will_message_edit_text)
        val minHumidityEditText = findViewById<EditText>(R.id.min_humidity_edit_text)
        val maxHumidityEditText = findViewById<EditText>(R.id.max_humidity_edit_text)
        val spinner = findViewById<Spinner>(R.id.spinner)

        // Get the selected plant type
        val plantType = PlantDataUtil.plantTypes[spinner.selectedItemPosition].name

        if(location == null) {
            Toast.makeText(this, "Por favor aguarde ser localizado", Toast.LENGTH_SHORT).show()
            return
        }

        val system = "${title.text}|$location|${ipAddressEditText.text}|${clientIdEditText.text}|${usernameEditText.text}|${passwordEditText.text}|${lastWillTopicEditText.text}|${lastWillPayloadEditText.text}|${minHumidityEditText.text}|${maxHumidityEditText.text}|$plantType"

        LocalStorage.addPlant(this, system)

        Toast.makeText(this, "Sistema adicionado com sucesso", Toast.LENGTH_SHORT).show()
        finish()

    }

    /**
     * Get the location of the device.
     */
    private fun getLocation() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
            return
        }
        // If location permission is already granted, request location updates
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 500, 0f, this)
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0f, this)
    }

    /**
     * Called when the location has changed.
     * @param location The new location.
     */
    override fun onLocationChanged(location: Location) {
        this.location = location.latitude.toString() + "," + location.longitude.toString()
    }
}