package com.example.mytaxicounterd

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var languageSpinner: Spinner
    private var currentLanguage: String = "en" // Default language
    private var isLanguageChangeInProgress = false // Flag to prevent triggering spinner event during recreation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        languageSpinner = findViewById(R.id.languageSpinner)

        // Set up a simple ArrayAdapter for the spinner
        val simpleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("English", "Arabic", "French"))
        simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = simpleAdapter

        // Set the spinner to the saved language without triggering changes
        val savedLanguage = getSavedLanguage()
        if (currentLanguage != savedLanguage) {
            currentLanguage = savedLanguage
            languageSpinner.setSelection(getLanguageSelectionIndex(currentLanguage))
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = resources.getColor(R.color.white, theme)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.gold, theme)
        }

        languageSpinner.isEnabled = true // Ensure the spinner is enabled

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Prevent triggering during language change
                if (isLanguageChangeInProgress) {
                    return
                }

                val selectedLanguage = when (position) {
                    0 -> "en" // English
                    1 -> "ar" // Arabic
                    2 -> "fr" // French
                    else -> "en"
                }

                // Only change language if it's different from the current language
                if (selectedLanguage != currentLanguage) {
                    changeLanguage(selectedLanguage)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Retrieve user data from SharedPreferences
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userName = sharedPref.getString("userName", "Guest")
        val userEmail = sharedPref.getString("userEmail", "")
        val userPhone = sharedPref.getString("userPhone", "")

        // Display user information
        val userInfoTextView: TextView = findViewById(R.id.userInfo)
        userInfoTextView.text = Html.fromHtml(buildString {
            append(getString(R.string.b_name_b_br, userName ?: getString(R.string.unknown)))
            append(getString(R.string.b_email_b_br, userEmail ?: getString(R.string.not_provided)))
            append(getString(R.string.b_phone_b, userPhone ?: getString(R.string.not_provided)))
        }, Html.FROM_HTML_MODE_COMPACT)

        // Generate and display QR code
        generateQRCode(userName, userEmail, userPhone)

        // Logout button click listener
        val logoutButton: Button = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Clear user session data
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("isLoggedIn", false)
                apply()
            }

            // Navigate back to LoginActivity with flags to clear the back stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish() // Close ProfileActivity
        }

        // Bottom Navigation setup
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView.selectedItemId = R.id.nav_profile
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, History::class.java))
                    true
                }R.id.nav_scan -> {
                startActivity(Intent(this, ScanActivity::class.java))
                true
            }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun getLanguageSelectionIndex(languageCode: String): Int {
        return when (languageCode) {
            "en" -> 0
            "ar" -> 1
            "fr" -> 2
            else -> 0
        }
    }

    private fun getSavedLanguage(): String {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPref.getString("language", "en") ?: "en"
    }

    private fun generateQRCode(name: String?, email: String?, phone: String?) {
        val qrContent = getString(
            R.string.name_email_phone,
            name ?: getString(R.string.unknown),
            email ?: getString(R.string.not_provided),
            phone ?: getString(R.string.not_provided)
        )
        val writer = QRCodeWriter()
        try {
            val bitMatrix: BitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200)
            val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            for (x in 0 until 200) {
                for (y in 0 until 200) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun changeLanguage(languageCode: String) {
        Log.d("ProfileActivity", "Changing language to: $languageCode")

        // Set the flag to true before changing the language to prevent spinner event from firing
        isLanguageChangeInProgress = true

        // Set the locale and update resources
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Save the language preference
        saveLanguagePreference(languageCode)
        currentLanguage = languageCode // Update the current language

        // Recreate the activity after the language change to apply the update
        Handler(Looper.getMainLooper()).postDelayed({
            isLanguageChangeInProgress = false // Reset the flag
            recreate() // Recreate the activity to apply new locale
        }, 200)
    }

    private fun saveLanguagePreference(languageCode: String) {
        Log.d("ProfileActivity", "Saving language preference: $languageCode")
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("language", languageCode)
            apply()
        }
    }
}
