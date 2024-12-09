import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lendlyapp.adapters.ProductAdapter
import com.example.lendlyapp.databinding.FragmentOngoingRentalsBinding
import com.example.lendlyapp.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import com.example.lendlyapp.ProductDetailActivity

class OngoingRentalsFragment : Fragment() {
    private lateinit var binding: FragmentOngoingRentalsBinding
    private lateinit var adapter: ProductAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOngoingRentalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = ProductAdapter(
            products = emptyList(),
            onLocationClick = { /* Not needed for ongoing rentals */ },
            onDetailsClick = { product ->
                val intent = Intent(context, ProductDetailActivity::class.java).apply {
                    putExtra("product_id", product.id)
                }
                startActivity(intent)
            }
        )
        
        binding.recyclerView.apply {
            this.adapter = this@OngoingRentalsFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
        
        fetchRentedProducts()
    }

    private fun fetchRentedProducts() {
        currentUser?.let { user ->
            db.collection("products")
                .whereEqualTo("status", "rented")
                .whereArrayContains("renters", user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(context, "Error fetching products", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    val products = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Product::class.java)?.copy(id = doc.id)
                    } ?: listOf()

                    adapter.products = products
                    adapter.notifyDataSetChanged()
                }
        }
    }
} 