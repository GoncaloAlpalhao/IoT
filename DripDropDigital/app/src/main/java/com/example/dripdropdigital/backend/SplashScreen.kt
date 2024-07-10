package com.example.dripdropdigital.backend

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.example.dripdropdigital.R
import com.example.dripdropdigital.userinterface.Home

/**
 * Display the splash screen of the application
 */
class SplashScreen : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 1000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)
        supportActionBar?.hide()

        // Get the ImageView from the layout file
        val imageView = findViewById<ImageView>(R.id.imageView2)

        // Post delayed transition to home screen after the specified delay
        Handler().postDelayed({
            val intent = Intent(this@SplashScreen, Home::class.java)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@SplashScreen,
                Pair.create(imageView, "iconTransition")
            )
            startActivity(intent, options.toBundle())
            //Delay the finish of the splash screen activity for 1 second
            Thread {
                Thread.sleep(500)
                finish()
            }.start()
        }, SPLASH_DELAY)
    }
}