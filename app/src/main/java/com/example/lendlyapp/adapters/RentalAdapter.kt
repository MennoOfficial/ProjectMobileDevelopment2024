import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lendlyapp.R
import com.example.lendlyapp.models.RentalPeriod
import java.text.SimpleDateFormat
import java.util.Locale

class RentalAdapter : RecyclerView.Adapter<RentalAdapter.RentalViewHolder>() {
    private var rentals = listOf<RentalPeriod>()

    class RentalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(rental: RentalPeriod) {
            val timeStatus = TimeUtils.calculateRentalTimeStatus(rental.startDate, rental.endDate)
            
            // Load product image
            Glide.with(itemView.context)
                .load(rental.productImage)
                .into(itemView.findViewById(R.id.productImage))

            // Set product name
            itemView.findViewById<TextView>(R.id.productName).text = rental.productName

            // Set rental period
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            itemView.findViewById<TextView>(R.id.rentalPeriod).text = 
                "From ${dateFormat.format(rental.startDate)} to ${dateFormat.format(rental.endDate)}"

            // Set remaining time with color
            val remainingTimeText = itemView.findViewById<TextView>(R.id.remainingTime)
            val remainingDays = ((rental.endDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
            
            remainingTimeText.text = "Remaining: $remainingDays days"
            remainingTimeText.setTextColor(when (timeStatus) {
                RentalTimeStatus.CRITICAL -> Color.RED
                RentalTimeStatus.WARNING -> Color.parseColor("#FFA500") // Orange
                RentalTimeStatus.PLENTY -> Color.parseColor("#4CAF50") // Green
            })
        }
    }

    fun updateRentals(newRentals: List<RentalPeriod>) {
        rentals = newRentals
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rental, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        holder.bind(rentals[position])
    }

    override fun getItemCount() = rentals.size
} 