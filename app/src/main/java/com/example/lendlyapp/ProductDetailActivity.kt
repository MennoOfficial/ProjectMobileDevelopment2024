package com.example.lendlyapp

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.lendlyapp.databinding.ActivityProductDetailBinding
import com.example.lendlyapp.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestoreException
import com.example.lendlyapp.models.RentalPeriod
import android.content.res.ColorStateList
import com.example.lendlyapp.models.ProductStatus
import com.google.firebase.firestore.FieldValue

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var product: Product
    private var selectedStartDate: Calendar = Calendar.getInstance()
    private var selectedEndDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get product ID from intent
        val productId = intent.getStringExtra("product_id") ?: run {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadProduct(productId)
        setupDatePickers()
        binding.rentButton.isEnabled = false
        setupRentButton()
    }

    private fun loadProduct(productId: String) {
        FirebaseFirestore.getInstance().collection("products")
            .document(productId)
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
            toolbarLayout.title = product.name
            
            Glide.with(this@ProductDetailActivity)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(productImage)

            priceText.apply {
                text = "€${String.format("%.2f", product.price)} first day"
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }
            extraDayPrice.apply {
                text = "€${String.format("%.2f", product.price - 0.50)} per extra day"
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }
            
            tagChip.apply {
                text = ProductStatus.valueOf(product.status.uppercase()).displayName
                chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, when (product.status.uppercase()) {
                        ProductStatus.AVAILABLE.name -> R.color.status_available
                        ProductStatus.RENTED.name -> R.color.status_rented
                        else -> R.color.status_unavailable
                    })
                )
                setTextColor(Color.WHITE)
            }
            descriptionText.text = product.details

            // Format the date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            datePostedText.text = "Posted on ${dateFormat.format(product.createdAt)}"

            // Disable rent button if product is not available
            rentButton.isEnabled = product.status == "available"
        }
    }

    private fun setupRentButton() {
        binding.rentButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Please login to rent items", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUser.uid == product.userId) {
                Toast.makeText(this, "You cannot rent your own product", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calculate total price
            val diffInMillis = selectedEndDate.timeInMillis - selectedStartDate.timeInMillis
            val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            val totalPrice = product.price + (days - 1) * (product.price - 0.50)

            // Create rental period
            val rentalPeriod = RentalPeriod(
                startDate = selectedStartDate.timeInMillis,
                endDate = selectedEndDate.timeInMillis,
                renterId = currentUser.uid,
                totalPrice = totalPrice,
                productId = product.id,
                productName = product.name,
                productImage = product.imageUrl
            )

            // Update product in Firestore
            val db = FirebaseFirestore.getInstance()
            db.runTransaction { transaction ->
                val productRef = db.collection("products").document(product.id)
                val productSnapshot = transaction.get(productRef)
                
                val currentProduct = productSnapshot.toObject(Product::class.java)
                if (currentProduct?.status != "available") {
                    throw FirebaseFirestoreException(
                        "Product is no longer available",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }

                // Create updates map
                val updates = hashMapOf(
                    "status" to "rented",
                    "renters" to FieldValue.arrayUnion(currentUser.uid),
                    "rentalPeriods" to FieldValue.arrayUnion(mapOf(
                        "startDate" to selectedStartDate.timeInMillis,
                        "endDate" to selectedEndDate.timeInMillis,
                        "renterId" to currentUser.uid,
                        "totalPrice" to totalPrice
                    ))
                )

                transaction.update(productRef, updates)
            }.addOnSuccessListener {
                Toast.makeText(this, "Product rented successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to rent: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDatePickers() {
        val today = Calendar.getInstance()
        
        // Set minimum date to today for both pickers
        binding.startDatePicker.minDate = today.timeInMillis
        binding.endDatePicker.minDate = today.timeInMillis

        // Set maximum date to 1 year from now
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.YEAR, 1)
        binding.startDatePicker.maxDate = maxDate.timeInMillis
        binding.endDatePicker.maxDate = maxDate.timeInMillis

        // Setup start date picker listener
        binding.startDatePicker.init(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ) { _, year, month, day ->
            selectedStartDate.set(year, month, day)
            
            // Update end date picker's minimum date
            binding.endDatePicker.minDate = selectedStartDate.timeInMillis
            
            updateSelectedDatesAndPrice()
        }

        // Setup end date picker listener
        binding.endDatePicker.init(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ) { _, year, month, day ->
            selectedEndDate.set(year, month, day)
            updateSelectedDatesAndPrice()
        }
    }

    private fun updateSelectedDatesAndPrice() {
        if (selectedEndDate.after(selectedStartDate)) {
            val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
            binding.selectedDatesText.text = "Selected Period:\n${dateFormat.format(selectedStartDate.time)} - ${dateFormat.format(selectedEndDate.time)}"
            binding.selectedDatesText.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            
            val diffInMillis = selectedEndDate.timeInMillis - selectedStartDate.timeInMillis
            val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            val totalPrice = product.price + (days - 1) * (product.price - 0.50)
            
            binding.rentButton.apply {
                isEnabled = true
                text = "RENT NOW - €${String.format("%.2f", totalPrice)}"
                setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
            }
        } else {
            binding.selectedDatesText.text = "Please select a valid date range"
            binding.selectedDatesText.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
            binding.rentButton.apply {
                isEnabled = false
                text = "RENT"
                setBackgroundColor(ContextCompat.getColor(context, R.color.text_hint))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 