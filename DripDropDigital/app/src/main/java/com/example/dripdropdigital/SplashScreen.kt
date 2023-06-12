package com.example.dripdropdigital

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)

        supportActionBar?.hide()

        // Get the ImageView from the layout file
        val imageView = findViewById<ImageView>(R.id.imageView2)

        Handler().postDelayed({
            // Create animation to zoom out on the image
            val animation = ScaleAnimation(1f, 10f, 1f, 10f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 4f)
            animation.duration = 4000
            animation.fillAfter = true
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    val intent = Intent(this@SplashScreen, Home::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onAnimationEnd(animation: Animation?) {

                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            // Apply the animation to the ImageView
            imageView.startAnimation(animation)

        }, 1000)

    }
}