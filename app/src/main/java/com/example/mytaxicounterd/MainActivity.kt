package com.example.mytaxicounterd

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private var totalDistance = 0.0
    private var totalTime = 0L
    private var startTimeMillis: Long = 0L

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private lateinit var distanceTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var fareTextView: TextView
    private lateinit var startRideButton: Button
    private lateinit var endRideButton: Button

    private lateinit var googleMap: GoogleMap

    private val baseFare = 2.5
    private val farePerKm = 1.5
    private val farePerMin = 0.5

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ride_summary_channel"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val language = sharedPref.getString("language", "en") // Default to English
        changeLanguage(language ?: "en")

        // Check login state
        val sharedPrefLogin = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isLoggedIn = sharedPrefLogin.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // Navigate to LoginActivity if not logged in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize UI
        distanceTextView = findViewById(R.id.distanceTextView)
        timeTextView = findViewById(R.id.timeTextView)
        fareTextView = findViewById(R.id.fareTextView)
        startRideButton = findViewById(R.id.startRideButton)
        endRideButton = findViewById(R.id.endRideButton)

        // Map setup

        val prefs: SharedPreferences = getSharedPreferences("MyTaxiCounterD", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("isFirstLaunch", true)
        Log.d("MainActivity", "isFirstLaunch: $isFirstLaunch")
        if (isFirstLaunch) {
            // Launch OnboardingActivity
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        } else {
            Log.d("MainActivity", "Onboarding not shown, proceeding to MainActivity")
        }
        Log.d("MainActivity", "isFirstLaunch value: $isFirstLaunch")


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { map -> googleMap = map }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Button Listeners
        startRideButton.setOnClickListener { startRide() }
        endRideButton.setOnClickListener { stopRide() }

        val floatingActionButton: FloatingActionButton = findViewById(R.id.fab_scan)
        floatingActionButton.setOnClickListener{
            startActivity(Intent(this, ScanActivity::class.java))
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView.selectedItemId = R.id.nav_map
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, History::class.java))
                    true
                }
                else -> false
            }
        }

        createNotificationChannel()
        checkNotificationPermission()
    }

    private fun startRide() {
        startTimeMillis = System.currentTimeMillis()
        totalDistance = 0.0
        totalTime = 0L

        startRideButton.isEnabled = false
        endRideButton.isEnabled = true

        // Start location updates
        startLocationUpdates()

        // Start timer
        startTimer()

        Toast.makeText(this, getString(R.string.ride_started), Toast.LENGTH_SHORT).show()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { updateLocation(it) }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun startTimer() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTimeMillis
                totalTime = elapsedTime / 1000 // Time in seconds

                val minutes = totalTime / 60
                val seconds = totalTime % 60
                timeTextView.text = getString(R.string.time_02d_min_02d_sec).format(minutes, seconds)

                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(runnable)
    }

    private fun updateLocation(location: Location) {
        currentLocation?.let { previousLocation ->
            val distance = previousLocation.distanceTo(location) / 1000.0
            totalDistance += distance
        }

        currentLocation = location

        // Update UI
        distanceTextView.text = getString(R.string.distance_2f_km).format(totalDistance)
        val totalFare = calculateFare()
        fareTextView.text = getString(R.string.fare_2f_dh).format(totalFare)

        // Update map
        val latLng = LatLng(location.latitude, location.longitude)
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(latLng).title("Current Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun stopRide() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            handler.removeCallbacks(runnable)
            val totalFare = calculateFare()
            showNotification(totalDistance, totalTime, totalFare)

            startRideButton.isEnabled = true
            endRideButton.isEnabled = false

            Toast.makeText(this, getString(R.string.ride_ended), Toast.LENGTH_SHORT).show()

            // Save traffic data to database
            val dbHelper = UserDatabaseHelper(this)
            val success = dbHelper.saveTraffic(totalDistance, totalTime, totalFare)
            if (success) {
                Toast.makeText(this, getString(R.string.ride_saved_successfully), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.failed_to_save_ride), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateFare(): Double {
        return baseFare + (farePerKm * totalDistance) + (farePerMin * (totalTime / 60.0))
    }

    private fun showNotification(distance: Double, time: Long, fare: Double) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.ride_summary))
            .setContentText(
                getString(R.string.distance_2f_km_time_02d_min_02d_sec_fare_2f_dh).format(
                    distance, time / 60, time % 60, fare
                )
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ requires runtime permission
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request notification permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                    getString(R.string.notification_permission_granted), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                    getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.ride_summary_channel)
            val descriptionText = getString(R.string.notifications_for_ride_summary)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun isOnboardingComplete(): Boolean {
        val sharedPreferences: android.content.SharedPreferences = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        return true
    }

    private fun changeLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun saveLanguagePreference(languageCode: String) {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("language", languageCode)
            apply()
        }
    }
}
