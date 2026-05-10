import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.muhammadahmad.pedometer.MyForegroundService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "Alarm received, triggering function...")
        // Replace with the function you want to call when the alarm triggers
        context?.let {
            val serviceIntent = Intent(it, MyForegroundService::class.java)
            serviceIntent.action = "SEND_STEPS_TO_FIREBASE"
            it.startService(serviceIntent)
        }
    }
}
