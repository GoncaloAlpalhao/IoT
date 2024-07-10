package com.example.dripdropdigital.userinterface

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.example.dripdropdigital.R

/**
 * Displays the home screen of the application, provinding navigation to other app sections
 */
class Home : AppCompatActivity() {

    private lateinit var connect: CardView
    private lateinit var settings: CardView
    private lateinit var goToRasp: CardView
    private lateinit var about: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        connect = findViewById(R.id.connect)
        settings = findViewById(R.id.settings)
        goToRasp = findViewById(R.id.goToRasp)
        about = findViewById(R.id.about)

        val homeIcon: ImageView = findViewById(R.id.homeIcon)

        // Postpone the enter transition until the homeIcon is ready to ensure a smooth effect
        supportPostponeEnterTransition()
        homeIcon.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                homeIcon.viewTreeObserver.removeOnPreDrawListener(this)
                supportStartPostponedEnterTransition()
                return true
            }
        })

        // Set click listeners for navigation to different sections of the app
        connect.setOnClickListener {
            startActivity(Intent(this, SystemList::class.java))
        }
        settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        goToRasp.setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }
        about.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

    }
}