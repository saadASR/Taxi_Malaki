package com.example.mytaxicounterd

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Apply language settings globally when the app starts
        applyLanguageAndLayoutDirection()
    }

    // This function will apply the language and layout direction based on saved preferences
    fun applyLanguageAndLayoutDirection() {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPref.getString("language", "en") ?: "en"

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        // Set layout direction based on language (RTL for Arabic, LTR for others)
        if (languageCode == "ar") {
            config.setLayoutDirection(Locale("ar"))
        } else {
            config.setLayoutDirection(Locale("en"))
        }

        // Update configuration based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            resources.updateConfiguration(config, resources.displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

    // Function to save language preference
    fun saveLanguagePreference(languageCode: String) {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("language", languageCode)
            apply()
        }

        // Reapply the language and layout direction after saving preference
        applyLanguageAndLayoutDirection()
    }

    // This function ensures that the app layout is updated globally (optional use)
    fun updateLayoutDirectionForActivity(activity: AppCompatActivity) {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPref.getString("language", "en") ?: "en"

        // Set layout direction for RTL languages (e.g., Arabic) and LTR for others
        activity.window.decorView.layoutDirection =
            if (languageCode == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
    }
}
