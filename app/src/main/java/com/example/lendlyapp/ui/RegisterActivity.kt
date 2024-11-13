package com.example.lendlyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.lendlyapp.databinding.ActivityRegisterBinding
import com.example.lendlyapp.MainActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(name, email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginTextView.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        Log.d("RegisterActivity", "Starting registration for email: $email")
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterActivity", "User created successfully")
                    // User is automatically signed in after creation
                    val user = auth.currentUser
                    
                    if (user != null) {
                        val userRef = database.reference.child("users").child(user.uid)
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email
                        )

                        // Navigate to MainActivity immediately after registration
                        // Don't wait for database operation to complete
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        
                        // Save user data in background
                        userRef.setValue(userData)
                            .addOnSuccessListener {
                                Log.d("RegisterActivity", "User data saved successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterActivity", "Failed to save user data: ${e.message}")
                            }
                        
                        // Show success message and start MainActivity
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Log.e("RegisterActivity", "Registration failed: ${task.exception?.message}")
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
