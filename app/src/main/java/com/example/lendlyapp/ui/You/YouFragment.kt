package com.example.lendlyapp.ui.you

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
import com.example.lendlyapp.adapters.ProductAdapter

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
        val adapter = ProductAdapter(emptyList()) { product ->
            // Handle product click - implement edit functionality
            Toast.makeText(context, "Edit ${product.name}", Toast.LENGTH_SHORT).show()
        }
        
        binding.productsRecyclerView.layoutManager = 
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.productsRecyclerView.adapter = adapter

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
