package com.muhammadahmad.pedometer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import com.muhammadahmad.pedometer.ShowDialogBox
import com.bumptech.glide.Glide
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    private lateinit var fireAuth: FirebaseAuth
    private lateinit var email: EditText

    private lateinit var showDialogBox: ShowDialogBox
    private lateinit var pass: EditText
    private lateinit var dbRef: DatabaseReference
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: AlertDialog
    private val LOADING_TIMEOUT_MS = 12000L // 10 seconds timeout for loading dialog




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val login: Button = findViewById(R.id.LoginButton)
        pass = findViewById(R.id.Password2)
        email = findViewById(R.id.Email)
        fireAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("user")
        // Initialize ShowDialogBox instance
        showDialogBox = ShowDialogBox(this)

        val signUp = findViewById<TextView>(R.id.createAcc)
        signUp.paintFlags = Paint.UNDERLINE_TEXT_FLAG or signUp.paintFlags

        val forgotpassword = findViewById<TextView>(R.id.ForgotPassword)

        login.setOnClickListener {
            var check = true
            val password = pass.text.toString()
            val mail = email.text.toString()
            if (mail.isEmpty()) {
                email.error = "Please enter email"
                check = false
            }
            if (password.isEmpty()) {
                pass.error = "Please enter password"
                check = false
            }

            if (check) {
                login.isEnabled = false // Disable login button
                showLoadingDialog()

                // Handler to manage loading timeout
                val timeoutHandler = Handler(Looper.getMainLooper())
                timeoutHandler.postDelayed({
                    if (loadingDialog.isShowing) {
                        hideLoadingDialog()
                        Toast.makeText(this, "Check network connection and try again.", Toast.LENGTH_SHORT).show()
                        login.isEnabled = true // Re-enable login button
                    }
                }, LOADING_TIMEOUT_MS)

                fireAuth.signInWithEmailAndPassword(mail, password)
                    .addOnCompleteListener { task ->
                        hideLoadingDialog() // Hide loading dialog on Firebase task complete
                        timeoutHandler.removeCallbacksAndMessages(null) // Cancel timeout handler

                        if (task.isSuccessful) {
                            val userID = mail.substringBefore('@').replace('.', ' ').replace('_', '2')
                            database = FirebaseDatabase.getInstance().getReference("user")
                            database.child(userID).get().addOnSuccessListener { snapshot ->
                                val nam = snapshot.child("count").value.toString()
                                sharedPreferences = getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putString("Login", userID).putBoolean("isLoggedIn", true).apply()

                                val intent = if (nam == "1") {
                                    database.child(userID).child("count").setValue("2")
                                    Intent(this, Main_Screen::class.java)
                                } else {
                                    Intent(this, Main_Screen::class.java)
                                }
                                intent.putExtra("USER_ID", userID)
                                startActivity(intent)
                                finishAffinity()
                                // Finish the login activity
                            }
                        } else {
                            login.isEnabled = true // Re-enable the button on failure
                            Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            Log.d("LoginActivity", "signInWithEmail:failure", task.exception)
                        }
                    }
            }
        }

        signUp.setOnClickListener {
            val intent = Intent(this, SignUP::class.java)
            startActivity(intent)

        }

        forgotpassword.setOnClickListener {
            val intent = Intent(this, VerifyCode::class.java)
            startActivity(intent)

        }
    }

    /*private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog_gif, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        loadingDialog = builder.create()
        loadingDialog.show()

        // Load the GIF
        val loadingGif: ImageView = dialogView.findViewById(R.id.loading_gif)
        Glide.with(this).asGif().load(R.drawable.loading).into(loadingGif)
    }

    private fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }*/

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        loadingDialog = builder.create()
        loadingDialog.show()
    }
    override fun onBackPressed() {
        // Call super implementation
        showDialogBox.showExitConfirmationDialog()
    }

    private fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
}