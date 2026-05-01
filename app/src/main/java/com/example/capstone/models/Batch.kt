package com.example.capstone.models

import com.google.firebase.Timestamp

/**
 * Batch/Class data model for grouping students
 * Teachers create batches, students select which batch to join
 */
data class Batch(
    val batchId: String = "",               // Auto-generated unique ID
    val batchName: String = "",             // "Grade 10-A" or "Biology 101"
    val schoolId: String = "",              // Reference to parent school
    val teacherId: String = "",             // Creator teacher's UID
    val teacherName: String = "",           // Denormalized for display
    val academicYear: String = "",          // "2024-2025"
    val isActive: Boolean = true,           // Batch status
    val createdAt: Timestamp? = null,       // Creation timestamp
    val studentCount: Int = 0               // Counter for analytics (optional)
)
