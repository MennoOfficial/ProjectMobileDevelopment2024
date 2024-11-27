package com.example.lendlyapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lendlyapp.R
import com.example.lendlyapp.databinding.ItemProductBinding
import com.example.lendlyapp.models.Product
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
                productPrice.text = "€${String.format("%.2f", product.price)}"
                tagChip.text = product.tag

                Glide.with(productImage.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(productImage)

                viewDetailsButton.setOnClickListener { 
                    val intent = Intent(it.context, ProductDetailActivity::class.java)
                    intent.putExtra("product_id", product.id)
                    it.context.startActivity(intent)
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