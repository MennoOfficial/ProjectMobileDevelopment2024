package com.example.lendlyapp.ui.ongoing

import OngoingRentalsFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.lendlyapp.R
import com.example.lendlyapp.databinding.FragmentOngoingBinding

class OngoingFragment : Fragment() {
    private var _binding: FragmentOngoingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOngoingBinding.inflate(inflater, container, false)
        
        // Add OngoingRentalsFragment to the container
        childFragmentManager.beginTransaction()
            .replace(R.id.rentalsContainer, OngoingRentalsFragment())
            .commit()
        
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
