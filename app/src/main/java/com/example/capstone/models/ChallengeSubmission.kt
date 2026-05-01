package com.example.capstone.models

import com.google.firebase.Timestamp

data class ChallengeSubmission(
    val id: String = "",
    val challengeId: String = "",
    val challengeTitle: String = "",
    val challengePoints: Long = 0,
    val studentId: String = "",
    val studentName: String = "",
    val submittedAt: Timestamp = Timestamp.now(),
    val status: String = STATUS_PENDING, // "pending", "approved", "rejected"
    val photoUrl: String = "",
    val reviewedBy: String = "",
    val reviewedAt: Timestamp? = null,
    val adminFeedback: String = "" // Feedback when rejected
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_APPROVED = "approved"
        const val STATUS_REJECTED = "rejected"
    }
    
    fun getStatusColor(): String {
        return when (status) {
            STATUS_PENDING -> "#FF9800" // Orange
            STATUS_APPROVED -> "#4CAF50" // Green
            STATUS_REJECTED -> "#F44336" // Red
            else -> "#9E9E9E" // Gray
        }
    }
    
    fun getStatusText(): String {
        return when (status) {
            STATUS_PENDING -> "Pending Review"
            STATUS_APPROVED -> "Approved"
            STATUS_REJECTED -> "Rejected"
            else -> "Unknown"
        }
    }
}
