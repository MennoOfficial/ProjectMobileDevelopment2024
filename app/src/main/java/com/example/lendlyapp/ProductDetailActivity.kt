package com.example.lendlyapp

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.lendlyapp.databinding.ActivityProductDetailBinding
import com.example.lendlyapp.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.util.Calendar
import android.widget.CalendarView

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var product: Product
    private var selectedStartDate: Calendar? = null
    private var selectedEndDate: Calendar? = null

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
        setupCalendar()
        binding.rentButton.isEnabled = false
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

            priceText.text = "€${String.format("%.2f", product.price)} first day"
            extraDayPrice.text = "€${String.format("%.2f", product.price - 0.50)} per extra day"
            
            tagChip.text = product.tag
            descriptionText.text = product.details

            // Format the date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            datePostedText.text = "Posted on ${dateFormat.format(product.createdAt)}"
        }
    }

    private fun setupRentButton() {
        binding.rentButton.setOnClickListener {
            // TODO: Implement rent functionality
            Toast.makeText(this, "Rent functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCalendar() {
        val calendar = Calendar.getInstance()
        
        binding.calendarView.minDate = calendar.timeInMillis
        calendar.add(Calendar.YEAR, 1)
        binding.calendarView.maxDate = calendar.timeInMillis

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            
            if (selectedStartDate == null) {
                selectedStartDate = selectedCalendar
                selectedEndDate = null
            } else if (selectedEndDate == null && selectedCalendar.after(selectedStartDate)) {
                selectedEndDate = selectedCalendar
                updateSelectedDatesText()
                calculatePrice()
            } else {
                selectedStartDate = selectedCalendar
                selectedEndDate = null
            }
        }
    }

    private fun updateSelectedDatesText() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val start = selectedStartDate
        val end = selectedEndDate
        if (start != null && end != null) {
            binding.selectedDatesText.text = "Selected: ${dateFormat.format(start.time)} - ${dateFormat.format(end.time)}"
        }
    }

    private fun calculatePrice() {
        val start = selectedStartDate
        val end = selectedEndDate
        if (start != null && end != null) {
            val diffInMillis = end.timeInMillis - start.timeInMillis
            val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            val totalPrice = product.price + (days - 1) * (product.price - 0.50)
            binding.rentButton.text = "RENT - €${String.format("%.2f", totalPrice)}"
            binding.rentButton.isEnabled = true
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