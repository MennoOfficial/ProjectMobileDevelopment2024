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

        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.nameTextView.text = "${userData.firstName} ${userData.lastName}"
            binding.emailTextView.text = userData.email
            binding.phoneTextView.text = userData.phone
            binding.addressTextView.text = userData.address
        }

        binding.addProductButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        viewModel.loadUserData()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
