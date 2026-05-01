package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.databinding.ActivityTeacherApprovalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Activity for lead teachers to approve/reject pending teacher registrations
 * Only accessible by lead teachers (isLeadTeacher = true)
 */
class TeacherApprovalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherApprovalBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PendingTeacherAdapter
    
    private var teacherSchoolId: String? = null
    private var isLeadTeacher: Boolean = false
    private var pendingTeachersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityTeacherApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        
        setupToolbar()
        setupRecyclerView()
        verifyLeadTeacher()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = PendingTeacherAdapter(
            emptyList(),
            onApprove = { teacher -> showApproveConfirmation(teacher) },
            onReject = { teacher -> showRejectConfirmation(teacher) }
        )
        
        binding.rvPendingTeachers.apply {
            layoutManager = LinearLayoutManager(this@TeacherApprovalActivity)
            adapter = this@TeacherApprovalActivity.adapter
        }
    }
    
    /**
     * Verify that current user is a lead teacher
     */
    private fun verifyLeadTeacher() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        showLoading(true)
        
        firestore.collection("Users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    isLeadTeacher = document.getBoolean("isLeadTeacher") ?: false
                    teacherSchoolId = document.getString("schoolId")
                    
                    if (!isLeadTeacher) {
                        showLoading(false)
                        Toast.makeText(
                            this,
                            "Access denied. Only lead teachers can approve registrations.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else if (teacherSchoolId != null) {
                        loadPendingTeachers()
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "School information not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
    
    /**
     * Load pending teachers with real-time updates
     */
    private fun loadPendingTeachers() {
        if (teacherSchoolId == null) return
        
        showLoading(true)
        
        // Real-time listener for pending teachers (not yet approved AND not rejected)
        pendingTeachersListener = firestore.collection("Users")
            .whereEqualTo("schoolId", teacherSchoolId)
            .whereEqualTo("role", "teacher")
            .whereEqualTo("isApproved", false)
            .addSnapshotListener { snapshots, error ->
                showLoading(false)
                
                if (error != null) {
                    Toast.makeText(this, "Error loading teachers: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                
                if (snapshots != null) {
                    val pendingTeachers = snapshots.documents.mapNotNull { doc ->
                        // Skip rejected teachers — only show truly pending ones
                        val isRejected = doc.getBoolean("isRejected") ?: false
                        if (isRejected) return@mapNotNull null
                        
                        try {
                            PendingTeacher(
                                uid = doc.id,
                                name = doc.getString("name") ?: "",
                                email = doc.getString("email") ?: "",
                                phone = doc.getString("phone") ?: "",
                                dob = doc.getString("dob") ?: "",
                                gender = doc.getString("gender") ?: "",
                                schoolId = doc.getString("schoolId") ?: "",
                                schoolCode = doc.getString("schoolCode") ?: "",
                                createdAt = doc.getTimestamp("createdAt")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    if (pendingTeachers.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        adapter.updateTeachers(pendingTeachers)
                    }
                }
            }
    }
    
    /**
     * Show confirmation dialog before approving
     */
    private fun showApproveConfirmation(teacher: PendingTeacher) {
        AlertDialog.Builder(this)
            .setTitle("Approve Teacher")
            .setMessage("Approve ${teacher.name} as a teacher for your school?\n\nThey will be able to:\n• Create and manage batches\n• View students in their batches\n• Review challenge submissions")
            .setPositiveButton("Approve") { dialog, _ ->
                dialog.dismiss()
                approveTeacher(teacher)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Show confirmation dialog before rejecting
     */
    private fun showRejectConfirmation(teacher: PendingTeacher) {
        AlertDialog.Builder(this)
            .setTitle("Reject Teacher")
            .setMessage("Reject ${teacher.name}'s registration?\n\nTheir account will be deleted and they will need to register again.")
            .setPositiveButton("Reject") { dialog, _ ->
                dialog.dismiss()
                rejectTeacher(teacher)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Approve a teacher
     */
    private fun approveTeacher(teacher: PendingTeacher) {
        showLoading(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update isApproved to true
                firestore.collection("Users")
                    .document(teacher.uid)
                    .update("isApproved", true)
                    .await()
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@TeacherApprovalActivity,
                        "${teacher.name} has been approved!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Remove from list
                    adapter.removeTeacher(teacher)
                    
                    // Check if list is now empty
                    if (adapter.itemCount == 0) {
                        showEmptyState(true)
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@TeacherApprovalActivity,
                        "Error approving teacher: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Reject a teacher - marks as rejected in Firestore
     * Note: Firebase Auth account deletion requires the user to be signed in,
     * so we mark isApproved=false and add isRejected=true instead of deleting.
     * The login check in LoginActivity will block rejected teachers from logging in.
     */
    private fun rejectTeacher(teacher: PendingTeacher) {
        showLoading(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Mark as rejected — LoginActivity checks isApproved before allowing login
                firestore.collection("Users")
                    .document(teacher.uid)
                    .update(
                        mapOf(
                            "isApproved" to false,
                            "isRejected" to true,
                            "rejectedAt" to com.google.firebase.Timestamp.now()
                        )
                    )
                    .await()
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@TeacherApprovalActivity,
                        "${teacher.name}'s registration has been rejected",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    adapter.removeTeacher(teacher)
                    if (adapter.itemCount == 0) {
                        showEmptyState(true)
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@TeacherApprovalActivity,
                        "Error rejecting teacher: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Show/hide loading indicator
     */
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvPendingTeachers.visibility = if (show) View.GONE else View.VISIBLE
        binding.cardInfo.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    /**
     * Show/hide empty state
     */
    private fun showEmptyState(show: Boolean) {
        binding.llEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvPendingTeachers.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Remove real-time listener
        pendingTeachersListener?.remove()
    }
}
