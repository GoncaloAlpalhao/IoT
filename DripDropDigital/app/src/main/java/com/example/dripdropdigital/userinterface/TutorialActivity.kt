package com.example.dripdropdigital.userinterface

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView
import com.example.dripdropdigital.R

/**
 * This class is responsible for displaying the tutorial page of the application
 */
class TutorialActivity : AppCompatActivity() {

    lateinit var connect: CardView
    lateinit var uri: Uri
    lateinit var launchBrowser: android.content.Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        supportActionBar?.hide()
        connect = findViewById(R.id.connectButton)

        connect.setOnClickListener {
            uri = Uri.parse("http://192.168.4.1")
            launchBrowser = Intent(Intent.ACTION_VIEW, uri)
            startActivity(launchBrowser)
        }

    }


}