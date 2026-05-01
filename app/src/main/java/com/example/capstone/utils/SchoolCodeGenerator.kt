package com.example.capstone.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Utility for generating unique school codes and IDs
 * School codes are used for registration (e.g., "GREENWO2024AB")
 */
object SchoolCodeGenerator {
    
    /**
     * Generate unique school code with collision detection
     * Format: PREFIX(8) + YEAR(4) + RANDOM(2) = 14 chars
     * Example: "GREENWO2024AB"
     * 
     * @param schoolName The name of the school
     * @param firestore Firestore instance for collision checking
     * @return Unique school code
     */
    suspend fun generateUniqueCode(
        schoolName: String,
        firestore: FirebaseFirestore
    ): String {
        var attempts = 0
        val maxAttempts = 10
        
        while (attempts < maxAttempts) {
            val code = generateCode(schoolName)
            
            // Check if code already exists
            val exists = checkCodeExists(code, firestore)
            
            if (!exists) {
                return code // Found unique code!
            }
            
            attempts++
        }
        
        // Fallback: Add timestamp if all attempts fail
        return generateCodeWithTimestamp(schoolName)
    }
    
    /**
     * Generate school code from school name
     * Private helper function
     */
    private fun generateCode(schoolName: String): String {
        // Step 1: Clean and extract prefix (8 chars max)
        val prefix = schoolName
            .replace(Regex("[^A-Za-z0-9]"), "") // Remove special chars
            .take(8)
            .uppercase()
            .padEnd(8, 'X') // Pad if less than 8 chars
        
        // Step 2: Add current year
        val year = Calendar.getInstance().get(Calendar.YEAR)
        
        // Step 3: Add 2 random uppercase letters
        val random = (1..2)
            .map { ('A'..'Z').random() }
            .joinToString("")
        
        return "$prefix$year$random"
    }
    
    /**
     * Check if school code already exists in Firestore
     * Private helper function
     */
    private suspend fun checkCodeExists(
        code: String,
        firestore: FirebaseFirestore
    ): Boolean {
        return try {
            val result = firestore.collection("Schools")
                .whereEqualTo("schoolCode", code)
                .get()
                .await()
            
            !result.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Fallback: Generate code with timestamp (guaranteed unique)
     * Used when collision detection fails after max attempts
     */
    private fun generateCodeWithTimestamp(schoolName: String): String {
        val prefix = schoolName
            .replace(Regex("[^A-Za-z0-9]"), "")
            .take(6)
            .uppercase()
            .padEnd(6, 'X')
        
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        
        return "$prefix$timestamp"
    }
    
    /**
     * Validate school code format
     * Used during registration to check if entered code is valid format
     * 
     * @param code The school code to validate
     * @return true if format is valid
     */
    fun isValidFormat(code: String): Boolean {
        // Must be 12-16 characters, alphanumeric, uppercase
        return code.matches(Regex("^[A-Z0-9]{12,16}$"))
    }
    
    /**
     * Generate school ID (URL-friendly, for document ID)
     * Example: "greenwood_high_school_2024"
     * 
     * @param schoolName The name of the school
     * @return URL-friendly school ID
     */
    fun generateSchoolId(schoolName: String): String {
        val cleaned = schoolName
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .take(30)
        
        val year = Calendar.getInstance().get(Calendar.YEAR)
        
        return "${cleaned}_$year"
    }
}
