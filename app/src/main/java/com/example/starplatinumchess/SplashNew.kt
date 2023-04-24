package com.example.starplatinumchess

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class SplashNew : AppCompatActivity() {
    private lateinit var imgv: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_new)
        imgv = findViewById<LottieAnimationView>(R.id.lot)
        Handler().postDelayed({
            val intent = Intent(this@SplashNew, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}