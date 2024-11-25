package com.example.lendlyapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.lendlyapp.databinding.FragmentHomeBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        
        // Initialize the map
        Configuration.getInstance().userAgentValue = requireActivity().packageName
        map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(15.0)
        
        // Set default position (e.g., Brussels)
        val startPoint = GeoPoint(50.8503, 4.3517)
        map.controller.setCenter(startPoint)

        // Observe products from ViewModel
        val viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.loadProducts()
        
        viewModel.products.observe(viewLifecycleOwner) { products ->
            map.overlays.clear()
            products.forEach { product ->
                product.location?.let { location ->
                    val marker = Marker(map)
                    marker.position = GeoPoint(location.latitude, location.longitude)
                    marker.title = "${product.name} - €${product.price}"
                    map.overlays.add(marker)
                }
            }
            map.invalidate()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}