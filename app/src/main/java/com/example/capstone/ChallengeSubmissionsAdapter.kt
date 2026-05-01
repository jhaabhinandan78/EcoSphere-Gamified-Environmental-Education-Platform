package com.example.capstone

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ItemChallengeSubmissionBinding
import com.example.capstone.models.ChallengeSubmission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChallengeSubmissionsAdapter(
    private val submissions: List<ChallengeSubmission>,
    private val onApproveClick: (ChallengeSubmission) -> Unit,
    private val onRejectClick: (ChallengeSubmission) -> Unit
) : RecyclerView.Adapter<ChallengeSubmissionsAdapter.SubmissionViewHolder>() {

    inner class SubmissionViewHolder(private val binding: ItemChallengeSubmissionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(submission: ChallengeSubmission) {
            binding.tvStudentName.text = submission.studentName
            binding.tvChallengeTitle.text = submission.challengeTitle
            binding.tvPoints.text = "${submission.challengePoints} pts"
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            binding.tvSubmittedAt.text = "Submitted: ${dateFormat.format(submission.submittedAt.toDate())}"

            // Set status badge
            binding.tvStatus.text = submission.getStatusText()
            binding.tvStatus.setBackgroundColor(Color.parseColor(submission.getStatusColor()))

            // Load proof photo
            if (submission.photoUrl.isNotEmpty()) {
                loadImageFromUrl(submission.photoUrl, binding.imgProof)
                
                // Click to view full size
                binding.imgProof.setOnClickListener {
                    val context = binding.root.context
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(submission.photoUrl))
                    context.startActivity(intent)
                }
            } else {
                binding.imgProof.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Show/hide action buttons based on status
            if (submission.status == ChallengeSubmission.STATUS_PENDING) {
                binding.btnApprove.visibility = android.view.View.VISIBLE
                binding.btnReject.visibility = android.view.View.VISIBLE
                
                binding.btnApprove.setOnClickListener {
                    onApproveClick(submission)
                }
                
                binding.btnReject.setOnClickListener {
                    onRejectClick(submission)
                }
            } else {
                binding.btnApprove.visibility = android.view.View.GONE
                binding.btnReject.visibility = android.view.View.GONE
            }
        }

        private fun loadImageFromUrl(url: String, imageView: android.widget.ImageView) {
            // Use Firebase Storage to load image
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl(url)
            
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Load image using coroutines
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val connection = java.net.URL(uri.toString()).openConnection()
                        connection.connect()
                        val input = connection.getInputStream()
                        val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                        
                        withContext(Dispatchers.Main) {
                            imageView.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ImageLoad", "Failed to load image", e)
                        withContext(Dispatchers.Main) {
                            imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                        }
                    }
                }
            }.addOnFailureListener { e ->
                android.util.Log.e("ImageLoad", "Failed to get download URL", e)
                imageView.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val binding = ItemChallengeSubmissionBinding.inflate(
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
