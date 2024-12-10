package com.example.lendlyapp.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lendlyapp.R
import com.example.lendlyapp.databinding.ItemProductBinding
import com.example.lendlyapp.models.Product
import com.example.lendlyapp.models.ProductStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.lendlyapp.models.UserData

class ProductAdapter(
    var products: List<Product>,
    private val onDetailsClick: (Product) -> Unit,
    private val onDeleteClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            binding.apply {
                productName.text = product.name
                productPrice.text = String.format("%.2f€/day", product.price)
                
                // Set owner name
                if (product.userId == currentUserId) {
                    ownerName.text = "You"
                    Log.d("ProductAdapter", "Current user's product")
                } else {
                    Log.d("ProductAdapter", "Fetching user: ${product.userId}")
                    db.collection("users")
                        .document(product.userId)
                        .get()
                        .addOnSuccessListener { document ->
                            Log.d("ProductAdapter", "Document snapshot: $document")
                            Log.d("ProductAdapter", "Document data: ${document.data}")
                            Log.d("ProductAdapter", "User document exists: ${document.exists()}")
                            if (document != null && document.exists()) {
                                val userData = document.toObject(UserData::class.java)
                                Log.d("ProductAdapter", "UserData object: $userData")
                                ownerName.text = userData?.firstName ?: "Unknown"
                            } else {
                                Log.d("ProductAdapter", "Document doesn't exist for ID: ${product.userId}")
                                ownerName.text = "Unknown"
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProductAdapter", "Error fetching user", e)
                            ownerName.text = "Unknown"
                        }
                }

                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(productImage)

                statusPill.apply {
                    text = when (product.status.uppercase()) {
                        "AVAILABLE" -> ProductStatus.AVAILABLE.displayName
                        "RENTED" -> ProductStatus.RENTED.displayName
                        else -> ProductStatus.UNAVAILABLE.displayName
                    }
                    background.setTint(
                        ContextCompat.getColor(itemView.context, when (product.status.uppercase()) {
                            "AVAILABLE" -> R.color.status_available
                            "RENTED" -> R.color.status_rented
                            else -> R.color.status_unavailable
                        })
                    )
                }

                // Add tag chip if it exists
                if (product.tag.isNotEmpty()) {
                    tagChip.apply {
                        visibility = View.VISIBLE
                        text = product.tag
                        chipBackgroundColor = ColorStateList.valueOf(
                            ContextCompat.getColor(itemView.context, R.color.tag_background)
                        )
                        setTextColor(ContextCompat.getColor(itemView.context, R.color.tag_text))
                    }
                } else {
                    tagChip.visibility = View.GONE
                }

                // Make the entire card clickable
                root.setOnClickListener { 
                    onDetailsClick(product)
                }

                // Show delete button only for user's own products
                if (product.userId == currentUserId) {
                    deleteButton.visibility = View.VISIBLE
                    deleteButton.setOnClickListener {
                        onDeleteClick?.invoke(product)
                    }
                } else {
                    deleteButton.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}