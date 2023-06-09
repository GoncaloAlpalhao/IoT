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
            // Create a ScaleAnimation to zoom in on the image
            val animation = ScaleAnimation(1f, 50f, 1f, 50f, Animation.RELATIVE_TO_SELF, 0.495f, Animation.RELATIVE_TO_SELF, 0.47f)
            animation.duration = 3000
            animation.fillAfter = true
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    val intent = Intent(this@SplashScreen, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }

                override fun onAnimationEnd(animation: Animation?) {

                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            // Apply the animation to the ImageView
            imageView.startAnimation(animation)

        }, 1250)

    }
}