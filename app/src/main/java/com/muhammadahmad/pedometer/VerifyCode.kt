package com.muhammadahmad.pedometer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class VerifyCode : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var sendButton: TextView
    private lateinit var loadingDialog: AlertDialog
    private lateinit var showDialogBox:ShowDialogBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)
        showDialogBox=ShowDialogBox(this)

        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        sendButton = findViewById(R.id.ForgotPassword)

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                sendButton.isEnabled = false
                showLoadingDialog()
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                hideLoadingDialog()
                if (task.isSuccessful) {
                    sendButton.isEnabled = true
                    emailEditText.setText("") // Clear email field
                    Toast.makeText(this, "Reset link sent to your email. Check Your Inbox", Toast.LENGTH_SHORT).show()
                    loginscreen()
                } else {
                    sendButton.isEnabled = true
                    Toast.makeText(this, "Unable to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
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

    override fun onBackPressed() {
        // Call super implementation
        showDialogBox.showExitConfirmationDialog()
    }
    fun loginscreen() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    fun login(view: View) {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    fun loginback(view: View) {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}
