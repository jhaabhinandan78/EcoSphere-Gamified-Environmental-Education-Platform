package com.example.capstone

data class Module(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val points: Long = 0,
    val order: Long = 0,
    val schoolId: String = "",
    // Completion status for the current student
    var isCompleted: Boolean = false
)
