package com.example.capstone

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.databinding.ActivityMySubmissionsBinding
import com.example.capstone.models.ChallengeSubmission
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MySubmissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMySubmissionsBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var submissionsAdapter: MySubmissionsAdapter
    private val submissions = mutableListOf<ChallengeSubmission>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySubmissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadMySubmissions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        submissionsAdapter = MySubmissionsAdapter(submissions)
        binding.recyclerViewSubmissions.apply {
            layoutManager = LinearLayoutManager(this@MySubmissionsActivity)
            adapter = submissionsAdapter
        }
    }

    private fun loadMySubmissions() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            android.widget.Toast.makeText(this, "User not logged in", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE

        android.util.Log.d("MySubmissions", "Loading submissions for user: $userId")

        firestore.collection("ChallengeSubmissions")
            .whereEqualTo("studentId", userId)
            .orderBy("submittedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                android.util.Log.d("MySubmissions", "✅ Query successful! Found ${documents.size()} submissions")
                
                submissions.clear()
                for (doc in documents) {
                    try {
                        val submission = ChallengeSubmission(
                            id = doc.id,
                            challengeId = doc.getString("challengeId") ?: "",
                            challengeTitle = doc.getString("challengeTitle") ?: "",
                            challengePoints = doc.getLong("challengePoints") ?: 0,
                            studentId = doc.getString("studentId") ?: "",
                            studentName = doc.getString("studentName") ?: "",
                            submittedAt = doc.getTimestamp("submittedAt") ?: Timestamp.now(),
                            status = doc.getString("status") ?: ChallengeSubmission.STATUS_PENDING,
                            photoUrl = doc.getString("photoUrl") ?: "",
                            reviewedBy = doc.getString("reviewedBy") ?: "",
                            reviewedAt = doc.getTimestamp("reviewedAt"),
                            adminFeedback = doc.getString("adminFeedback") ?: ""
                        )
                        submissions.add(submission)
                    } catch (e: Exception) {
                        android.util.Log.e("MySubmissions", "Error parsing submission", e)
                    }
                }

                binding.progressBar.visibility = View.GONE
                
                // Update stats
                val pending = submissions.count { it.status == ChallengeSubmission.STATUS_PENDING }
                val approved = submissions.count { it.status == ChallengeSubmission.STATUS_APPROVED }
                val rejected = submissions.count { it.status == ChallengeSubmission.STATUS_REJECTED }
                
                binding.tvPendingCount.text = pending.toString()
                binding.tvApprovedCount.text = approved.toString()
                binding.tvRejectedCount.text = rejected.toString()
                
                if (submissions.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.recyclerViewSubmissions.visibility = View.GONE
                    binding.statsLayout.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.recyclerViewSubmissions.visibility = View.VISIBLE
                    binding.statsLayout.visibility = View.VISIBLE
                    submissionsAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("MySubmissions", "❌ Failed to load submissions", e)
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.recyclerViewSubmissions.visibility = View.GONE
                binding.statsLayout.visibility = View.GONE
            }
    }
}
