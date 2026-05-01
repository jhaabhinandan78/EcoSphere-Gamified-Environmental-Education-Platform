package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.capstone.databinding.ActivityTopPerformersBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TopPerformersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopPerformersBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var performersAdapter: TopPerformersAdapter
    private val performers = mutableListOf<StudentData>()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tvPerformerCount: TextView
    
    // NEW: Multi-tenancy fields
    private var teacherSchoolId: String? = null
    private var assignedBatches: List<String> = emptyList()
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopPerformersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views via findViewById to avoid binding regeneration issues
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        tvPerformerCount = findViewById(R.id.tvPerformerCount)

        setupToolbar()
        setupSwipeRefresh()
        setupRecyclerView()
        loadTeacherInfo()
    }
    
    /**
     * NEW: Load teacher information to get assigned batches
     */
    private fun loadTeacherInfo() {
        currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            loadTopPerformers()
            return
        }
        
        firestore.collection("Users")
            .document(currentUserId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    teacherSchoolId = document.getString("schoolId")
                    val batchesArray = document.get("assignedBatches") as? List<*>
                    assignedBatches = batchesArray?.mapNotNull { it as? String } ?: emptyList()
                    
                    android.util.Log.d("TopPerformers", "Teacher school: $teacherSchoolId")
                    android.util.Log.d("TopPerformers", "Assigned batches: $assignedBatches")
                    
                    // Update toolbar subtitle based on context
                    updateToolbarSubtitle()
                }
                loadTopPerformers()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("TopPerformers", "Failed to load teacher info", e)
                loadTopPerformers()
            }
    }
    
    /**
     * NEW: Update toolbar subtitle to show context
     */
    private fun updateToolbarSubtitle() {
        val subtitle = when {
            assignedBatches.isNotEmpty() -> "Your Batches"
            teacherSchoolId != null -> "Your School"
            else -> {
                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                monthFormat.format(Date())
            }
        }
        binding.toolbar.subtitle = subtitle
    }

    private fun setupToolbar() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.toolbar.title = "🏆 Top Performers"
        binding.toolbar.subtitle = monthFormat.format(Date())
        binding.toolbar.setTitleTextColor(0xFFFFFFFF.toInt())
        binding.toolbar.setSubtitleTextColor(0xFFFFE0B2.toInt())
        binding.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(
            0xFF00897B.toInt(), // Teal
            0xFF4DB6AC.toInt(), // Light teal
            0xFF00695C.toInt()  // Dark teal
        )
        swipeRefreshLayout.setOnRefreshListener {
            android.util.Log.d("TopPerformers", "Swipe refresh triggered")
            loadTopPerformers()
        }
    }

    private fun setupRecyclerView() {
        performersAdapter = TopPerformersAdapter(performers)
        binding.recyclerViewPerformers.apply {
            layoutManager = LinearLayoutManager(this@TopPerformersActivity)
            adapter = performersAdapter
        }
    }

    private fun loadTopPerformers() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.recyclerViewPerformers.visibility = View.GONE
        tvPerformerCount.visibility = View.GONE

        android.util.Log.d("TopPerformers", "Loading top performers...")

        // NEW: Filter by batches or school
        val query = when {
            assignedBatches.isNotEmpty() -> {
                firestore.collection("Users")
                    .whereEqualTo("role", "student")
                    .whereIn("batchId", assignedBatches.take(10))
            }
            teacherSchoolId != null -> {
                firestore.collection("Users")
                    .whereEqualTo("role", "student")
                    .whereEqualTo("schoolId", teacherSchoolId)
            }
            else -> {
                firestore.collection("Users")
                    .whereEqualTo("role", "student")
            }
        }

        query.orderBy("ecoPoints", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                android.util.Log.d("TopPerformers", "✅ Query successful! Found ${documents.size()} students")

                // Stop swipe refresh animation
                swipeRefreshLayout.isRefreshing = false

                performers.clear()
                for (doc in documents) {
                    try {
                        val points = doc.getLong("ecoPoints") ?: 0
                        android.util.Log.d("TopPerformers", "Student: ${doc.getString("name")} - Points: $points")

                        val student = StudentData(
                            uid = doc.id,
                            name = doc.getString("name") ?: "Unknown",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: "",
                            dob = doc.getString("dob") ?: "",
                            gender = doc.getString("gender") ?: "",
                            ecoPoints = points,
                            profilePictureUrl = doc.getString("profilePictureUrl") ?: ""
                        )
                        performers.add(student)
                    } catch (e: Exception) {
                        android.util.Log.e("TopPerformers", "Error parsing student doc ${doc.id}", e)
                    }
                }

                binding.progressBar.visibility = View.GONE

                if (performers.isEmpty()) {
                    android.util.Log.d("TopPerformers", "No performers to display")
                    binding.tvEmptyState.visibility = View.VISIBLE
                    
                    // NEW: Context-aware empty message
                    val emptyMessage = when {
                        assignedBatches.isNotEmpty() -> "🏆 No performers yet\n\nStudents in your batches will appear here\nonce they start earning points"
                        teacherSchoolId != null -> "🏆 No performers yet\n\nStudents in your school will appear here\nonce they start earning points"
                        else -> "🏆 No performers yet\n\nTop students will appear here\nonce they start earning points"
                    }
                    binding.tvEmptyState.text = emptyMessage
                    binding.recyclerViewPerformers.visibility = View.GONE
                    tvPerformerCount.visibility = View.GONE
                } else {
                    android.util.Log.d("TopPerformers", "Displaying ${performers.size} performers")
                    binding.tvEmptyState.visibility = View.GONE
                    binding.recyclerViewPerformers.visibility = View.VISIBLE
                    tvPerformerCount.visibility = View.VISIBLE
                    
                    // NEW: Context-aware count message
                    val countMessage = when {
                        assignedBatches.isNotEmpty() -> "Top ${performers.size} students in your batches"
                        teacherSchoolId != null -> "Top ${performers.size} students in your school"
                        else -> "Showing top ${performers.size} students this month"
                    }
                    tvPerformerCount.text = countMessage
                    performersAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("TopPerformers", "❌ Failed to load performers", e)
                android.util.Log.e("TopPerformers", "Error message: ${e.message}")

                // Stop swipe refresh animation
                swipeRefreshLayout.isRefreshing = false

                binding.progressBar.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                tvPerformerCount.visibility = View.GONE

                val errorMsg = when {
                    e.message?.contains("index") == true ->
                        "❌ Database index required\n\nClick the link in Android Studio Logcat\nto create the required index"
                    e.message?.contains("PERMISSION_DENIED") == true || e.message?.contains("permission") == true ->
                        "❌ Permission denied\n\nCheck Firestore security rules\nfor Users collection"
                    e.message?.contains("network") == true || e.message?.contains("UNAVAILABLE") == true ->
                        "❌ Network Error\n\nCheck your internet connection and try again"
                    else ->
                        "❌ Error loading data\n\n${e.message}\n\nCheck Logcat for details"
                }

                binding.tvEmptyState.text = errorMsg
                binding.recyclerViewPerformers.visibility = View.GONE
            }
    }
}
