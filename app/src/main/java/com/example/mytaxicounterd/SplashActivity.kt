package com.example.mytaxicounterd

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs: SharedPreferences = getSharedPreferences("MyTaxiCounterD", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            // Launch OnboardingActivity
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            // Launch MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish() // Close the SplashActivity
    }
}
