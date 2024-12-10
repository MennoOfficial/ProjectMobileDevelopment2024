package com.example.lendlyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.lendlyapp.databinding.ActivityRegisterBinding
import com.example.lendlyapp.utils.ThemeHelper

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.applyTheme(this)
        
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateThemeIcon()
        
        binding.themeToggleButton.setOnClickListener {
            ThemeHelper.toggleTheme(this)
            updateThemeIcon()
        }

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
            val street = binding.streetEditText.text.toString()
            val houseNumber = binding.houseNumberEditText.text.toString()
            val city = binding.cityEditText.text.toString()
            val postalCode = binding.postalCodeEditText.text.toString()
            val country = binding.countryEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && phone.isNotEmpty() && 
                street.isNotEmpty() && houseNumber.isNotEmpty() && city.isNotEmpty() &&
                postalCode.isNotEmpty() && country.isNotEmpty() && 
                email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(firstName, lastName, phone, street, houseNumber, 
                           city, postalCode, country, email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginTextView.setOnClickListener {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        updateThemeIcon(menu.findItem(R.id.action_toggle_theme))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_theme -> {
                ThemeHelper.toggleTheme(this)
                updateThemeIcon(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateThemeIcon(menuItem: MenuItem) {
        menuItem.setIcon(
            if (ThemeHelper.isDarkMode(this)) 
                R.drawable.ic_light_mode 
            else 
                R.drawable.ic_dark_mode
        )
    }

    private fun registerUser(
        firstName: String, 
        lastName: String, 
        phone: String, 
        street: String,
        houseNumber: String,
        city: String,
        postalCode: String,
        country: String,
        email: String, 
        password: String
    ) {
        // Show loading indicator or disable register button
        binding.registerButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    
                    if (user != null) {
                        val userData = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "phone" to phone,
                            "street" to street,
                            "houseNumber" to houseNumber,
                            "city" to city,
                            "postalCode" to postalCode,
                            "country" to country,
                            "email" to email
                        )

                        // Create a reference to the Firestore database
                        val db = FirebaseFirestore.getInstance()
                        
                        // Set the user document with a specific ID (user.uid)
                        db.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("RegisterActivity", "User data saved successfully")
                                // Navigate to MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterActivity", "Failed to save user data: ${e.message}")
                                // Show error message to user
                                Toast.makeText(
                                    this,
                                    "Failed to create user profile: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                // Delete the authentication user if Firestore creation fails
                                user.delete()
                                binding.registerButton.isEnabled = true
                            }
                    }
                } else {
                    // Authentication failed
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.registerButton.isEnabled = true
                }
            }
    }

    private fun updateThemeIcon() {
        binding.themeToggleButton.setImageResource(
            if (ThemeHelper.isDarkMode(this)) 
                R.drawable.ic_light_mode 
            else 
                R.drawable.ic_dark_mode
        )
    }
}
