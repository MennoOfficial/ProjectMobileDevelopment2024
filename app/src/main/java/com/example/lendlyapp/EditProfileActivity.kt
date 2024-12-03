package com.example.lendlyapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lendlyapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.lendlyapp.models.UserData

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        loadUserData()
        setupSaveButton()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = document.toObject(UserData::class.java)
                    userData?.let { populateFields(it) }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateFields(userData: UserData) {
        binding.apply {
            firstNameEditText.setText(userData.firstName)
            lastNameEditText.setText(userData.lastName)
            phoneEditText.setText(userData.phone)
            streetEditText.setText(userData.street)
            houseNumberEditText.setText(userData.houseNumber)
            cityEditText.setText(userData.city)
            postalCodeEditText.setText(userData.postalCode)
            countryEditText.setText(userData.country)
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            
            val updatedData = hashMapOf(
                "firstName" to binding.firstNameEditText.text.toString(),
                "lastName" to binding.lastNameEditText.text.toString(),
                "phone" to binding.phoneEditText.text.toString(),
                "street" to binding.streetEditText.text.toString(),
                "houseNumber" to binding.houseNumberEditText.text.toString(),
                "city" to binding.cityEditText.text.toString(),
                "postalCode" to binding.postalCodeEditText.text.toString(),
                "country" to binding.countryEditText.text.toString()
            )

            firestore.collection("users").document(userId)
                .update(updatedData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 