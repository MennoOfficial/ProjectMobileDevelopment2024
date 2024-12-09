package com.example.lendlyapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lendlyapp.databinding.ActivityEditProductBinding
import com.example.lendlyapp.models.Product
import com.google.firebase.firestore.FirebaseFirestore

class EditProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProductBinding
    private lateinit var product: Product
    private var productId: String = ""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Product"

        productId = intent.getStringExtra("product_id") ?: run {
            finish()
            return
        }

        loadProduct()
        setupSaveButton()
    }

    private fun loadProduct() {
        db.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    product = document.toObject(Product::class.java)?.copy(id = document.id)
                        ?: run {
                            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show()
                            finish()
                            return@addOnSuccessListener
                        }
                    updateUI()
                } else {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun updateUI() {
        binding.apply {
            nameEditText.setText(product.name)
            priceEditText.setText(product.price.toString())
            descriptionEditText.setText(product.details)
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val price = binding.priceEditText.text.toString().toFloatOrNull()
            val description = binding.descriptionEditText.text.toString()

            if (name.isBlank() || price == null) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("products").document(productId)
                .update(
                    mapOf(
                        "name" to name,
                        "price" to price,
                        "details" to description
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}