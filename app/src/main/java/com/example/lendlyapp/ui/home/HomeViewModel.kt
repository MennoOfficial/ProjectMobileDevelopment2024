package com.example.lendlyapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lendlyapp.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class HomeViewModel : ViewModel() {
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products
    
    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts
    
    private val _currentRadius = MutableLiveData<Float>(5f)
    val currentRadius: LiveData<Float> = _currentRadius
    
    private var allProducts = listOf<Product>()
    private var currentSearchQuery = ""
    private var currentTag: String? = null
    private var currentCenter: GeoPoint? = null
    private val _userLocation = MutableLiveData<GeoPoint>()
    val userLocation: LiveData<GeoPoint> = _userLocation

    val tags = listOf(
        "Electronics",
        "Garden Tools",
        "Sports Equipment",
        "Home Appliances",
        "Books",
        "Music Instruments",
        "Camping Gear",
        "Party Supplies",
        "Tools",
        "Games"
    )

    fun loadProducts() {
        FirebaseFirestore.getInstance()
            .collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    val productList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java)?.copy(id = doc.id)
                    }
                    _products.value = productList
                    allProducts = productList
                    filterProducts()
                }
            }
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        filterProducts()
    }

    fun setTag(tag: String?) {
        currentTag = tag
        filterProducts()
    }

    fun setRadius(radius: Float) {
        _currentRadius.value = radius
        filterProducts()
    }

    fun setMapCenter(center: GeoPoint) {
        filterProducts()
    }

    fun setUserLocation(location: com.google.firebase.firestore.GeoPoint) {
        _userLocation.value = location
        filterProducts()
    }

    private fun filterProducts() {
        var filtered = allProducts

        // Apply search filter
        if (currentSearchQuery.isNotEmpty()) {
            filtered = filtered.filter { it.name.contains(currentSearchQuery, ignoreCase = true) }
        }

        // Apply tag filter
        currentTag?.let { tag ->
            filtered = filtered.filter { it.tag == tag }
        }

        // Apply radius filter from user's location
        _userLocation.value?.let { center ->
            val radiusKm = _currentRadius.value ?: 5f
            filtered = filtered.filter { product ->
                product.location?.let { location ->
                    val distanceKm = calculateDistance(
                        center.latitude, center.longitude,
                        location.latitude, location.longitude
                    )
                    println("Product distance: $distanceKm km, Radius: $radiusKm km")
                    distanceKm <= radiusKm
                } ?: false
            }
        }

        _filteredProducts.value = filtered
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c  // Return distance in kilometers
    }
}