package com.example.capstone.utils

import com.example.capstone.models.ActivityFeed
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ActivityLogger {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Log when a student completes a module
     */
    fun logModuleCompleted(moduleTitle: String, points: Long) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("ActivityLogger", "Cannot log module - user not authenticated")
            return
        }
        
        android.util.Log.d("ActivityLogger", "Logging module completion: $moduleTitle for user ${currentUser.uid}")
        
        getUserInfo(currentUser.uid) { userName, schoolId, batchId ->
            val activityData = hashMapOf(
                "userId" to currentUser.uid,
                "userName" to userName,
                "activityType" to ActivityFeed.TYPE_MODULE_COMPLETED,
                "activityTitle" to moduleTitle,
                "points" to points,
                "metadata" to emptyMap<String, Any>(),
                "timestamp" to Timestamp.now(),
                "schoolId" to (schoolId ?: ""),   // NEW: for school-level filtering
                "batchId" to (batchId ?: "")       // NEW: for batch-level filtering
            )
            
            android.util.Log.d("ActivityLogger", "Saving activity to Firestore: $activityData")
            
            firestore.collection("ActivityFeed")
                .add(activityData)
                .addOnSuccessListener { docRef ->
                    android.util.Log.d("ActivityLogger", "✅ Module completion logged successfully! Doc ID: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("ActivityLogger", "❌ Failed to log module completion", e)
                }
        }
    }
    
    /**
     * Log when a student completes a quiz
     */
    fun logQuizCompleted(quizTitle: String, score: Int, points: Long = 0) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("ActivityLogger", "Cannot log quiz - user not authenticated")
            return
        }
        
        android.util.Log.d("ActivityLogger", "Logging quiz completion: $quizTitle (score: $score) for user ${currentUser.uid}")
        
        getUserInfo(currentUser.uid) { userName, schoolId, batchId ->
            val activityData = hashMapOf(
                "userId" to currentUser.uid,
                "userName" to userName,
                "activityType" to ActivityFeed.TYPE_QUIZ_COMPLETED,
                "activityTitle" to quizTitle,
                "points" to points,
                "metadata" to mapOf("score" to score.toLong()),
                "timestamp" to Timestamp.now(),
                "schoolId" to (schoolId ?: ""),   // NEW
                "batchId" to (batchId ?: "")       // NEW
            )
            
            android.util.Log.d("ActivityLogger", "Saving activity to Firestore: $activityData")
            
            firestore.collection("ActivityFeed")
                .add(activityData)
                .addOnSuccessListener { docRef ->
                    android.util.Log.d("ActivityLogger", "✅ Quiz completion logged successfully! Doc ID: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("ActivityLogger", "❌ Failed to log quiz completion", e)
                }
        }
    }
    
    /**
     * Log when a student submits a challenge
     */
    fun logChallengeSubmitted(challengeTitle: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("ActivityLogger", "Cannot log challenge - user not authenticated")
            return
        }
        
        android.util.Log.d("ActivityLogger", "Logging challenge submission: $challengeTitle for user ${currentUser.uid}")
        
        getUserInfo(currentUser.uid) { userName, schoolId, batchId ->
            val activityData = hashMapOf(
                "userId" to currentUser.uid,
                "userName" to userName,
                "activityType" to ActivityFeed.TYPE_CHALLENGE_SUBMITTED,
                "activityTitle" to challengeTitle,
                "points" to 0L,
                "metadata" to emptyMap<String, Any>(),
                "timestamp" to Timestamp.now(),
                "schoolId" to (schoolId ?: ""),   // NEW
                "batchId" to (batchId ?: "")       // NEW
            )
            
            android.util.Log.d("ActivityLogger", "Saving activity to Firestore: $activityData")
            
            firestore.collection("ActivityFeed")
                .add(activityData)
                .addOnSuccessListener { docRef ->
                    android.util.Log.d("ActivityLogger", "✅ Challenge submission logged successfully! Doc ID: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("ActivityLogger", "❌ Failed to log challenge submission", e)
                }
        }
    }
    
    /**
     * Log when a student earns a badge or reaches a milestone
     */
    fun logBadgeEarned(badgeName: String, points: Long) {
        val currentUser = auth.currentUser ?: return
        
        getUserInfo(currentUser.uid) { userName, schoolId, batchId ->
            val activityData = hashMapOf(
                "userId" to currentUser.uid,
                "userName" to userName,
                "activityType" to ActivityFeed.TYPE_BADGE_EARNED,
                "activityTitle" to "",
                "points" to points,
                "metadata" to mapOf("badgeName" to badgeName),
                "timestamp" to Timestamp.now(),
                "schoolId" to (schoolId ?: ""),   // NEW
                "batchId" to (batchId ?: "")       // NEW
            )
            
            firestore.collection("ActivityFeed")
                .add(activityData)
                .addOnFailureListener { e ->
                    android.util.Log.e("ActivityLogger", "Failed to log badge earned", e)
                }
        }
    }
    
    /**
     * Log when a student starts a module (optional)
     */
    fun logModuleStarted(moduleTitle: String) {
        val currentUser = auth.currentUser ?: return
        
        getUserInfo(currentUser.uid) { userName, schoolId, batchId ->
            val activityData = hashMapOf(
                "userId" to currentUser.uid,
                "userName" to userName,
                "activityType" to ActivityFeed.TYPE_MODULE_STARTED,
                "activityTitle" to moduleTitle,
                "points" to 0L,
                "metadata" to emptyMap<String, Any>(),
                "timestamp" to Timestamp.now(),
                "schoolId" to (schoolId ?: ""),   // NEW
                "batchId" to (batchId ?: "")       // NEW
            )
            
            firestore.collection("ActivityFeed")
                .add(activityData)
                .addOnFailureListener { e ->
                    android.util.Log.e("ActivityLogger", "Failed to log module started", e)
                }
        }
    }
    
    /**
     * Helper: fetch user name, schoolId, and batchId in one read
     */
    private fun getUserInfo(
        userId: String,
        callback: (name: String, schoolId: String?, batchId: String?) -> Unit
    ) {
        firestore.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: "Unknown User"
                val schoolId = document.getString("schoolId")
                val batchId = document.getString("batchId")
                callback(name, schoolId, batchId)
            }
            .addOnFailureListener {
                callback("Unknown User", null, null)
            }
    }
}
