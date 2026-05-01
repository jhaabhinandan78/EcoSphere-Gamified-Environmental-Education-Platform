package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.databinding.ActivityStudentChallengesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentChallengesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentChallengesBinding
    private val db = FirebaseFirestore.getInstance()
    private val list = mutableListOf<Challenge>()
    private var currentUserSchoolId: String = ""

    override fun onResume() {
        super.onResume()
        // Refresh challenge list when returning (e.g. after submitting a challenge)
        if (currentUserSchoolId.isNotEmpty()) {
            loadChallenges()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentChallengesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.recyclerChallenges.layoutManager = LinearLayoutManager(this)

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                currentUserSchoolId = document.getString("schoolId") ?: ""

                if (currentUserSchoolId.isEmpty()) {
                    Toast.makeText(this, "Your account is not associated with a school", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                loadChallenges()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load user profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadChallenges() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("Challenges")
            .whereEqualTo("schoolId", currentUserSchoolId)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snap ->
                val tempList = mutableListOf<Challenge>()
                for (doc in snap.documents) {
                    try {
                        val c = doc.toObject(Challenge::class.java) ?: continue
                        tempList.add(c.copy(id = doc.id))
                    } catch (e: Exception) {
                        android.util.Log.e("StudentChallenges", "Error parsing challenge ${doc.id}", e)
                    }
                }

                db.collection("ChallengeSubmissions")
                    .whereEqualTo("studentId", uid)
                    .get()
                    .addOnSuccessListener { submissionSnap ->
                        val submissionStatusMap = mutableMapOf<String, String>()
                        for (doc in submissionSnap.documents) {
                            val challengeId = doc.getString("challengeId") ?: continue
                            val status = doc.getString("status") ?: "pending"
                            val existing = submissionStatusMap[challengeId]
                            if (existing == null || status == "approved" ||
                                (status == "pending" && existing == "rejected")) {
                                submissionStatusMap[challengeId] = status
                            }
                        }

                        for (challenge in tempList) {
                            val status = submissionStatusMap[challenge.id]
                            challenge.submissionStatus = status
                            challenge.isCompleted = (status == "approved")
                        }

                        list.clear()
                        list.addAll(tempList)

                        if (list.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                            binding.recyclerChallenges.visibility = View.GONE
                        } else {
                            binding.emptyState.visibility = View.GONE
                            binding.recyclerChallenges.visibility = View.VISIBLE
                        }

                        binding.recyclerChallenges.adapter = ChallengeAdapter(list) { selected ->
                            val i = Intent(this, ChallengeDetailActivity::class.java)
                            i.putExtra("challengeId", selected.id)
                            i.putExtra("title", selected.title)
                            i.putExtra("description", selected.description)
                            i.putExtra("points", selected.points)
                            startActivity(i)
                        }
                    }
                    .addOnFailureListener {
                        list.clear()
                        list.addAll(tempList)
                        if (list.isEmpty()) {
                            binding.emptyState.visibility = View.VISIBLE
                            binding.recyclerChallenges.visibility = View.GONE
                        } else {
                            binding.emptyState.visibility = View.GONE
                            binding.recyclerChallenges.visibility = View.VISIBLE
                        }
                        binding.recyclerChallenges.adapter = ChallengeAdapter(list) { selected ->
                            val i = Intent(this, ChallengeDetailActivity::class.java)
                            i.putExtra("challengeId", selected.id)
                            i.putExtra("title", selected.title)
                            i.putExtra("description", selected.description)
                            i.putExtra("points", selected.points)
                            startActivity(i)
                        }
                    }
            }
            .addOnFailureListener { e ->
                val msg = when {
                    e.message?.contains("PERMISSION_DENIED") == true -> "Permission denied. Check Firestore rules."
                    e.message?.contains("network") == true || e.message?.contains("UNAVAILABLE") == true -> "Network error. Check your connection."
                    else -> "Failed to load challenges: ${e.message}"
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerChallenges.visibility = View.GONE
            }
    }
}
