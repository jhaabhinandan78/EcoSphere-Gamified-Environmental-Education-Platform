package com.example.capstone.models

import com.google.firebase.Timestamp

data class Announcement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val readBy: List<String> = emptyList(), // List of student IDs who have read this
    val schoolId: String = "" // School ID for multi-tenancy filtering
) {
    fun isReadBy(studentId: String): Boolean {
        return readBy.contains(studentId)
    }
}
