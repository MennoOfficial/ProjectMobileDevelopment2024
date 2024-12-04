import com.example.lendlyapp.R

enum class ProductStatus(val displayName: String, val color: Int) {
    AVAILABLE("Available", R.color.status_available),
    RENTED("Rented", R.color.status_rented),
    UNAVAILABLE("Unavailable", R.color.status_unavailable)
} 