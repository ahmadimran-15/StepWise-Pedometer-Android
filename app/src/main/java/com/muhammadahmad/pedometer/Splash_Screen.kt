package com.muhammadahmad.pedometer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.provider.Settings

class Splash_Screen : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: AlertDialog
    private var appLogicStarted = false // Flag to ensure startAppLogic is called only once

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Log.d("MainActivity", "onCreate")

        sharedPreferences = getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
        loadingDialog = createLoadingDialog()

        // Check and request permissions
        if (allPermissionsGranted()) {
            // If permissions are already granted, proceed with your logic
            Log.d("MainActivity", "Permissions already granted")
            startAppLogic()
        } else {
            // Request permissions
            Log.d("MainActivity", "Requesting permissions")
            showLoadingDialog()
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        showLoadingDialog()
        // Check permissions again when returning from settings
        if (allPermissionsGranted() && !appLogicStarted) {
            // If permissions are now granted, proceed with app logic
            startAppLogic()
        } else {
            hideLoadingDialog()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  //send to mashood and umer bhai and run successful on both
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.CAMERA,
                Manifest.permission.ACTIVITY_RECOGNITION
                // Add other permissions as needed
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACTIVITY_RECOGNITION
                // Add other permissions for older Android versions
            )
        }

        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showLoadingDialog() {
        loadingDialog.show()
    }

    private fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    private fun createLoadingDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        return builder.create()
    }

    private fun requestPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.CAMERA,
                Manifest.permission.ACTIVITY_RECOGNITION
            )

        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        }

        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions are granted
                Log.d("MainActivity", "All requested permissions granted")
                startAppLogic()
            } else {
                // If any permission is denied, show permission denied dialog
                if (permissions.any { permission ->
                        ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                    }) {
                    // Permissions were denied, but some can still be requested
                    Log.d("MainActivity", "Some permissions denied, requesting again")
                    showPermissionDeniedDialog()
                } else {
                    // All permissions denied, show permission denied dialog
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.go_to_settings_dialog_box, null)

        builder.setView(dialogView)
        val alertDialog = builder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<TextView>(R.id.btn_yes).setOnClickListener {
            alertDialog.dismiss()
            showLoadingDialog() // Show loading dialog before navigating to settings
            navigateToAppSettings()
        }

        dialogView.findViewById<TextView>(R.id.btn_no).setOnClickListener {
            alertDialog.dismiss()
            finishAffinity() // Finish all activities and exit the application
        }

        alertDialog.show()
    }

    private fun navigateToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun startAppLogic() {
        if (appLogicStarted) return // Prevent multiple calls
        appLogicStarted = true // Set flag to true

        try {
            hideLoadingDialog() // Hide loading dialog when starting app logic
            val serviceIntent = Intent(this, MyForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            // Logging to check if the service is running
            Log.d("MainActivity", "Service started")

            Handler().postDelayed({
                val storedUserID = sharedPreferences.getString("Login", null)
                val isLogin = sharedPreferences.getBoolean("isLoggedIn", false)
                Log.d("logvalue", "log value: $isLogin")
                val hasInternet = isInternetAvailable()

                Log.d("MainActivity", "Stored UserID: $storedUserID, Has Internet: $hasInternet")

                val intent = if (storedUserID.isNullOrEmpty()) {
                    Intent(this, Login::class.java)
                } else if (isLogin && storedUserID != null) {
                    Intent(this, Main_Screen::class.java).apply {
                        putExtra("USER_ID", storedUserID) // Pass the userID to Main_Screen
                        putExtra("HAS_INTERNET", hasInternet)
                    }
                } else {
                    Intent(this, Login::class.java)
                }

                startActivity(intent)
                finishAffinity() // Finish the MainActivity
            }, 2000) // Reduced to 2 seconds for a quicker splash screen
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in startAppLogic: ${e.message}")
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}
