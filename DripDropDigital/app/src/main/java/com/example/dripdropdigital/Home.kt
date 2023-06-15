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
    lateinit var about: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        connect = findViewById(R.id.connect)
        settings = findViewById(R.id.settings)
        goToRasp = findViewById(R.id.goToRasp)
        about = findViewById(R.id.about)

        connect.setOnClickListener {
            val intent = Intent(this, MainDashboard::class.java)
            startActivity(intent)
        }

        settings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        goToRasp.setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)
        }

        about.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

    }
}