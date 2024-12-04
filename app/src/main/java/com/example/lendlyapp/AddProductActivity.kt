package com.example.lendlyapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import com.example.lendlyapp.databinding.ActivityAddProductBinding
import com.example.lendlyapp.utils.GeocodingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.util.Log

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userStreet: String = ""
    private var userCity: String = ""
    private var selectedImageUri: Uri? = null
    private lateinit var photoUri: Uri
    private lateinit var currentPhotoPath: String
    private companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

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

        binding.productImageView.setOnClickListener {
            openImagePicker()
        }

        binding.addProductButton.setOnClickListener {
            val name = binding.productNameEditText.text.toString()
            val priceText = binding.priceEditText.text.toString()
            val details = binding.detailsEditText.text.toString()

            if (name.isNotEmpty() && priceText.isNotEmpty() && details.isNotEmpty() && selectedImageUri != null) {
                try {
                    val price = priceText.toDouble()
                    uploadImageAndAddProduct(name, price, details)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            }
        }

        binding.addPictureButton.setOnClickListener {
            openImagePicker()
        }
    }

    private fun checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermissions()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val photoFile = createImageFile() // Create a file to save the image
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        imagePickerLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            selectedImageUri = data?.data ?: photoUri // Use photoUri if the image was taken with the camera
            binding.productImageView.setImageURI(selectedImageUri)
            binding.productImageView.visibility = View.VISIBLE
        }
    }

    private fun uploadImageAndAddProduct(name: String, price: Double, details: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("product_images/${UUID.randomUUID()}.jpg")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        addProduct(name, price, details, downloadUrl.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addProduct(name: String, price: Double, details: String, imageUrl: String) {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show()
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
            "imageUrl" to imageUrl,
            "tag" to binding.tagSpinner.selectedItem.toString(),
            "status" to ProductStatus.AVAILABLE.name
        )

        Log.d("AddProductActivity", "Adding product: $product")

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
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tags)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.tagSpinner.adapter = adapter
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
}