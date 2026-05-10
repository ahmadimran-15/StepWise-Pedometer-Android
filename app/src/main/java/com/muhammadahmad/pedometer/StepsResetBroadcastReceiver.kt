// StepsResetBroadcastReceiver.kt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.muhammadahmad.pedometer.Main_Screen

class StepsResetBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "RESET_STEPS_ACTION") {
            val totalSteps = intent.getFloatExtra("totalSteps", 0f)
            Log.d("StepsResetBroadcast", "Received broadcast to reset steps: $totalSteps")

            // Reset the steps in MainActivity or wherever the steps are managed
            if (context is Main_Screen) {
               // context.resetSteps()
            }
        }
    }
}
