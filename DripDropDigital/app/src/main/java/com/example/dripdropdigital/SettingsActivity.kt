package com.example.dripdropdigital

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

        // Set up the "Apply" button click listener to save the updated settings
        val applyButton = findViewById<Button>(R.id.save_button)
        applyButton.setOnClickListener {
            saveSettings()
        }
        supportActionBar?.hide()
    }

    // ...

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

        editor.apply()

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}