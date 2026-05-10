package com.muhammadahmad.pedometer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muhammadahmad.pedometer.StepItem
import com.muhammadahmad.pedometer.R

class StepItemAdapter(private val stepItems: List<StepItem>) :
    RecyclerView.Adapter<StepItemAdapter.StepItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_step_history, parent, false)
        return StepItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StepItemViewHolder, position: Int) {
        val currentItem = stepItems[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return if (stepItems.isEmpty()) 1 else stepItems.size
    }

    inner class StepItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val tvSteps: TextView = itemView.findViewById(R.id.tvSteps)
        private val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)

        fun bind(stepItem: StepItem) {
            /*if (stepItems.isEmpty()) {
                // Display default values if no data available
                tvDay.text = "No Data"
                tvSteps.text = "0"
                tvCalories.text = "0"
                tvDistance.text = "0.0"
            } else {*/
                // Bind actual data to views
                tvDay.text = stepItem.day
                tvSteps.text = "${stepItem.steps} steps"
                tvCalories.text = String.format("%.2f cal", stepItem.calories)
                tvDistance.text = String.format("%.2f km", stepItem.distance)
            //}
        }
    }
}
