package com.example.capstone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.databinding.ActivitySendAnnouncementBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SendAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendAnnouncementBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // NEW: Multi-tenancy fields
    private var teacherSchoolId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadTeacherInfo()
        setupClickListeners()
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
                    android.util.Log.d("SendAnnouncement", "Teacher school: $teacherSchoolId")
                    updateToolbarSubtitle()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("SendAnnouncement", "Failed to load teacher info", e)
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
        binding.toolbar.title = "📢 Send Announcement"
        binding.toolbar.setTitleTextColor(0xFFFFFFFF.toInt())
        binding.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            sendAnnouncement()
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun sendAnnouncement() {
        val title = binding.etTitle.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()

        // Validation
        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            binding.etTitle.requestFocus()
            return
        }
        if (title.length > 100) {
            binding.etTitle.error = "Title must be under 100 characters"
            binding.etTitle.requestFocus()
            return
        }
        if (message.isEmpty()) {
            binding.etMessage.error = "Message is required"
            binding.etMessage.requestFocus()
            return
        }
        if (message.length > 1000) {
            binding.etMessage.error = "Message must be under 1000 characters"
            binding.etMessage.requestFocus()
            return
        }

        // Disable button to prevent double submission
        binding.btnSend.isEnabled = false
        binding.btnSend.text = "Sending..."

        val currentAdmin = auth.currentUser
        val adminName = currentAdmin?.displayName?.takeIf { it.isNotEmpty() }
            ?: currentAdmin?.email
            ?: "Admin"

        android.util.Log.d("SendAnnouncement", "Sending announcement: '$title' by $adminName")

        val announcementData = hashMapOf(
            "title" to title,
            "message" to message,
            "createdBy" to adminName,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "readBy" to emptyList<String>(),
            "schoolId" to (teacherSchoolId ?: "")  // NEW: Add school filter
        )

        firestore.collection("Announcements")
            .add(announcementData)
            .addOnSuccessListener { docRef ->
                android.util.Log.d("SendAnnouncement", "✅ Announcement sent with ID: ${docRef.id}")
                
                // NEW: Context-aware success message
                val successMessage = if (teacherSchoolId != null) {
                    "✅ Announcement sent to students in your school!"
                } else {
                    "✅ Announcement sent to all students!"
                }
                
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    successMessage,
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
                // Small delay so user sees the snackbar before closing
                binding.root.postDelayed({ finish() }, 1500)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("SendAnnouncement", "❌ Failed to send announcement", e)
                binding.btnSend.isEnabled = true
                binding.btnSend.text = "Send Announcement"

                val errorMessage = when {
                    e.message?.contains("PERMISSION_DENIED") == true ->
                        "❌ Permission denied. Check Firestore security rules."
                    e.message?.contains("network") == true || e.message?.contains("UNAVAILABLE") == true ->
                        "❌ Network error. Check your connection."
                    else ->
                        "❌ Failed to send: ${e.message}"
                }

                com.google.android.material.snackbar.Snackbar.make(binding.root, errorMessage, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
            }
    }
}
