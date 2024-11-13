package com.example.lendlyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.lendlyapp.databinding.ActivityRegisterBinding
import com.example.lendlyapp.MainActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Add animations
        binding.logoImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        binding.appNameText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        binding.titleText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        binding.subtitleText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        binding.formCard.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))

        auth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            val firstName = binding.firstNameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()
            val phone = binding.phoneEditText.text.toString()
            val address = binding.addressEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && phone.isNotEmpty() && 
                address.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(firstName, lastName, phone, address, email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginTextView.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(firstName: String, lastName: String, phone: String, address: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    
                    if (user != null) {
                        val userData = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "phone" to phone,
                            "address" to address,
                            "email" to email
                        )

                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("RegisterActivity", "User data saved successfully")
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterActivity", "Failed to save user data: ${e.message}")
                            }
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
