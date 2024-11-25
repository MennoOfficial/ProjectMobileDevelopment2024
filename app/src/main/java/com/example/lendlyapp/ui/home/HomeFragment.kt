package com.example.lendlyapp.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lendlyapp.adapters.ProductAdapter
import com.example.lendlyapp.databinding.FragmentHomeBinding
import com.example.lendlyapp.models.Product
import com.example.lendlyapp.utils.CustomMarker
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import com.google.android.material.chip.Chip

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        
        setupMap()
        setupRecyclerView()
        
        return binding.root
    }

    private fun setupMap() {
        Configuration.getInstance().userAgentValue = requireActivity().packageName
        map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        
        // Enable multitouch
        map.setMultiTouchControls(true)
        
        // Hide default zoom buttons
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        
        // Enable rotation gestures
        val rotationGestureOverlay = RotationGestureOverlay(map)
        rotationGestureOverlay.isEnabled = true
        map.overlays.add(rotationGestureOverlay)
        
        // Set initial position and zoom
        map.controller.setZoom(6.0)
        val startPoint = GeoPoint(50.8503, 4.3517) // Belgium
        map.controller.setCenter(startPoint)
        
        // Prevent map from intercepting parent scroll
        map.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(emptyList()) { product ->
            product.location?.let { location ->
                // Zoom to product location
                map.controller.animateTo(GeoPoint(location.latitude, location.longitude))
                map.controller.setZoom(15.0)
                
                // Find and open the marker for this product
                map.overlays.filterIsInstance<CustomMarker>()
                    .firstOrNull { it.product == product }
                    ?.showInfoWindow()
            }
        }
        
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.productsRecyclerView.adapter = adapter

        viewModel.loadProducts()
        setupSearchAndFilter()
    }

    private fun setupSearchAndFilter() {
        // Setup search
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Setup tag chips
        viewModel.tags.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.setTag(if (isChecked) tag else null)
                }
            }
            binding.tagChipGroup.addView(chip)
        }

        // Observe filtered products
        viewModel.filteredProducts.observe(viewLifecycleOwner) { products ->
            adapter.updateProducts(products)
            updateMapMarkers(products)
        }
    }

    private fun updateMapMarkers(products: List<Product>) {
        map.overlays.clear()
        products.forEach { product ->
            product.location?.let { location ->
                val marker = CustomMarker(map, requireContext(), product).apply {
                    position = GeoPoint(location.latitude, location.longitude)
                    setOnMarkerClickListener { marker, mapView ->
                        marker.showInfoWindow()
                        mapView.controller.animateTo(marker.position)
                        true
                    }
                }
                map.overlays.add(marker)
            }
        }
        map.invalidate()
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