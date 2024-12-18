package com.example.mytaxicounterd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrafficAdapter(private val trafficList: List<Traffic>) : RecyclerView.Adapter<TrafficAdapter.TrafficViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrafficViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_traffic, parent, false)
        return TrafficViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TrafficViewHolder, position: Int) {
        val traffic = trafficList[position]

        // Format distance: Display 0.xx for less than 1 km
        val formattedDistance = if (traffic.distance < 1) {
            String.format("Distance: %.2f km", traffic.distance)
        } else {
            String.format("Distance: %.2f km", traffic.distance)
        }

        // Format time: Convert total seconds to minutes
        val minutes = traffic.time / 60
        val seconds = traffic.time % 60
        val formattedTime = "Time: $minutes min $seconds sec"

        // Format fare: Display two decimals
        val formattedFare = String.format("Fare: %.2f DH", traffic.fare)

        // Format date: Convert timestamp to human-readable format
        val formattedDate = "Date: ${formatTimestamp(traffic.timestamp)}"

        // Set values to views
        holder.distanceTextView.text = formattedDistance
        holder.timeTextView.text = formattedTime
        holder.fareTextView.text = formattedFare
        holder.timestampTextView.text = formattedDate
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val date = Date(timestamp.toLong())
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            sdf.format(date)
        } catch (e: Exception) {
            "Invalid date"
        }
    }

    override fun getItemCount() = trafficList.size

    class TrafficViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val distanceTextView: TextView = itemView.findViewById(R.id.textViewDistance)
        val timeTextView: TextView = itemView.findViewById(R.id.textViewTime)
        val fareTextView: TextView = itemView.findViewById(R.id.textViewFare)
        val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestamp)
    }
}