package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.databinding.ActivityLeaderboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var adapter: LeaderboardAdapter
    private var listener: ListenerRegistration? = null
    private var currentUserSchoolId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        adapter = LeaderboardAdapter(emptyList())
        binding.recyclerLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.recyclerLeaderboard.adapter = adapter

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showLoading(true)

        db.collection("Users")
            .document(currentUid)
            .get()
            .addOnSuccessListener { document ->
                currentUserSchoolId = document.getString("schoolId") ?: ""

                if (currentUserSchoolId.isEmpty()) {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Your account is not associated with a school",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@addOnSuccessListener
                }

                android.util.Log.d("Leaderboard", "User profile loaded: schoolId=$currentUserSchoolId")
                startRealtimeLeaderboard()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                android.util.Log.e("Leaderboard", "Failed to load user profile", e)
                Toast.makeText(
                    this,
                    "Failed to load user profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
    }

    private fun startRealtimeLeaderboard() {
        val currentUid = auth.currentUser?.uid

        // Show loading state
        showLoading(true)

        listener = db.collection("Users")
            .whereEqualTo("role", "student")  // Only show students in leaderboard
            .whereEqualTo("schoolId", currentUserSchoolId)  // Only students from same school
            .orderBy("ecoPoints", Query.Direction.DESCENDING)
            .limit(100) // enough for rank in class demo
            .addSnapshotListener { snapshots, error ->

                showLoading(false)

                if (error != null) {
                    android.util.Log.e("Leaderboard", "Error loading leaderboard", error)
                    
                    val errorMsg = when {
                        error.message?.contains("index") == true ->
                            "Database index required. Check Logcat for index creation link."
                        error.message?.contains("PERMISSION_DENIED") == true || 
                        error.message?.contains("permission") == true ->
                            "Permission denied. Check Firestore security rules."
                        error.message?.contains("network") == true || 
                        error.message?.contains("UNAVAILABLE") == true ->
                            "Network error. Check your internet connection."
                        else ->
                            "Error: ${error.message}"
                    }
                    
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    showEmptyState(true)
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    android.util.Log.w("Leaderboard", "Snapshots is null")
                    showEmptyState(true)
                    return@addSnapshotListener
                }

                android.util.Log.d("Leaderboard", "Loaded ${snapshots.size()} users")

                val list = mutableListOf<LeaderboardUser>()
                for (doc in snapshots.documents) {
                    try {
                        val name = doc.getString("name") ?: ""
                        val points = doc.getLong("ecoPoints") ?: 0
                        list.add(LeaderboardUser(uid = doc.id, name = name, ecoPoints = points))
                    } catch (e: Exception) {
                        android.util.Log.e("Leaderboard", "Error parsing user ${doc.id}", e)
                    }
                }

                if (list.isEmpty()) {
                    showEmptyState(true)
                } else {
                    showEmptyState(false)
                    // ✅ Update RecyclerView
                    adapter.setData(list)
                }

                // ✅ Update "Your Rank" Card
                if (currentUid != null) {
                    val index = list.indexOfFirst { it.uid == currentUid }
                    if (index != -1) {
                        val you = list[index]
                        binding.tvYourRank.text = "#${index + 1}"
                        binding.tvYourPoints.text = "${you.ecoPoints}"
                        binding.tvYourName.text = if (you.name.isBlank()) "You" else you.name
                    } else {
                        binding.tvYourRank.text = "#-"
                        binding.tvYourPoints.text = "0"
                        binding.tvYourName.text = "Complete modules to rank!"
                    }
                } else {
                    android.util.Log.w("Leaderboard", "No current user")
                    binding.tvYourRank.text = "#-"
                    binding.tvYourPoints.text = "0"
                    binding.tvYourName.text = "Login to see your rank"
                }
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerLeaderboard.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerLeaderboard.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}