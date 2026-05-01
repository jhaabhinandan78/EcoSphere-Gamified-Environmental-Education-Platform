package com.example.capstone.models

/**
 * User data model with multi-tenancy support
 * Represents both students and teachers in the system
 */
data class User(
    // ═══════════════════════════════════════════════════════════════════════
    // EXISTING FIELDS (DO NOT CHANGE)
    // ═══════════════════════════════════════════════════════════════════════
    val uid: String = "",                       // Firebase Auth UID
    val name: String = "",                      // Full name
    val email: String = "",                     // Email address
    val role: String = "",                      // "student" or "teacher"
    val phone: String = "",                     // Phone number
    val dob: String = "",                       // Date of birth (DD/MM/YYYY)
    val gender: String = "",                    // "Male", "Female", "Other"
    val profilePictureUrl: String = "",         // Firebase Storage URL
    val ecoPoints: Long = 0,                    // Gamification points
    
    // ═══════════════════════════════════════════════════════════════════════
    // NEW MULTI-TENANCY FIELDS
    // ═══════════════════════════════════════════════════════════════════════
    
    // Common fields (both students and teachers)
    val schoolId: String? = null,               // "greenwood_high_2024"
    val schoolCode: String? = null,             // "GREENWO2024AB"
    
    // Teacher-specific fields
    val isLeadTeacher: Boolean? = null,         // true if first teacher in school
    val isApproved: Boolean? = null,            // true if approved by lead teacher
    val assignedBatches: List<String>? = null,  // ["batch_id_1", "batch_id_2"]
    
    // Student-specific fields
    val batchId: String? = null,                // "batch_grade10a_xyz"
    val teacherId: String? = null               // Teacher's UID (from batch)
)
