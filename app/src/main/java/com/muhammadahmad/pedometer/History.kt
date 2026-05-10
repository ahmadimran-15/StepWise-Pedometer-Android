package com.muhammadahmad.pedometer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class History : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingDialog: AlertDialog
    private val days = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true

        }

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid
            val currentDate = System.currentTimeMillis()

            val dates = mutableListOf<Pair<String, String>>()

            for (i in 1..days)
            {
                dates.add(Pair(getDayName(i), dateToString(currentDate - i * 86400000L)))
            }

            showLoadingDialog() // Show loading dialog while fetching data

            database.child("steps").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val stepItems = mutableListOf<StepItem>()

                        for (dataSnapshot in snapshot.children) {
                            val date =
                                dataSnapshot.child("date").getValue(String::class.java) ?: continue
                            val steps =
                                dataSnapshot.child("steps").getValue(Int::class.java) ?: 0
                            val distance = calculateDistance(steps.toFloat())
                            val calories = calculateCalories(steps.toFloat())

                            stepItems.add(
                                StepItem(
                                    day = getDayName(dates.indexOfFirst { it.second == date } + 1),
                                    steps = steps,
                                    calories = calories,
                                    distance = distance
                                )
                            )
                        }

                        // Display only the last 3 days
                        val lastThreeDays = stepItems.takeLast(days)

                        val adapter = StepItemAdapter(lastThreeDays)
                        recyclerView.adapter = adapter

                        hideLoadingDialog() // Hide the loading dialog when data is fetched
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("History", "Error fetching data: ${error.message}")
                        hideLoadingDialog() // Hide the loading dialog if there's an error
                    }
                })
        }
    }

    private fun dateToString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun getDayName(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
    }

    private fun calculateDistance(steps: Float): Double {
        // Example distance calculation based on steps
        val averageStrideLengthMeters = 0.000762 // Example stride length in meters
        return steps * averageStrideLengthMeters
    }

    private fun calculateCalories(steps: Float): Double {
        // Example calories calculation based on steps
        val caloriesPerStep = 0.04 // Example calories burned per step
        return steps * caloriesPerStep
    }

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        loadingDialog = builder.create()
        loadingDialog.show()
    }

    private fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
}
