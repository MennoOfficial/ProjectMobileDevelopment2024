package com.example.lendlyapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.lendlyapp.R
import com.example.lendlyapp.databinding.CustomMarkerBinding
import com.example.lendlyapp.models.Product
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomMarker(
    mapView: MapView,
    private val context: Context,
    val product: Product
) : Marker(mapView) {

    init {
        val binding = CustomMarkerBinding.inflate(LayoutInflater.from(context))
        binding.markerTitle.text = product.name
        binding.markerPrice.text = "€${product.price}"
        
        icon = createBitmapFromView(binding.root)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        
        infoWindow = ProductInfoWindow(mapView, product)
    }
}

class ProductInfoWindow(
    mapView: MapView,
    private val product: Product
) : InfoWindow(R.layout.custom_marker, mapView) {

    override fun onOpen(item: Any?) {
        val view = mView
        view.findViewById<TextView>(R.id.markerTitle).text = product.name
        view.findViewById<TextView>(R.id.markerPrice).text = "€${product.price}"
    }

    override fun onClose() {
        // Optional: Add any cleanup code here
    }
}

fun Marker.createBitmapFromView(view: View): BitmapDrawable {
    if (view.layoutParams == null) {
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    view.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )

    view.layout(0, 0, view.measuredWidth, view.measuredHeight)

    val bitmap = Bitmap.createBitmap(
        view.measuredWidth,
        view.measuredHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    view.draw(canvas)

    return BitmapDrawable(view.resources, bitmap)
} 