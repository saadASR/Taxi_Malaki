package com.example.mytaxicounterd

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {

    private lateinit var dbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize the CheckBox and set styled text
        val checkBox = findViewById<CheckBox>(R.id.checkbox)
        val fullText = "By signing up, you agree to the Terms of service and Privacy policy."
        val spannableString = SpannableString(fullText)
        val termsStart = fullText.indexOf("Terms of service")
        val termsEnd = termsStart + "Terms of service".length
        val privacyStart = fullText.indexOf("Privacy policy")
        val privacyEnd = privacyStart + "Privacy policy".length
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#EDAE10")),
            termsStart, termsEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#EDAE10")),
            privacyStart, privacyEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        checkBox.text = spannableString

        // Initialize database helper
        dbHelper = UserDatabaseHelper(this)

        // Navigate to LoginActivity
        val loginButton = findViewById<Button>(R.id.btnLogin1)
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = resources.getColor(R.color.white, theme)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.gold, theme)
        }
        // Initialize input fields and button
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        // Handle sign-up logic
        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                val success = dbHelper.addUser(username, password, email)
                if (success) {
                    val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("userName", username)
                        putString("userEmail", email)
                        putString("userPhone", phone)
                        apply()
                    }
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
