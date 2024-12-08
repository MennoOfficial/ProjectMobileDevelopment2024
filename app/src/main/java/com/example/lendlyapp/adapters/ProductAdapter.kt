package com.example.lendlyapp.adapters

import android.content.Intent
import android.content.res.ColorStateList
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
import com.example.lendlyapp.ProductDetailActivity

class ProductAdapter(
    private var products: List<Product>,
    private val onLocationClick: ((Product) -> Unit)? = null,
    private val onDetailsClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            binding.apply {
                productName.text = product.name
                productPrice.text = String.format("%.2f€/day", product.price)
                
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(productImage)

                tagChip.apply {
                    text = product.tag.ifEmpty { 
                        when (product.status.uppercase()) {
                            "AVAILABLE" -> ProductStatus.AVAILABLE.displayName
                            "RENTED" -> ProductStatus.RENTED.displayName
                            "UNAVAILABLE" -> ProductStatus.UNAVAILABLE.displayName
                            else -> ProductStatus.AVAILABLE.displayName
                        }
                    }
                    visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
                    setBackgroundResource(R.drawable.bg_status_pill)
                    setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, 
                            if (product.tag.isNotEmpty()) {
                                R.color.primary
                            } else {
                                when (product.status.uppercase()) {
                                    "AVAILABLE" -> R.color.status_available
                                    "RENTED" -> R.color.status_rented
                                    else -> R.color.status_unavailable
                                }
                            }
                        )
                    )
                }

                viewDetailsButton.setOnClickListener { 
                    val intent = Intent(itemView.context, ProductDetailActivity::class.java)
                    intent.putExtra("product_id", product.id)
                    itemView.context.startActivity(intent)
                }
                
                if (onLocationClick != null) {
                    viewLocationButton.visibility = View.VISIBLE
                    viewLocationButton.setOnClickListener { onLocationClick.invoke(product) }
                } else {
                    viewLocationButton.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
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