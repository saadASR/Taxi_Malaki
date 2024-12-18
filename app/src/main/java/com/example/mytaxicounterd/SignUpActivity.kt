package com.example.mytaxicounterd

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var dbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        dbHelper = UserDatabaseHelper(this)

        // Initialize login button after setting the content view
        val logintr = findViewById<Button>(R.id.btnLogin1)

        logintr.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

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
                    // Save user information in SharedPreferences
                    val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("userName", username)
                        putString("userEmail", email) // Store the actual email
                        putString("userPhone", phone) // Store the phone number
                        apply()
                    }
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close the sign-up activity
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
