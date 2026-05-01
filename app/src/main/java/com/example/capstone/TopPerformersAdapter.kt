package com.example.capstone

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ItemTopPerformerBinding
import com.google.firebase.firestore.FirebaseFirestore

class TopPerformersAdapter(
    private val performers: List<StudentData>
) : RecyclerView.Adapter<TopPerformersAdapter.PerformerViewHolder>() {

    inner class PerformerViewHolder(private val binding: ItemTopPerformerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(student: StudentData, position: Int) {
            // Set rank medal
            binding.tvRank.text = when (position) {
                0 -> "🥇"
                1 -> "🥈"
                2 -> "🥉"
                else -> "${position + 1}."
            }

            binding.tvStudentName.text = student.name
            binding.tvEcoPoints.text = "${student.ecoPoints} pts"

            // Show level info in stats using LevelCalculator — will be overwritten once real stats load
            val levelInfo = com.example.capstone.utils.LevelCalculator.getLevelInfo(student.ecoPoints.toInt())
            binding.tvStats.text = "${levelInfo.emoji} Lvl ${levelInfo.level} · ${levelInfo.title}\nLoading stats..."

            // Highlight top 3 with medal border colors
            if (position < 3) {
                binding.cardPerformer.strokeWidth = 4
                binding.cardPerformer.strokeColor = when (position) {
                    0 -> 0xFFFFD700.toInt() // Gold
                    1 -> 0xFFC0C0C0.toInt() // Silver
                    2 -> 0xFFCD7F32.toInt() // Bronze
                    else -> 0xFFE0E0E0.toInt()
                }
            } else {
                binding.cardPerformer.strokeWidth = 1
                binding.cardPerformer.strokeColor = 0xFFE0E0E0.toInt()
            }

            // Load real statistics from Firestore
            // Pass the binding tag to detect if view has been recycled
            val currentTag = student.uid
            binding.root.tag = currentTag

            loadStudentStats(student.uid) { modulesCompleted, challengesApproved, avgQuizScore ->
                // Only update if this ViewHolder still shows the same student (not recycled)
                if (binding.root.tag == currentTag) {
                    val li = com.example.capstone.utils.LevelCalculator.getLevelInfo(student.ecoPoints.toInt())
                    binding.tvStats.text = buildString {
                        append("${li.emoji} Lvl ${li.level} · ${li.title}\n")
                        append("📚 $modulesCompleted modules completed\n")
                        append("✅ $challengesApproved challenges approved\n")
                        if (avgQuizScore > 0) {
                            append("📝 $avgQuizScore% avg quiz score")
                        } else {
                            append("📝 No quizzes taken yet")
                        }
                    }
                }
            }
        }

        private fun loadStudentStats(
            userId: String,
            callback: (modulesCompleted: Int, challengesApproved: Int, avgQuizScore: Int) -> Unit
        ) {
            val firestore = FirebaseFirestore.getInstance()

            // Count completed modules from subcollection
            firestore.collection("Users").document(userId)
                .collection("completions")
                .get()
                .addOnSuccessListener { completions ->
                    val modulesCompleted = completions.size()
                    android.util.Log.d("TopPerformers", "User $userId: $modulesCompleted modules completed")

                    // Count approved challenges from top-level ChallengeSubmissions collection
                    firestore.collection("ChallengeSubmissions")
                        .whereEqualTo("studentId", userId)
                        .whereEqualTo("status", "approved")
                        .get()
                        .addOnSuccessListener { challenges ->
                            val challengesApproved = challenges.size()
                            android.util.Log.d("TopPerformers", "User $userId: $challengesApproved challenges approved")

                            // Calculate average quiz score from subcollection
                            firestore.collection("Users").document(userId)
                                .collection("quizAttempts")
                                .get()
                                .addOnSuccessListener { quizzes ->
                                    var totalScore = 0
                                    var quizCount = 0

                                    for (quiz in quizzes) {
                                        val percentage = quiz.getLong("percentage")?.toInt() ?: 0
                                        if (percentage > 0) {
                                            totalScore += percentage
                                            quizCount++
                                        }
                                    }

                                    val avgQuizScore = if (quizCount > 0) totalScore / quizCount else 0
                                    android.util.Log.d("TopPerformers", "User $userId: avg quiz score $avgQuizScore%")

                                    callback(modulesCompleted, challengesApproved, avgQuizScore)
                                }
                                .addOnFailureListener { e ->
                                    android.util.Log.e("TopPerformers", "Failed to load quiz attempts for $userId", e)
                                    callback(modulesCompleted, challengesApproved, 0)
                                }
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("TopPerformers", "Failed to load challenges for $userId", e)
                            callback(modulesCompleted, 0, 0)
                        }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("TopPerformers", "Failed to load completions for $userId", e)
                    callback(0, 0, 0)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerformerViewHolder {
        val binding = ItemTopPerformerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PerformerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PerformerViewHolder, position: Int) {
        holder.bind(performers[position], position)
    }

    // Clear tag when view is recycled to prevent stale callbacks updating wrong item
    override fun onViewRecycled(holder: PerformerViewHolder) {
        super.onViewRecycled(holder)
        holder.itemView.tag = null
    }

    override fun getItemCount() = performers.size
}
