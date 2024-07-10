package com.example.dripdropdigital.userinterface

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.dripdropdigital.R

/**
 * Displays the about page of the application
 */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Hide the action bar for a cleaner look on the about page
        supportActionBar?.hide()
    }
}