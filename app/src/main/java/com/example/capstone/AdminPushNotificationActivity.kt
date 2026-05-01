package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.databinding.ActivityAdminPushNotificationBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AdminPushNotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPushNotificationBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    
    // NEW: Multi-tenancy fields
    private var teacherSchoolId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPushNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadTeacherInfo()  // NEW: Load teacher info first
        setupClickListeners()
        loadNotificationSettings()
    }
    
    /**
     * NEW: Load teacher information to get school
     */
    private fun loadTeacherInfo() {
        currentUserId = auth.currentUser?.uid
        if (currentUserId == null) return
        
        firestore.collection("Users")
            .document(currentUserId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    teacherSchoolId = document.getString("schoolId")
                    android.util.Log.d("PushNotification", "Teacher school: $teacherSchoolId")
                    updateToolbarSubtitle()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("PushNotification", "Failed to load teacher info", e)
            }
    }
    
    /**
     * NEW: Update toolbar subtitle to show context
     */
    private fun updateToolbarSubtitle() {
        val subtitle = if (teacherSchoolId != null) {
            "To students in your school"
        } else {
            "To all students"
        }
        binding.toolbar.subtitle = subtitle
    }

    private fun setupToolbar() {
        binding.toolbar.title = "📢 Push Notifications"
        binding.toolbar.setTitleTextColor(0xFFFFFFFF.toInt())
        binding.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnSendNotification.setOnClickListener {
            sendPushNotification()
        }

        binding.btnClear.setOnClickListener {
            clearForm()
        }
    }

    private fun loadNotificationSettings() {
        firestore.collection("PlatformSettings")
            .document("config")
            .get()
            .addOnSuccessListener { document ->
                val notifications = document.data?.get("notifications") as? Map<String, Any>
                val enabled = notifications?.get("enablePushNotifications") as? Boolean ?: true

                if (!enabled) {
                    binding.tvWarning.visibility = View.VISIBLE
                    binding.tvWarning.text = "⚠️ Push notifications are currently disabled in Platform Settings"
                } else {
                    binding.tvWarning.visibility = View.GONE
                }
            }
    }

    private fun sendPushNotification() {
        val title = binding.etTitle.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()

        // Validation
        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            binding.etTitle.requestFocus()
            return
        }

        if (title.length < 3) {
            binding.etTitle.error = "Title must be at least 3 characters"
            binding.etTitle.requestFocus()
            return
        }

        if (message.isEmpty()) {
            binding.etMessage.error = "Message is required"
            binding.etMessage.requestFocus()
            return
        }

        if (message.length < 10) {
            binding.etMessage.error = "Message must be at least 10 characters"
            binding.etMessage.requestFocus()
            return
        }

        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSendNotification.isEnabled = false
        binding.btnSendNotification.text = "Sending..."

        android.util.Log.d("PushNotification", "Sending notification: $title")

        // Save notification to Firestore (will be picked up by Cloud Function)
        val notificationData = hashMapOf(
            "title" to title,
            "message" to message,
            "type" to "admin_push",
            "sentBy" to "admin",
            "sentAt" to Timestamp.now(),
            "status" to "pending",
            "targetAudience" to "all_students",
            "schoolId" to (teacherSchoolId ?: "")  // NEW: Add school filter
        )

        firestore.collection("PushNotifications")
            .add(notificationData)
            .addOnSuccessListener { docRef ->
                android.util.Log.d("PushNotification", "✅ Notification created with ID: ${docRef.id}")

                // Immediately update status to "sent" so it appears in the list
                firestore.collection("PushNotifications")
                    .document(docRef.id)
                    .update("status", "sent")
                    .addOnSuccessListener {
                        android.util.Log.d("PushNotification", "✅ Notification status updated to sent")
                        
                        // Now send via FCM
                        sendViaFCMTopic(title, message, docRef.id)
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("PushNotification", "❌ Failed to update status", e)
                        // Still try to send
                        sendViaFCMTopic(title, message, docRef.id)
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("PushNotification", "❌ Failed to create notification", e)
                binding.progressBar.visibility = View.GONE
                binding.btnSendNotification.isEnabled = true
                binding.btnSendNotification.text = "Send Notification"

                Toast.makeText(
                    this,
                    "❌ Failed to send notification: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun sendViaFCMTopic(title: String, message: String, notificationId: String) {
        // NEW: Get students from teacher's school only
        val query = if (teacherSchoolId != null) {
            android.util.Log.d("PushNotification", "Filtering by school: $teacherSchoolId")
            firestore.collection("Users")
                .whereEqualTo("role", "student")
                .whereEqualTo("schoolId", teacherSchoolId)
        } else {
            android.util.Log.d("PushNotification", "No school filter - sending to all students")
            firestore.collection("Users")
                .whereEqualTo("role", "student")
        }
        
        query.get()
            .addOnSuccessListener { documents ->
                val totalStudents = documents.size()
                val tokens = mutableListOf<String>()

                for (doc in documents) {
                    val token = doc.getString("fcmToken")
                    if (!token.isNullOrEmpty()) {
                        tokens.add(token)
                    }
                }

                android.util.Log.d("PushNotification", "Total students: $totalStudents")
                android.util.Log.d("PushNotification", "Students with FCM tokens: ${tokens.size}")

                // Update notification with recipient count
                firestore.collection("PushNotifications")
                    .document(notificationId)
                    .update(
                        mapOf(
                            "recipientCount" to totalStudents,
                            "studentsWithTokens" to tokens.size,
                            "deliveredAt" to Timestamp.now()
                        )
                    )

                binding.progressBar.visibility = View.GONE
                binding.btnSendNotification.isEnabled = true
                binding.btnSendNotification.text = "Send Notification"

                // NEW: Context-aware success message
                val successMessage = if (teacherSchoolId != null) {
                    if (tokens.size == totalStudents) {
                        "✅ Notification sent to $totalStudents students in your school!"
                    } else {
                        "✅ Notification sent to $totalStudents students in your school!\n(${tokens.size} will receive immediately)"
                    }
                } else {
                    if (tokens.size == totalStudents) {
                        "✅ Notification sent to $totalStudents students!"
                    } else {
                        "✅ Notification sent to $totalStudents students!\n(${tokens.size} will receive immediately)"
                    }
                }

                Toast.makeText(
                    this,
                    successMessage,
                    Toast.LENGTH_LONG
                ).show()

                // Clear form
                clearForm()

                // Note: Actual FCM sending would be done by Cloud Functions
                // This is a simplified version for demonstration
            }
            .addOnFailureListener { e ->
                android.util.Log.e("PushNotification", "❌ Failed to get student tokens", e)

                binding.progressBar.visibility = View.GONE
                binding.btnSendNotification.isEnabled = true
                binding.btnSendNotification.text = "Send Notification"

                Toast.makeText(
                    this,
                    "⚠️ Notification queued but delivery may be delayed",
                    Toast.LENGTH_LONG
                ).show()

                clearForm()
            }
    }

    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etMessage.text?.clear()
        binding.etTitle.requestFocus()
    }
}
