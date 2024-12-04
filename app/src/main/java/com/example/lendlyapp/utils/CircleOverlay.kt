package com.example.lendlyapp.utils

import android.graphics.Canvas
import android.graphics.Paint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.api.IGeoPoint

class CircleOverlay : Overlay() {
    private var center: GeoPoint? = null
    private var radius: Double = 5000.0 // Default 5km in meters
    
    val outlinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }
    
    val fillPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    fun setPoints(center: IGeoPoint) {
        this.center = center as GeoPoint
    }

    fun setRadius(radiusKm: Float) {
        this.radius = radiusKm * 1000.0  // Convert km to meters
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return

        center?.let { geoCenter ->
            val proj = mapView.projection
            val point = proj.toPixels(geoCenter, null)
            
            // Calculate pixels radius based on meters
            val radiusPoint = GeoPoint(
                geoCenter.latitude,
                geoCenter.longitude + (radius / 111320.0) // Convert meters to degrees
            )
            val radiusPixels = proj.toPixels(radiusPoint, null)
            val radiusSize = Math.abs(radiusPixels.x - point.x)

            canvas.drawCircle(
                point.x.toFloat(),
                point.y.toFloat(),
                radiusSize.toFloat(),
                fillPaint
            )
            canvas.drawCircle(
                point.x.toFloat(),
                point.y.toFloat(),
                radiusSize.toFloat(),
                outlinePaint
            )
        }
    }
} 