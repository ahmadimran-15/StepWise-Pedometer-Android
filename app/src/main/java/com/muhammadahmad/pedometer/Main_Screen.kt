package com.muhammadahmad.pedometer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.muhammadahmad.pedometer.ShowDialogBox
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mikhaellopez.circularprogressbar.CircularProgressBar



import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.widget.Button
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape


import nl.dionsegijn.konfetti.xml.KonfettiView
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit


/*// Define the goal variable
private val goal: Int = 10000*/
private var steps =0f


var stepscalculated = 0f
class Main_Screen : AppCompatActivity(), SensorEventListener {

    companion object {
        var goal: Int = 10 // Set your default goal here
    }

    private val sendStepsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("Main_Screen", "Broadcast received, sending steps data to Firebase")
            sendStepsDataToFirebase()
        }
    }

    private lateinit var drawerLayout:DrawerLayout
    private lateinit var sensorManager: SensorManager
    private lateinit var tvStepsTaken: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvTotalMax: TextView
    private lateinit var userImg: String
    private lateinit var userName: String
    private lateinit var konfettiView: KonfettiView
    private var confettiShown = false
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var loadingDialog: AlertDialog


    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private lateinit var showDialogBox: ShowDialogBox


    private val handler = Handler(Looper.getMainLooper())
    private val resetHandler = Handler()
    private var resetRunnable: Runnable? = null
    // private lateinit var stepsResetBroadcastReceiver: StepsResetBroadcastReceiver



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        val menu = findViewById<ImageView>(R.id.menu)


        konfettiView = findViewById(R.id.konfettiView)
        circularProgressBar = findViewById(R.id.progress_circular)


        // Initialize Firebase components and SharedPreferences
        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("my_shared_preferences", MODE_PRIVATE)

        // Initialize UI components
        initializeViews()

        /*if (steps >= goal) {
            showConfetti()
        }
*/
        // Register the receiver
        val intentFilter = IntentFilter("com.muhammadahmad.pedometer.SEND_STEPS_TO_FIREBASE")
        registerReceiver(sendStepsReceiver, intentFilter)


        if (intent.hasExtra("NEW_GOAL")) {
            goal = intent.getIntExtra("NEW_GOAL", 10000)
            // Update UI elements with the new goal
            tvTotalMax.text = "/$goal"
            // Save the updated goal in SharedPreferences
            saveGoal(goal)
        }

        // Set the progress bar max with the updated goal
        circularProgressBar.progressMax = goal.toFloat()


        // Initialize ShowDialogBox instance
        showDialogBox = ShowDialogBox(this)

        // Load user data from Firebase with a timeout mechanism
        loadUserDataFromFirebase()

        // Set click listener for logout button

        // Register the receiver
        val filter = IntentFilter("com.muhammadahmad.pedometer.ACTION_DATE_CHANGED")
        registerReceiver(dateChangeReceiver, filter)

        // Initialize SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Load previous step count
        loadPreviousSteps()

        // Start checking for midnight reset using handler
        startMidnightResetHandler()

        // Register BroadcastReceiver to handle steps reset
        /*   stepsResetBroadcastReceiver = StepsResetBroadcastReceiver()
           LocalBroadcastManager.getInstance(this)
               .registerReceiver(stepsResetBroadcastReceiver, IntentFilter("RESET_STEPS_ACTION"))
   */
        // Update the goal TextView with the goal value
        tvTotalMax.text = "/$goal"

        menu.setOnClickListener {
            val intent = Intent(this, Menu::class.java)
            startActivity(intent)

        }

        drawerLayout = findViewById(R.id.drawer_layout)
        findViewById<ImageView>(R.id.menu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Setup navigation item click listener
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_menu_item_menu -> {
                    startActivity(Intent(this, Menu::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_menu_item_history -> {
                    startActivity(Intent(this, History::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_menu_item_logout -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }




    }





    // Show logout confirmation dialog
    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_box, null)
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<TextView>(R.id.btn_yes).setOnClickListener {
            logoutUser()
            alertDialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_no).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    // Logout user and navigate to login screen
    private fun logoutUser() {
        val editor = sharedPreferences.edit()
        editor.putFloat("savedSteps", totalSteps)
        editor.putString("savedDate", getTodayDate())
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        firebaseAuth.signOut()
        startActivity(Intent(this@Main_Screen, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private val dateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == "com.muhammadahmad.pedometer.ACTION_DATE_CHANGED" && it.getStringExtra("ACTION") == "DATE_CHANGED") {
                    // Call sendStepsDataToFirebase or any other function you need
                    sendStepsDataToFirebase()
                }
            }
        }
    }




    override fun onBackPressed() {
        // Call super implementation
        showDialogBox.showExitConfirmationDialog()
    }


    // Function to save the goal in SharedPreferences
    private fun saveGoal(goal: Int) {
        val sharedPreferences = getSharedPreferences("my_shared_preferences", MODE_PRIVATE)
        sharedPreferences.edit().putInt("GOAL", goal).apply()

    }




    override fun onResume() {
        super.onResume()
        running = true

        // Check if step counter sensor is available
        val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(
                this,
                "No step counter sensor detected on this device",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Register sensor listener
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

        updateGoal()
    }

    private fun updateGoal() {
        // Get the updated goal from SharedPreferences
        goal = sharedPreferences.getInt("goal", 10000) // Default goal if not set
        tvTotalMax.text = "/$goal"
    }

    override fun onPause() {
        super.onPause()
        running = false

        // Unregister sensor listener to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver when activity is destroyed
        //   LocalBroadcastManager.getInstance(this).unregisterReceiver(stepsResetBroadcastReceiver)
        // Remove callbacks when the activity is destroyed
        handler.removeCallbacksAndMessages(null)
        resetHandler.removeCallbacksAndMessages(null)
        unregisterReceiver(sendStepsReceiver)
        unregisterReceiver(dateChangeReceiver)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter sensor
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            event?.let {
                totalSteps = event.values[0]


                // Check if today's date is different from stored date
                if (sharedPreferences.getString("TodayDate", "") != getTodayDate()) {

                    // loadUserDataFromFirebase()
                    // sendStepsDataToFirebase()
                    sharedPreferences.edit().putFloat("TotalSteps", totalSteps).apply()
                    sharedPreferences.edit().putString("TodayDate", getTodayDate()).apply()
                    Log.e("senssorChanged", "if mein giya hai")


                } else {
                    val storeSteps = sharedPreferences.getFloat("TotalSteps", 0f)
                    val currentSteps = totalSteps - storeSteps
                    stepscalculated = currentSteps
                    Log.e("senssorChanged", "else mein giya hai")

                    /* steps  = currentSteps*/

                    // Update UI with current steps
                    updateStepsDisplay(currentSteps)
                    // Set the progress bar max with the updated goal
                    circularProgressBar.progressMax = goal.toFloat()

                    // Calculate and update distance in meters
                    val distanceInMeters = calculateDistance(currentSteps)
                    tvDistance.text = DecimalFormat("#0.00").format(distanceInMeters) + " km"

                    // Calculate and update calories burned
                    val caloriesBurned = calculateCalories(currentSteps)
                    tvCalories.text = DecimalFormat("#0.00").format(caloriesBurned)

                    Log.d("MyForegroundService", "totalsteps: $totalSteps, storeSteps: $storeSteps, stepscalculated: $currentSteps todayDate: ${getTodayDate()}")


                    if (currentSteps >= goal && !confettiShown) {
                        showConfetti()
                        confettiShown = true // Set flag to true to prevent repeated confetti
                    } else {

                    }
                }
            }
        }
    }


    fun sendStepsDataToFirebase() {


        // Retrieve saved steps data from SharedPreferences
        val sharedPreferences = getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
        val savedSteps = sharedPreferences.getFloat("savedSteps", 0f)
        val savedDate = sharedPreferences.getString("savedDate", "") ?: ""

        Log.d("MyForegroundService", "savedSteps: $savedSteps, totalSteps: $totalSteps,  savedDate: $savedDate, todayDate: ${getTodayDate()}")



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



    private fun initializeViews() {
        // Initialize UI components
        tvStepsTaken = findViewById(R.id.tv_stepsTaken)
        tvDistance = findViewById(R.id.tv_distance)
        tvCalories = findViewById(R.id.calories)
        tvTotalMax = findViewById(R.id.tv_totalMax)
        circularProgressBar = findViewById(R.id.progress_circular)
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

    private fun loadPreviousSteps() {
        // Load previous total steps from SharedPreferences
        previousTotalSteps = sharedPreferences.getFloat("key1", 0f)
        Log.d("Load_steps", "Loaded previous steps: $previousTotalSteps")
        updateStepsDisplay(previousTotalSteps)
    }

    private fun updateStepsDisplay(steps: Float) {
        // Update UI with current steps
        tvStepsTaken.text = steps.toInt().toString()
        // Update circular progress bar
        circularProgressBar.setProgressWithAnimation(steps)
    }

    private fun startMidnightResetHandler() {
        // Check for midnight reset every minute
        resetRunnable = object : Runnable {
            override fun run() {
                // checkForMidnightReset()
                resetHandler.postDelayed(this, 60000) // Check every minute
            }
        }
        resetHandler.post(resetRunnable!!)
    }



    private val LOADING_TIMEOUT_MS = 9000L // 5 seconds timeout
    private val NETWORK_CHECK_DELAY_MS = 2500L // 3 seconds delay for network check

    private fun loadUserDataFromFirebase() {
        val userId = firebaseAuth.currentUser?.uid


        if (userId != null && !isDestroyed) {
            // Check for internet connectivity
            if (isNetworkConnected()) {
                // Show loading dialog
                showLoadingDialog()

                // Show toast message after 3 seconds for network check
                handler.postDelayed({
                    if (loadingDialog.isShowing) {
                        Toast.makeText(this@Main_Screen, "Network connection is slow. Checking connection.... Please dont exit the application", Toast.LENGTH_SHORT).show()
                    }
                }, NETWORK_CHECK_DELAY_MS)

                // Get reference to user's data in Firebase Realtime Database
                val userRef = FirebaseDatabase.getInstance().getReference("users/$userId")

                // Set a timer to check for slow internet and load cached data if necessary
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        // Check if data is still loading
                        if (loadingDialog.isShowing) {
                            // Hide loading dialog
                            runOnUiThread {
                                hideLoadingDialog()
                                loadCachedUserData()
                            }
                            timer.cancel() // Cancel timer
                        }
                    }
                }, LOADING_TIMEOUT_MS)

                // Read user's data
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!isDestroyed) {
                            // Get user's profile data
                            val imageUrl = dataSnapshot.child("profileImg").getValue(String::class.java)
                            userImg = imageUrl.toString()
                            val profileImg = findViewById<ImageView>(R.id.profileImg4)

                            // Load profile image using Glide
                            Glide.with(this@Main_Screen)
                                .load(imageUrl)
                                .error(R.drawable.ic_launcher_foreground) // Default image in case of error
                                .into(profileImg)

                            val username = dataSnapshot.child("name").getValue(String::class.java)

                            userName = username.toString()

                            val usernameTextView = findViewById<TextView>(R.id.username)
                            usernameTextView.text = username

                            // Save user data in SharedPreferences
                            saveUserData(imageUrl, username)

                            // Hide loading dialog after data is loaded
                            hideLoadingDialog()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors and hide loading dialog
                        hideLoadingDialog()
                        Toast.makeText(this@Main_Screen, "Failed to load data", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                // No internet connection detected, load cached data immediately
                loadCachedUserData()
            }
        } else {
            // Load cached data from SharedPreferences
            loadCachedUserData()
            hideLoadingDialog()
        }
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    /* fun resetSteps() {


         // Save the current total steps to SharedPreferences as the new previous total steps
         previousTotalSteps = totalSteps
         sharedPreferences.edit().putFloat("key1", previousTotalSteps).apply()

         // Reset other variables or UI elements as needed
         tvStepsTaken.text = "0"
         circularProgressBar.setProgressWithAnimation(0f)

         Log.d("MainScreen", "Steps reset to zero at midnight")
     }*/

    private fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
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
        if (::loadingDialog.isInitialized && loadingDialog.isShowing)
        {
            loadingDialog.dismiss()
        }
    }

    private fun showConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(Color.YELLOW, Color.GREEN, Color.MAGENTA),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )

        konfettiView.post {
            konfettiView.start(party)
        }

        // Delay showing the congratulatory dialog after the animation
        Handler(Looper.getMainLooper()).postDelayed({
            showCongratulatoryDialog()
        }, 0L) // Adjust this delay as needed (5000L = 5 seconds)
    }


    private fun showCongratulatoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.steps_completed_dialog, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)



        dialogTitle.text = "Congratulations!"
        dialogMessage.text = "You have completed your daily goal of $goal steps!"

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }


    private fun saveUserData(imageUrl: String?, username: String?) {
        with(sharedPreferences.edit()) {
            putString("CachedImageUrl", imageUrl)
            putString("CachedUsername", username)
            apply()
        }
    }

    private fun loadCachedUserData() {
        val cachedImageUrl = sharedPreferences.getString("CachedImageUrl", null)
        val cachedUsername = sharedPreferences.getString("CachedUsername", null)

        cachedImageUrl?.let {
            val profileImg = findViewById<ImageView>(R.id.profileImg4)
            Glide.with(this)
                .load(it)
                .error(R.drawable.ic_launcher_foreground) // Load a default image in case of error
                .into(profileImg)
        }

        cachedUsername?.let {
            val usernameTextView = findViewById<TextView>(R.id.username)
            usernameTextView.text = it
        }
    }


    // Inner BroadcastReceiver class to handle steps reset broadcast
    /*private class StepsResetBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "RESET_STEPS_ACTION") {
                val mainScreen = context as Main_Screen
                mainScreen.totalSteps = 0f
                mainScreen.previousTotalSteps = 0f
                mainScreen.sharedPreferences.edit().putFloat("key1", 0f).apply()
                Log.d("MainScreen_broadcast", "Steps reset to zero at midnight in mainscreen broadacast wala function")
                mainScreen.updateStepsDisplay(0f)
                mainScreen.circularProgressBar.progressMax = goal.toFloat()
            }
        }
    }*/




}