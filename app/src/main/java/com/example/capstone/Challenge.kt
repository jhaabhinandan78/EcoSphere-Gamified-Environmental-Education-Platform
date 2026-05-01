package com.example.capstone

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val points: Long = 0,
    val type: String = "one-time",
    val active: Boolean = true,
    val schoolId: String = "",

    // Submission status for the current student: null, "pending", "approved", "rejected"
    var submissionStatus: String? = null,

    // Keep for backward compatibility
    var isCompleted: Boolean = false
)
