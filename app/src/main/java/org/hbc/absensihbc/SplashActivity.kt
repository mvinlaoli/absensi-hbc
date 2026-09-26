package org.hbc.absensihbc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logoSplash)
        val title = findViewById<TextView>(R.id.titleSplash)
        val subtitle = findViewById<TextView>(R.id.subtitleSplash)

        // Animasi logo: fade in + scale up
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        logo.startAnimation(fadeIn)
        logo.startAnimation(scaleUp)

        // Animasi teks: slide up (delay 500ms)
        Handler(Looper.getMainLooper()).postDelayed({
            val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            title.startAnimation(slideUp)
            subtitle.startAnimation(slideUp)
        }, 500)

        // Delay total 2.5 detik → ke Login
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2500)
    }
}