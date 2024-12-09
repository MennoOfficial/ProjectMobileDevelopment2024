package com.example.lendlyapp.ui.you

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.lendlyapp.AddProductActivity
import com.example.lendlyapp.databinding.FragmentYouBinding
import com.example.lendlyapp.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.example.lendlyapp.EditProductActivity
import com.example.lendlyapp.adapters.ProductAdapter
import com.example.lendlyapp.EditProfileActivity
import com.example.lendlyapp.models.Product

class YouFragment : Fragment() {

    private var _binding: FragmentYouBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: YouViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(YouViewModel::class.java)
        _binding = FragmentYouBinding.inflate(inflater, container, false)

        setupUserDataObserver()
        setupProductsRecyclerView()
        setupButtons()
        setupEditProfileButton()

        viewModel.loadUserData()
        return binding.root
    }

    private fun setupUserDataObserver() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.nameTextView.text = "${userData.firstName} ${userData.lastName}"
            binding.emailTextView.text = userData.email
            binding.phoneTextView.text = userData.phone
            binding.addressTextView.text = "${userData.street} ${userData.houseNumber}, ${userData.postalCode} ${userData.city}, ${userData.country}"
        }
    }

    private fun setupProductsRecyclerView() {
        val adapter = ProductAdapter(
            products = emptyList(),
            onDetailsClick = { product ->
                val intent = Intent(requireContext(), EditProductActivity::class.java).apply {
                    putExtra("product_id", product.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { product ->
                showDeleteConfirmationDialog(product)
            }
        )
        
        binding.productsRecyclerView.adapter = adapter
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(context)
        
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.updateProducts(products)
        }
    }

    private fun setupButtons() {
        binding.addProductButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun setupEditProfileButton() {
        binding.editProfileButton.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete ${product.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        viewModel.deleteProduct(product.id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
