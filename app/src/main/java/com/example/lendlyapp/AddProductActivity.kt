package com.example.lendlyapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lendlyapp.databinding.ActivityAddProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

    private fun addProduct(name: String, price: Double, details: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val product = hashMapOf(
            "name" to name,
            "price" to price,
            "details" to details,
            "userId" to userId,
            "createdAt" to Date()
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
} 