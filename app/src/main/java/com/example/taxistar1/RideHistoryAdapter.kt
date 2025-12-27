package com.example.taxistar1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class RideHistoryAdapter(
    private var rides: List<RideData>
) : RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder>() {

    inner class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvFare: TextView = itemView.findViewById(R.id.tvFare)
        val tvCountry: TextView = itemView.findViewById(R.id.tvCountry)
        val tvRateType: TextView = itemView.findViewById(R.id.tvRateType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ride_history, parent, false)
        return RideViewHolder(view)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride = rides[position]

        holder.tvDate.text = ride.getFormattedDate()
        holder.tvDay.text = ride.getFormattedDay()
        holder.tvTime.text = ride.getFormattedTime()
        holder.tvDistance.text = String.format(Locale.US, "%.2f km", ride.distanceKm)
        holder.tvDuration.text = ride.getDuration()
        holder.tvFare.text = String.format(Locale.US, "%.2f %s", ride.fare, ride.currency)
        holder.tvCountry.text = ride.country
        holder.tvRateType.text = if (ride.isDay) "Day Rate" else "Night Rate"
    }

    override fun getItemCount(): Int = rides.size

    fun updateRides(newRides: List<RideData>) {
        rides = newRides
        notifyDataSetChanged()
    }
}