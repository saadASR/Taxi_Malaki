package com.example.mytaxicounterd

// Fare Calculator remains the same
class FareCalculator(
    private val baseFare: Double,
    private val ratePerKm: Double,
    private val ratePerMinute: Double
) {
    fun calculate(distanceKm: Double, timeMinutes: Double): Double {
        return baseFare +
                (ratePerKm * distanceKm) +
                (ratePerMinute * timeMinutes)
    }
}