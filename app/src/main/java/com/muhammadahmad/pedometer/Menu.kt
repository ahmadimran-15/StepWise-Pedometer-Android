package com.muhammadahmad.pedometer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.slider.Slider

class Menu : AppCompatActivity() {

    private lateinit var etGoal: TextView
    private lateinit var btnSave: Button
    private lateinit var distanceView: TextView
    private lateinit var caloriesView: TextView
    private lateinit var slider: SeekBar
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        slider = findViewById(R.id.slider)
        etGoal = findViewById(R.id.etGoal)
        distanceView = findViewById(R.id.distance)
        caloriesView = findViewById(R.id.calories)
        btnSave = findViewById(R.id.btnSaveGoal)
        sharedPreferences = getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)

        val currentGoal = sharedPreferences.getInt("goal", 10000) // Default goal if not set
        etGoal.text = currentGoal.toString()
        /*slider.progress = currentGoal / 500 //this is for steps 0 to 30000

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val value = progress * 500
                    etGoal.setText(value.toString())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }
        })

         */

        slider.progress = (currentGoal - 1000) / 500  //for steps 1000 to 30000

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val goal = 1000 + progress * 500
                    etGoal.text = goal.toString()
                    updateDistanceAndCalories(goal)
                }


            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }
        })

            btnSave.setOnClickListener {
            val newGoal = etGoal.text.toString().toIntOrNull()
            if (newGoal != null && newGoal > 0) {
                saveGoal(newGoal)
                Toast.makeText(this, "Goal updated successfully", Toast.LENGTH_SHORT).show()
                mainscreen()
            } else {
                Toast.makeText(this, "Invalid goal value", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // Update distance and calories when the activity resumes
        val currentGoal = etGoal.text.toString().toIntOrNull() ?: 10000
        updateDistanceAndCalories(currentGoal)
    }


    private fun updateDistanceAndCalories(goal: Int) {
        val stepLengthInMeters = 0.762 // Assuming each step is 0.8 meters
        val caloriesPerStep = 0.04 // Assuming each step burns 0.04 calories

        val distance = goal * stepLengthInMeters / 1000 // Convert to kilometers
        val calories = goal * caloriesPerStep

        distanceView.text = String.format("Distance: %.2f km", distance)
        caloriesView.text = String.format("Calories: %.2f kcal", calories)
    }


    private fun saveGoal(goal: Int) {
        with(sharedPreferences.edit()) {
            putInt("goal", goal)
            apply()
        }
    }

    fun mainscreen() {

        finish()
    }
}
