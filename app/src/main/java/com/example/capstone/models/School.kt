package com.example.capstone.models

import com.google.firebase.Timestamp

/**
 * School data model for multi-tenancy support
 * Each school is a separate tenant with complete data isolation
 */
data class School(
    val schoolId: String = "",              // "greenwood_high_2024" (document ID)
    val schoolCode: String = "",            // "GREENWO2024AB" (unique registration code)
    val schoolName: String = "",            // "Greenwood High School"
    val contactEmail: String = "",          // "principal@greenwood.edu"
    val contactPhone: String = "",          // Optional phone number
    val address: String = "",               // Optional physical address
    val city: String = "",                  // "New York"
    val state: String = "",                 // "New York" (optional)
    val country: String = "",               // "USA"
    val active: Boolean = true,             // School status (active/inactive) - matches Firebase field
    val createdAt: Timestamp? = null,       // Registration timestamp
    val totalTeachers: Int = 0,             // Counter for analytics
    val totalStudents: Int = 0,             // Counter for analytics
    val totalBatches: Int = 0               // Counter for analytics
)
