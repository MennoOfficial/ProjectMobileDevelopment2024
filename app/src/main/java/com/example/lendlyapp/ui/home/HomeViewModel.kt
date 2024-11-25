package com.example.lendlyapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lendlyapp.models.Product
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products
    
    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts
    
    private var currentSearchQuery: String = ""
    private var currentTag: String? = null
    
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

    private fun filterProducts() {
        val allProducts = _products.value ?: return
        
        _filteredProducts.value = allProducts.filter { product ->
            val matchesSearch = product.name.contains(currentSearchQuery, ignoreCase = true) ||
                              product.details.contains(currentSearchQuery, ignoreCase = true)
            val matchesTag = currentTag == null || product.tag == currentTag
            
            matchesSearch && matchesTag
        }
    }
}