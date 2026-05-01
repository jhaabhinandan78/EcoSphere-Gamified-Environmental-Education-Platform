package com.example.capstone

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.capstone.databinding.ActivityLiveActivityFeedBinding
import com.example.capstone.models.ActivityFeed
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LiveActivityFeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveActivityFeedBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var activityAdapter: ActivityFeedAdapter
    private val activities = mutableListOf<ActivityFeed>()
    private var teacherSchoolId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swipeRefreshLayout.setOnRefreshListener { loadActivities() }

        setupToolbar()
        setupRecyclerView()
        loadTeacherInfo()
    }
    
    /**
     * NEW: Load teacher information to get school
     */
    private fun loadTeacherInfo() {
        currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            loadActivities()
            return
        }
        
        firestore.collection("Users")
            .document(currentUserId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    teacherSchoolId = document.getString("schoolId")
                    android.util.Log.d("LiveActivity", "Teacher school: $teacherSchoolId")
                }
                loadActivities()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("LiveActivity", "Failed to load teacher info", e)
                loadActivities()
            }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Activity Feed"
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.swipeRefreshLayout.setColorSchemeColors(
            0xFF4CAF50.toInt(),
            0xFF388E3C.toInt(),
            0xFF2E7D32.toInt()
        )
    }

    private fun setupRecyclerView() {
        activityAdapter = ActivityFeedAdapter(activities)
        binding.recyclerViewActivities.apply {
            layoutManager = LinearLayoutManager(this@LiveActivityFeedActivity)
            adapter = activityAdapter
        }
    }

    private fun loadActivities() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.recyclerViewActivities.visibility = View.GONE

        android.util.Log.d("LiveActivity", "Loading activities from Firestore...")

        // NEW: Server-side filtering by school (now that ActivityLogger adds schoolId)
        val query = if (teacherSchoolId != null) {
            firestore.collection("ActivityFeed")
                .whereEqualTo("schoolId", teacherSchoolId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
        } else {
            // Admin mode - load all activities
            firestore.collection("ActivityFeed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
        }
        
        query.get()
            .addOnSuccessListener { documents ->
                android.util.Log.d("LiveActivity", "✅ Query successful! Found ${documents.size()} documents")
                
                binding.swipeRefreshLayout.isRefreshing = false
                
                activities.clear()
                var validCount = 0
                var invalidCount = 0
                
                for (doc in documents) {
                    try {
                        android.util.Log.d("LiveActivity", "Processing doc ${doc.id}: ${doc.data}")
                        
                        // Validate required fields
                        val userId = doc.getString("userId")
                        val userName = doc.getString("userName")
                        val activityType = doc.getString("activityType")
                        val timestamp = doc.getTimestamp("timestamp")
                        
                        if (userId.isNullOrEmpty() || userName.isNullOrEmpty() || activityType.isNullOrEmpty() || timestamp == null) {
                            android.util.Log.w("LiveActivity", "Skipping invalid activity doc ${doc.id}: missing required fields")
                            invalidCount++
                            continue
                        }
                        
                        val activity = ActivityFeed(
                            id = doc.id,
                            userId = userId,
                            userName = userName,
                            activityType = activityType,
                            activityTitle = doc.getString("activityTitle") ?: "",
                            points = doc.getLong("points") ?: 0,
                            metadata = doc.get("metadata") as? Map<String, Any> ?: emptyMap(),
                            timestamp = timestamp
                        )
                        activities.add(activity)
                        validCount++
                        android.util.Log.d("LiveActivity", "Added activity: ${activity.userName} - ${activity.activityType}")
                    } catch (e: Exception) {
                        android.util.Log.e("LiveActivity", "Error parsing activity doc ${doc.id}", e)
                        invalidCount++
                    }
                }

                android.util.Log.d("LiveActivity", "Loaded $validCount valid activities, skipped $invalidCount invalid")

                binding.progressBar.visibility = View.GONE
                if (activities.isEmpty()) {
                    android.util.Log.d("LiveActivity", "No activities to display")
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.recyclerViewActivities.visibility = View.GONE
                } else {
                    android.util.Log.d("LiveActivity", "Displaying ${activities.size} activities")
                    binding.tvEmptyState.visibility = View.GONE
                    binding.recyclerViewActivities.visibility = View.VISIBLE
                    activityAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("LiveActivity", "❌ Failed to load activities", e)
                
                binding.swipeRefreshLayout.isRefreshing = false
                
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.recyclerViewActivities.visibility = View.GONE
            }
    }

    companion object {
        fun getRelativeTime(timestamp: com.google.firebase.Timestamp): String {
            val now = System.currentTimeMillis()
            val time = timestamp.toDate().time
            val diff = now - time

            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} minutes ago"
                diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
                diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
                else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp.toDate())
            }
        }
    }
}
