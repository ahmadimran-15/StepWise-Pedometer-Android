package com.muhammadahmad.pedometer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class CustomGifProgressBar : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_DATE_CHANGED || intent?.action == Intent.ACTION_TIME_CHANGED || intent?.action == "com.muhammadahmad.pedometer.RESET_STEPS") {
            // Reset steps in SharedPreferences
            val sharedPreferences: SharedPreferences = context!!.getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
            sharedPreferences.edit().putFloat("key1", 0f).apply()

            // Optionally, you can show a notification or log a message
        }
    }
}
