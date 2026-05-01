package com.example.capstone

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ItemMySubmissionBinding
import com.example.capstone.models.ChallengeSubmission
import java.text.SimpleDateFormat
import java.util.*

class MySubmissionsAdapter(
    private val submissions: List<ChallengeSubmission>
) : RecyclerView.Adapter<MySubmissionsAdapter.SubmissionViewHolder>() {

    inner class SubmissionViewHolder(private val binding: ItemMySubmissionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(submission: ChallengeSubmission) {
            binding.tvChallengeTitle.text = submission.challengeTitle
            binding.tvPoints.text = "${submission.challengePoints} pts"
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvSubmittedAt.text = "Submitted: ${dateFormat.format(submission.submittedAt.toDate())}"

            // Set status
            binding.tvStatus.text = submission.getStatusText()
            binding.tvStatus.setBackgroundColor(Color.parseColor(submission.getStatusColor()))

            // Show status icon
            binding.tvStatusIcon.text = when (submission.status) {
                ChallengeSubmission.STATUS_PENDING -> "⏳"
                ChallengeSubmission.STATUS_APPROVED -> "✅"
                ChallengeSubmission.STATUS_REJECTED -> "❌"
                else -> "•"
            }

            // Show/hide feedback section
            if (submission.status == ChallengeSubmission.STATUS_REJECTED && submission.adminFeedback.isNotEmpty()) {
                binding.feedbackLayout.visibility = View.VISIBLE
                binding.tvFeedback.text = submission.adminFeedback
            } else if (submission.status == ChallengeSubmission.STATUS_APPROVED) {
                binding.feedbackLayout.visibility = View.VISIBLE
                binding.tvFeedbackLabel.text = "Reviewed by:"
                binding.tvFeedback.text = "${submission.reviewedBy} on ${dateFormat.format(submission.reviewedAt?.toDate() ?: Date())}"
            } else {
                binding.feedbackLayout.visibility = View.GONE
            }

            // View photo button
            binding.btnViewPhoto.setOnClickListener {
                if (submission.photoUrl.isNotEmpty()) {
                    val context = binding.root.context
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(submission.photoUrl))
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val binding = ItemMySubmissionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubmissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(submissions[position])
    }

    override fun getItemCount() = submissions.size
}
