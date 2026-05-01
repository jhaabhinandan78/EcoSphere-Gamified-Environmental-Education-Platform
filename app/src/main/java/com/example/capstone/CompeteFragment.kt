package com.example.capstone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.capstone.databinding.FragmentCompeteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CompeteFragment : Fragment(), NavigationAware {

    private var _binding: FragmentCompeteBinding? = null
    private val binding get() = _binding!!
    private var navigationController: NavigationController? = null
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    override fun setNavigationController(controller: NavigationController) {
        navigationController = controller
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompeteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        loadCompeteData()
    }

    override fun onResume() {
        super.onResume()
        loadCompeteData()
    }

    private fun loadCompeteData() {
        val userId = auth.currentUser?.uid ?: return
        
        // Load user data to get schoolId and points — single fetch, pass schoolId downstream
        firestore.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (_binding == null) return@addOnSuccessListener
                if (document.exists()) {
                    val schoolId = document.getString("schoolId") ?: ""
                    val ecoPoints = document.getLong("ecoPoints")?.toInt() ?: 0
                    
                    // Update total points display
                    binding.tvTotalPoints.text = ecoPoints.toString()
                    
                    // Load school-based rank
                    if (schoolId.isNotEmpty()) {
                        loadSchoolRank(userId, schoolId)
                        // Pass schoolId directly — avoids a second Firestore fetch
                        loadChallengeStats(userId, schoolId)
                    } else {
                        binding.tvRankNumber.text = "#--"
                        binding.tvTotalStudents.text = "No school assigned"
                        binding.tvChallengesCompleted.text = "0/0"
                        binding.tvChallengeCount.text = "No school assigned"
                    }
                }
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                binding.tvRankNumber.text = "#--"
                binding.tvTotalStudents.text = "Unable to load"
            }
    }

    private fun loadSchoolRank(userId: String, schoolId: String) {
        // Get user's rank within their school
        firestore.collection("Users")
            .whereEqualTo("schoolId", schoolId)
            .whereEqualTo("role", "student")
            .orderBy("ecoPoints", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val totalStudents = documents.size()
                val rank = documents.indexOfFirst { it.id == userId } + 1
                
                if (rank > 0) {
                    binding.tvRankNumber.text = "#$rank"
                    binding.tvTotalStudents.text = "out of $totalStudents students"
                    
                    // Calculate rank change (placeholder for now)
                    // In a real app, you'd compare with previous rank stored in user data
                    binding.tvRankChange.text = "Keep climbing! 💪"
                }
            }
            .addOnFailureListener { e ->
                // Handle error silently
                binding.tvRankNumber.text = "#--"
                binding.tvTotalStudents.text = "Loading..."
            }
    }

    private fun loadChallengeStats(userId: String, schoolId: String) {
        if (_binding == null) return

        if (schoolId.isEmpty()) {
            binding.tvChallengesCompleted.text = "0/0"
            binding.tvChallengeCount.text = "No school assigned"
            return
        }

        // Get total challenges for this school
        firestore.collection("Challenges")
            .whereEqualTo("schoolId", schoolId)
            .get()
            .addOnSuccessListener { allChallenges ->
                if (_binding == null) return@addOnSuccessListener
                val totalChallenges = allChallenges.size()
                val schoolChallengeIds = allChallenges.documents.map { it.id }.toSet()

                // Read from top-level ChallengeSubmissions collection (where submissions are actually stored)
                firestore.collection("ChallengeSubmissions")
                    .whereEqualTo("studentId", userId)
                    .whereEqualTo("status", "approved")
                    .get()
                    .addOnSuccessListener { submissions ->
                        if (_binding == null) return@addOnSuccessListener
                        // Only count approved submissions for this school's challenges
                        val completedCount = submissions.documents
                            .map { it.getString("challengeId") ?: it.id }
                            .count { it in schoolChallengeIds }

                        binding.tvChallengesCompleted.text = "$completedCount/$totalChallenges"

                        val availableChallenges = totalChallenges - completedCount
                        binding.tvChallengeCount.text = when {
                            totalChallenges == 0 -> "No challenges yet"
                            availableChallenges > 0 -> "$availableChallenges challenges available"
                            else -> "All challenges completed! 🎉"
                        }
                    }
                    .addOnFailureListener {
                        if (_binding == null) return@addOnFailureListener
                        binding.tvChallengesCompleted.text = "0/$totalChallenges"
                        binding.tvChallengeCount.text = "$totalChallenges challenges available"
                    }
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                binding.tvChallengesCompleted.text = "0/0"
                binding.tvChallengeCount.text = "Unable to load challenges"
            }
    }

    private fun setupClickListeners() {
        binding.cardProgress.setOnClickListener {
            navigationController?.navigateToActivity(ProgressTrackerActivity::class.java)
        }

        binding.cardChallenges.setOnClickListener {
            navigationController?.navigateToActivity(StudentChallengesActivity::class.java)
        }

        binding.cardLeaderboard.setOnClickListener {
            navigationController?.navigateToActivity(LeaderboardActivity::class.java)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
