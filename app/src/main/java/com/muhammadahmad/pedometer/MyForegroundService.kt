package com.muhammadahmad.pedometer

import AlarmReceiver
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MyForegroundService : Service() {

    private val notificationChannel = "ForegroundChannel"
    private val serviceId = 404



    private lateinit var notificationManager: NotificationManager
    private lateinit var notification: Notification
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var currentDate: String = ""

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createNotification()
        startForeground(serviceId, notification)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        setupAlarm()
    }



    @SuppressLint("NewApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val logRunnable = object : Runnable {
            override fun run() {
                checkForDateChange()
                Log.e("Service", "Service running...")
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(logRunnable, 1000)

        intent?.action?.let {
            if (it == "SEND_STEPS_TO_FIREBASE") {
                sendStepsDataToFirebase()
            }
        }

        /*// Check if notification is initialized before starting foreground
        if (::notification.isInitialized) {
            startForeground(serviceId, notification)
            Log.e("MyForegroundService", "Foreground service started")
        }
*/
        // Check if the reset flag is set in the intent and reset the steps
        /*intent?.getBooleanExtra("RESET_STEPS", false)?.let {
            if (it) {
                // Send steps data to Firebase
                sendStepsDataToFirebase()
                resetSteps()
            }
        }*/

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannel,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, Splash_Screen::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, notificationChannel)
            .setSmallIcon(R.drawable.appicon)
            .setContentTitle("Foreground Service")
            .setContentText("Service is running...")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notification = builder.build()
    }









    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyForegroundService", "Service destroyed")
        // Remove callbacks and handlers
        handler.removeCallbacksAndMessages(null)
        cancelAlarm()
        restartService()
    }

    private fun checkForDateChange() {
        val calendar = Calendar.getInstance()
        val todayDate = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"

        if (todayDate != currentDate) {
            currentDate = todayDate
            // Call your function here when the date changes
            sendStepsDataToFirebase()
        }
    }

    private fun setupAlarm() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 58)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(this, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelAlarm() {
        alarmManager.cancel(pendingIntent)
    }

    private fun sendStepsDataToFirebase() {


        // Retrieve saved steps data from SharedPreferences
        val sharedPreferences = getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
        val savedSteps = sharedPreferences.getFloat("savedSteps", 0f)
        val savedDate = sharedPreferences.getString("savedDate", "") ?: ""

        Log.d("MyForegroundService", "savedSteps: $savedSteps,  savedDate: $savedDate, todayDate: ${getTodayDate()}")



        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance().reference
            val usersRef = database.child("users").child(userId)

            // Retrieve the username from Firebase
            usersRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val loggedinuserName = dataSnapshot.getValue(String::class.java) ?: "Unknown User"

                    // Prepare steps data object
                    val stepsData = HashMap<String, Any>()
                    stepsData["userId"] = userId
                    stepsData["name"] = loggedinuserName
                    stepsData["steps"] = stepscalculated.toInt() // Convert to int if needed
                    stepsData["date"] = getYesterdayDate()

                    // Upload today's steps data to Firebase
                    database.child("steps").child(userId).push().setValue(stepsData)
                        .addOnSuccessListener {
                            Log.d("MyForegroundService", "Steps data uploaded successfully")
                            // Clear shared preferences after successful upload
                            sharedPreferences.edit().remove("savedSteps").apply()
                        }
                        .addOnFailureListener { e ->
                            Log.e("MyForegroundService", "Error uploading steps data", e)
                        }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("MyForegroundService", "Error retrieving username", databaseError.toException())
                }
            })
        } else {
            Log.e("MyForegroundService", "User not authenticated")
        }
    }

    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }




    private fun restartService() {
        val restartServiceIntent = Intent(applicationContext, MyForegroundService::class.java)
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartServiceIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_ONE_SHOT
        )
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
    }

}