package com.example.mytaxicounterd

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: UserDatabaseHelper
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure language is applied before UI is rendered
        val app = applicationContext as MyApplication
        app.updateLayoutDirectionForActivity(this)

        setContentView(R.layout.activity_login)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = resources.getColor(R.color.white, theme)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.gold, theme)
        }


        dbHelper = UserDatabaseHelper(this)
        sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        btnLogin.setOnClickListener {
            Log.d("LoginActivity", "Login button clicked")
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            Log.d("LoginActivity", "Email: $email, Password: $password")

            if (email.isEmpty() || password.isEmpty()) {
                Log.d("LoginActivity", "Email or password is empty")
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("LoginActivity", "Checking user credentials")
                val userExists = dbHelper.checkUser(email, password)
                if (userExists) {
                    Log.d("LoginActivity", "User exists, navigating to MainActivity")
                    val userEmail = dbHelper.getUserEmail(email)
                    with(sharedPref.edit()) {
                        putBoolean("isLoggedIn", true)
                        putString("userEmail", userEmail) // Store the actual email
                        apply()
                    }
                    Log.d("LoginActivity", "Starting MainActivity")
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    Log.d("LoginActivity", "MainActivity started, finishing LoginActivity")
                    finish()
                } else {
                    Log.d("LoginActivity", "Invalid email or password")
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
