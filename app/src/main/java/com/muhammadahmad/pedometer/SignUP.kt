package com.muhammadahmad.pedometer

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.util.*

class SignUP : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()
    private lateinit var showDialogBox: ShowDialogBox
    private lateinit var loadingDialog: AlertDialog
    private lateinit var signupButton: Button
    private lateinit var profileImg: ImageView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val logintext = findViewById<TextView>(R.id.login)
        showDialogBox = ShowDialogBox(this)

        logintext.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        profileImg = findViewById(R.id.editProfileImgBtn)
        profileImg.setOnClickListener {
            launchGallery()
        }

        emailEditText = findViewById(R.id.Email)
        passwordEditText = findViewById(R.id.Password)
        nameEditText = findViewById(R.id.Name)
        phoneEditText = findViewById(R.id.Phone)
        signupButton = findViewById(R.id.SignupButton)

            signupButton.setOnClickListener {
                var check = true
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val name = nameEditText.text.toString()
                val phone = phoneEditText.text.toString()

                if (email.isEmpty()) {
                    emailEditText.error = "Please enter email"
                    check = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.error = "Please enter a valid email"
                    check = false
                }
                else if (email[0].isUpperCase()) {
                    emailEditText.error = "First letter can't be capital"
                    check = false
                }

                if (password.isEmpty()) {
                    passwordEditText.error = "Please enter password"
                    check = false
                }

                if (name.isEmpty()) {
                    nameEditText.error = "Please enter name"
                    check = false
                }

                if (phone.isEmpty()) {
                    phoneEditText.error = "Please enter phone number"
                    check = false
                } else if (phone.length != 11) {
                    phoneEditText.error = "Phone number must be 11 digits"
                    check = false
                }
                else if (!phone.all { it.isDigit() }) {
                    phoneEditText.error = "Phone number must contain only digits"
                    check = false
                }

                if (filePath == null) {
                    Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
                    check = false
                }

            if (check) {
                signupButton.isEnabled = false // Disable signup button
                showLoadingDialog()

                // Create user with email and password
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            uploadImage { uri ->
                                hideLoadingDialog()
                                // User creation success
                                val user = firebaseAuth.currentUser
                                val userId = user?.uid

                                // Store user data in Firebase Realtime Database
                                val usersRef = firebaseDatabase.getReference("users")
                                val userData = hashMapOf(
                                    "name" to name,
                                    "password" to password,
                                    "Phone" to phone,
                                    "profileImg" to uri.toString()
                                )
                                userId?.let {
                                    usersRef.child(it).setValue(userData)
                                        .addOnSuccessListener {
                                            // Data written successfully, navigate to login page
                                            Toast.makeText(this@SignUP, "Your Account has been created Successfully", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, Login::class.java)
                                            startActivity(intent)
                                            finish() // Finish this activity to prevent user from coming back with back button
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Failed to write data to database: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        } else {
                            hideLoadingDialog()
                            signupButton.isEnabled = true // Re-enable the button on failure
                            // User creation failed
                            val errorMessage = task.exception?.message ?: "Unknown error occurred"
                            Toast.makeText(this, "Sign up failed: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setView(inflater.inflate(R.layout.loading_dialog, null))
        builder.setCancelable(false)
        loadingDialog = builder.create()
        loadingDialog.show()
    }

    private fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onBackPressed() {
        // Call super implementation
        showDialogBox.showExitConfirmationDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                val profileImg = findViewById<CircleImageView>(R.id.profileImg1)
                profileImg.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(onSuccess: (Uri) -> Unit) {
        if (filePath != null) {
            val ref = firebaseStorage.reference.child("images/" + UUID.randomUUID().toString())
            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        onSuccess(uri)
                    }
                    Toast.makeText(this@SignUP, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    hideLoadingDialog()
                    signupButton.isEnabled = true // Re-enable the button on failure
                    Toast.makeText(this@SignUP, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                }
        }
    }
}
