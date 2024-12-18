package com.example.mytaxicounterd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    private val layouts = intArrayOf(
        R.layout.onboarding_page_one
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layouts[viewType], parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        // You can set any data here if needed
    }

    override fun getItemCount(): Int = layouts.size

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
