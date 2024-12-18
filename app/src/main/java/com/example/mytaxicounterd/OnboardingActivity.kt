package com.example.mytaxicounterd

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.viewpager2.widget.ViewPager2
import com.example.mytaxicounterd.OnboardingAdapter

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = OnboardingAdapter()

        val buttonGetStarted: Button = findViewById(R.id.button_get_started)
        buttonGetStarted.setOnClickListener {
            // Mark onboarding as completed
            val prefs: SharedPreferences = getSharedPreferences("MyTaxiCounterD", MODE_PRIVATE)
            prefs.edit().putBoolean("isFirstLaunch", false).apply()
            // Start MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
