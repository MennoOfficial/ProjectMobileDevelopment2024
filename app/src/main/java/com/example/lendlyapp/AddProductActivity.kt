package com.example.lendlyapp

import android.R
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lendlyapp.databinding.ActivityAddProductBinding
import com.example.lendlyapp.utils.GeocodingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import com.google.firebase.firestore.GeoPoint

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userStreet: String = ""
    private var userCity: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Fetch user data when activity starts
        fetchUserData()

        // Setup the tag spinner
        setupTagSpinner()

        binding.addProductButton.setOnClickListener {
            val name = binding.productNameEditText.text.toString()
            val priceText = binding.priceEditText.text.toString()
            val details = binding.detailsEditText.text.toString()

            if (name.isNotEmpty() && priceText.isNotEmpty() && details.isNotEmpty()) {
                try {
                    val price = priceText.toDouble()
                    addProduct(name, price, details)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        userStreet = document.getString("street") ?: ""
                        userCity = document.getString("city") ?: ""
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addProduct(name: String, price: Double, details: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        if (userStreet.isEmpty() || userCity.isEmpty()) {
            Toast.makeText(this, "User address not available", Toast.LENGTH_SHORT).show()
            return
        }

        val geoPoint = GeocodingUtil.getGeoPointFromAddress(this, userStreet, userCity)

        val product = hashMapOf(
            "name" to name,
            "price" to price,
            "details" to details,
            "userId" to userId,
            "createdAt" to Date(),
            "location" to geoPoint,
            "tag" to binding.tagSpinner.selectedItem.toString()
        )

        firestore.collection("products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun setupTagSpinner() {
    val tags = listOf("Electronics", "Garden Tools", "Sports Equipment", "Home Appliances", "Books", "Music Instruments", "Camping Gear", "Party Supplies", "Tools", "Games")
    val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, tags)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    binding.tagSpinner.adapter = adapter
}
} 