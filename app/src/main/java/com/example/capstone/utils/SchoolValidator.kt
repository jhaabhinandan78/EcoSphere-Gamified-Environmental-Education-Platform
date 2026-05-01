package com.example.capstone.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object SchoolValidator {
    
    /**
     * Validates that the current user has a valid schoolId
     * @param onResult Callback with (isValid, schoolId or error message)
     */
    fun validateUserSchool(
        onResult: (isValid: Boolean, schoolIdOrError: String) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (userId == null) {
            onResult(false, "User not authenticated")
            return
        }
        
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val schoolId = document.getString("schoolId") ?: ""
                val role = document.getString("role") ?: ""
                
                when {
                    role == "admin" -> onResult(true, "admin")
                    schoolId.isEmpty() -> onResult(false, "No school associated with account")
                    else -> onResult(true, schoolId)
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "Failed to validate school: ${e.message}")
            }
    }
    
    /**
     * Checks if user is admin
     * @param onResult Callback with boolean indicating admin status
     */
    fun isAdmin(onResult: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (userId == null) {
            onResult(false)
            return
        }
        
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: ""
                onResult(role == "admin")
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}
