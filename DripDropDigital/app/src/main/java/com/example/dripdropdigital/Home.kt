package com.example.dripdropdigital

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView

class Home : AppCompatActivity() {

    lateinit var connect: CardView
    lateinit var settings: CardView
    lateinit var goToRasp: CardView
    lateinit var uri: Uri
    lateinit var launchBrowser: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        connect = findViewById(R.id.connect)
        settings = findViewById(R.id.settings)
        goToRasp = findViewById(R.id.goToRasp)

        connect.setOnClickListener {
            val intent = Intent(this, MainDashboard::class.java)
            startActivity(intent)
        }

        settings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        goToRasp.setOnClickListener {
            uri = Uri.parse("http://192.168.4.1")
            launchBrowser = Intent(Intent.ACTION_VIEW, uri)
            startActivity(launchBrowser)

        }

    }
}