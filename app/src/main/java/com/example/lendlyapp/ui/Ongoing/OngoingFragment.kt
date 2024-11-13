package com.example.lendlyapp.ui.ongoing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.lendlyapp.databinding.FragmentOngoingBinding

class OngoingFragment : Fragment() {

    private var _binding: FragmentOngoingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ongoingViewModel = ViewModelProvider(this).get(OngoingViewModel::class.java)

        _binding = FragmentOngoingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textOngoing
        ongoingViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
