package com.example.capstone.models

import com.google.firebase.Timestamp

data class ActivityFeed(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val activityType: String = "", // "module_completed", "quiz_completed", "challenge_submitted", "badge_earned", "module_started"
    val activityTitle: String = "",
    val points: Long = 0,
    val metadata: Map<String, Any> = emptyMap(), // Additional info like quiz score, badge name
    val timestamp: Timestamp = Timestamp.now()
) {
    companion object {
        const val TYPE_MODULE_COMPLETED = "module_completed"
        const val TYPE_QUIZ_COMPLETED = "quiz_completed"
        const val TYPE_CHALLENGE_SUBMITTED = "challenge_submitted"
        const val TYPE_BADGE_EARNED = "badge_earned"
        const val TYPE_MODULE_STARTED = "module_started"
    }
    
    fun getActivityDescription(): String {
        return when (activityType) {
            TYPE_MODULE_COMPLETED -> "$userName completed \"$activityTitle\" module (+$points pts)"
            TYPE_QUIZ_COMPLETED -> {
                val score = metadata["score"] as? Long ?: 0
                "$userName completed \"$activityTitle\" quiz (Score: $score%)"
            }
            TYPE_CHALLENGE_SUBMITTED -> "$userName submitted \"$activityTitle\" challenge (pending review)"
            TYPE_BADGE_EARNED -> {
                val badgeName = metadata["badgeName"] as? String ?: "badge"
                "$userName earned \"$badgeName\" badge ($points pts milestone)"
            }
            TYPE_MODULE_STARTED -> "$userName started \"$activityTitle\" module"
            else -> "$userName performed an activity"
        }
    }
    
    fun getActivityIcon(): String {
        return when (activityType) {
            TYPE_MODULE_COMPLETED -> "✅"
            TYPE_QUIZ_COMPLETED -> "📝"
            TYPE_CHALLENGE_SUBMITTED -> "📸"
            TYPE_BADGE_EARNED -> "🏆"
            TYPE_MODULE_STARTED -> "📚"
            else -> "•"
        }
    }
}
