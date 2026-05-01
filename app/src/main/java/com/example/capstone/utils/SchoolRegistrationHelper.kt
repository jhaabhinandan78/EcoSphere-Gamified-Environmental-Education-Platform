package com.example.capstone.utils

import com.example.capstone.models.School
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Helper utility for creating schools in the system
 * Used by platform owner to register new schools
 * Can be called from DataSeeder or admin tools
 */
object SchoolRegistrationHelper {
    
    /**
     * Create a new school in the system
     * Auto-generates school code and ID
     * 
     * @param schoolName Name of the school
     * @param contactEmail Primary contact email
     * @param contactPhone Phone number (optional)
     * @param address Physical address (optional)
     * @param city City name
     * @param state State/Province (optional)
     * @param country Country name
     * @param firestore Firestore instance
     * @return Result with created School object or error
     */
    suspend fun createSchool(
        schoolName: String,
        contactEmail: String,
        contactPhone: String = "",
        address: String = "",
        city: String,
        state: String = "",
        country: String,
        firestore: FirebaseFirestore
    ): Result<School> {
        return try {
            // Auto-generate codes
            val schoolCode = SchoolCodeGenerator.generateUniqueCode(schoolName, firestore)
            val schoolId = SchoolCodeGenerator.generateSchoolId(schoolName)
            
            val school = School(
                schoolId = schoolId,
                schoolCode = schoolCode,
                schoolName = schoolName,
                contactEmail = contactEmail,
                contactPhone = contactPhone,
                address = address,
                city = city,
                state = state,
                country = country,
                active = true,  // Changed from isActive to active
                createdAt = Timestamp.now(),
                totalTeachers = 0,
                totalStudents = 0,
                totalBatches = 0
            )
            
            // Save to Firestore
            firestore.collection("Schools")
                .document(schoolId)
                .set(school)
                .await()
            
            println("✅ School created successfully!")
            println("   School ID: $schoolId")
            println("   School Code: $schoolCode")
            println("   Share this code with teachers: $schoolCode")
            
            Result.success(school)
            
        } catch (e: Exception) {
            println("❌ Failed to create school: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Verify if a school code exists and is active
     * Used during registration to validate entered codes
     * 
     * @param schoolCode The code to verify
     * @param firestore Firestore instance
     * @return School object if found and active, null otherwise
     */
    suspend fun verifySchoolCode(
        schoolCode: String,
        firestore: FirebaseFirestore
    ): School? {
        return try {
            println("🔍 Verifying school code: '$schoolCode' (length: ${schoolCode.length})")
            
            // Validate format first
            if (!SchoolCodeGenerator.isValidFormat(schoolCode)) {
                println("❌ Invalid school code format: '$schoolCode'")
                println("   Expected: 12-14 uppercase alphanumeric characters")
                println("   Received: ${schoolCode.length} characters")
                return null
            }
            
            println("✅ School code format is valid")
            
            // Query Firestore
            println("🔍 Querying Firestore for school code: '$schoolCode'")
            val result = firestore.collection("Schools")
                .whereEqualTo("schoolCode", schoolCode)
                .whereEqualTo("active", true)  // Changed from isActive to active
                .get()
                .await()
            
            println("📊 Query result: ${result.size()} documents found")
            
            if (result.isEmpty) {
                println("❌ School code not found or inactive: '$schoolCode'")
                
                // Debug: Check if school exists without active filter
                val allSchools = firestore.collection("Schools")
                    .whereEqualTo("schoolCode", schoolCode)
                    .get()
                    .await()
                
                if (allSchools.isEmpty) {
                    println("❌ School code does not exist at all: '$schoolCode'")
                } else {
                    println("⚠️ School exists but is inactive: '$schoolCode'")
                }
                
                return null
            }
            
            val school = result.documents[0].toObject(School::class.java)
            println("✅ School verified: ${school?.schoolName} (${school?.schoolCode})")
            school
            
        } catch (e: Exception) {
            println("❌ Error verifying school code: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get school by ID
     * 
     * @param schoolId The school document ID
     * @param firestore Firestore instance
     * @return School object if found, null otherwise
     */
    suspend fun getSchoolById(
        schoolId: String,
        firestore: FirebaseFirestore
    ): School? {
        return try {
            val document = firestore.collection("Schools")
                .document(schoolId)
                .get()
                .await()
            
            document.toObject(School::class.java)
        } catch (e: Exception) {
            println("❌ Error fetching school: ${e.message}")
            null
        }
    }
}
