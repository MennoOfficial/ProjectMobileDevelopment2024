package com.example.lendlyapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lendlyapp.R
import com.example.lendlyapp.adapters.ProductAdapter
import com.example.lendlyapp.databinding.FragmentHomeBinding
import com.example.lendlyapp.models.Product
import com.example.lendlyapp.utils.CircleOverlay
import com.example.lendlyapp.utils.CustomMarker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import com.google.android.material.chip.Chip
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: ProductAdapter
    private lateinit var radiusOverlay: CircleOverlay
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLocationPermission()
        setupRadiusSlider()
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
        
        // Add radius overlay
        radiusOverlay = CircleOverlay().apply {
            outlinePaint.color = ContextCompat.getColor(requireContext(), R.color.primary)
            outlinePaint.alpha = 128
            fillPaint.color = ContextCompat.getColor(requireContext(), R.color.primary)
            fillPaint.alpha = 32
        }
        map.overlays.add(radiusOverlay)
        
        // Update center when map is moved
        map.setMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                val center = GeoPoint(map.mapCenter.latitude, map.mapCenter.longitude)
                viewModel.setMapCenter(center)
                updateRadiusOverlay()
                return true
            }
            override fun onZoom(event: ZoomEvent?): Boolean {
                updateRadiusOverlay()
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            emptyList(),
            onLocationClick = { product ->
                product.location?.let { location ->
                    map.controller.animateTo(GeoPoint(location.latitude, location.longitude))
                    map.controller.setZoom(15.0)
                    
                    map.overlays.filterIsInstance<CustomMarker>()
                        .firstOrNull { it.product == product }
                        ?.showInfoWindow()
                }
            },
            onDetailsClick = { product ->
                // TODO: Implement navigation to product details
                Toast.makeText(context, "View details for ${product.name}", Toast.LENGTH_SHORT).show()
            }
        )
        
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
        // Remove only markers, keep the radius overlay
        map.overlays.removeAll { it is CustomMarker }
        
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
        
        // Ensure radius overlay is always on top
        map.overlays.remove(radiusOverlay)
        map.overlays.add(radiusOverlay)
        
        map.invalidate()
    }

    private fun setupRadiusSlider() {
        binding.radiusSlider.addOnChangeListener { _, value, _ ->
            binding.radiusText.text = "Radius: ${value.toInt()} km"
            viewModel.setRadius(value)
            updateRadiusOverlay()
        }
    }

    private fun updateRadiusOverlay() {
        val radiusKm = viewModel.currentRadius.value ?: 5f
        val center = map.mapCenter
        
        // Calculate appropriate zoom level based on radius
        val zoomLevel = when {
            radiusKm <= 1 -> 15.0
            radiusKm <= 5 -> 13.0
            radiusKm <= 10 -> 12.0
            radiusKm <= 20 -> 11.0
            radiusKm <= 30 -> 10.0
            else -> 9.0
        }
        map.controller.setZoom(zoomLevel)
        
        radiusOverlay.apply {
            setPoints(center)
            setRadius(radiusKm)
        }
        map.invalidate()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getUserLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showLocationPermissionDialog()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun getUserLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    map.controller.animateTo(userLocation)
                    viewModel.setMapCenter(userLocation)
                    updateRadiusOverlay()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLocationPermissionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage("We need your location to show nearby products")
            .setPositiveButton("Grant") { _, _ ->
                requestLocationPermission()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                }
            }
        }
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}