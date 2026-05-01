package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.databinding.ActivityReviewChallengesBinding
import com.example.capstone.models.ActivityFeed
import com.example.capstone.models.ChallengeSubmission
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewChallengesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewChallengesBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var submissionsAdapter: ChallengeSubmissionsAdapter
    private val submissions = mutableListOf<ChallengeSubmission>()
    
    // NEW: Multi-tenancy fields
    private var teacherSchoolId: String? = null
    private var assignedBatches: List<String> = emptyList()
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewChallengesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadTeacherInfo()
    }
    
    /**
     * NEW: Load teacher information to get assigned batches
     */
    private fun loadTeacherInfo() {
        currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            loadPendingSubmissions()
            return
        }
        
        firestore.collection("Users")
            .document(currentUserId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    teacherSchoolId = document.getString("schoolId")
                    val batchesArray = document.get("assignedBatches") as? List<*>
                    assignedBatches = batchesArray?.mapNotNull { it as? String } ?: emptyList()
                    
                    android.util.Log.d("ReviewChallenges", "Teacher school: $teacherSchoolId")
                    android.util.Log.d("ReviewChallenges", "Assigned batches: $assignedBatches")
                }
                loadPendingSubmissions()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ReviewChallenges", "Failed to load teacher info", e)
                loadPendingSubmissions()
            }
    }

    private fun setupToolbar() {
        binding.toolbar.title = "📝 Review Challenges"
        binding.toolbar.setTitleTextColor(0xFFFFFFFF.toInt())
        binding.toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        submissionsAdapter = ChallengeSubmissionsAdapter(
            submissions = submissions,
            onApproveClick = { submission -> showApproveDialog(submission) },
            onRejectClick = { submission -> showRejectDialog(submission) }
        )
        binding.recyclerViewSubmissions.apply {
            layoutManager = LinearLayoutManager(this@ReviewChallengesActivity)
            adapter = submissionsAdapter
        }
    }

    private fun loadPendingSubmissions() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE

        android.util.Log.d("ReviewChallenges", "Loading pending submissions...")

        // NEW: Filter by batches or school
        // First, get the base query for pending submissions
        val baseQuery = firestore.collection("ChallengeSubmissions")
            .whereEqualTo("status", ChallengeSubmission.STATUS_PENDING)
            .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
        
        // If teacher has batches, we need to filter by students in those batches
        if (assignedBatches.isNotEmpty()) {
            // Get students from assigned batches first
            loadSubmissionsByBatches()
        } else if (teacherSchoolId != null) {
            // Get students from school first
            loadSubmissionsBySchool()
        } else {
            // Admin mode - load all pending submissions
            loadAllPendingSubmissions(baseQuery)
        }
    }
    
    /**
     * NEW: Load submissions from students in teacher's batches
     */
    private fun loadSubmissionsByBatches() {
        android.util.Log.d("ReviewChallenges", "Filtering by batches: $assignedBatches")
        
        // First, get all students in the assigned batches
        firestore.collection("Users")
            .whereEqualTo("role", "student")
            .whereIn("batchId", assignedBatches.take(10))
            .get()
            .addOnSuccessListener { studentDocs ->
                val studentIds = studentDocs.documents.mapNotNull { it.id }
                android.util.Log.d("ReviewChallenges", "Found ${studentIds.size} students in batches")
                
                if (studentIds.isEmpty()) {
                    showEmptyState("No students in your batches yet")
                    return@addOnSuccessListener
                }
                
                // Now get submissions from these students
                loadSubmissionsForStudents(studentIds)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ReviewChallenges", "Failed to load students by batches", e)
                handleLoadError(e)
            }
    }
    
    /**
     * NEW: Load submissions from students in teacher's school
     */
    private fun loadSubmissionsBySchool() {
        android.util.Log.d("ReviewChallenges", "Filtering by school: $teacherSchoolId")
        
        // First, get all students in the school
        firestore.collection("Users")
            .whereEqualTo("role", "student")
            .whereEqualTo("schoolId", teacherSchoolId)
            .get()
            .addOnSuccessListener { studentDocs ->
                val studentIds = studentDocs.documents.mapNotNull { it.id }
                android.util.Log.d("ReviewChallenges", "Found ${studentIds.size} students in school")
                
                if (studentIds.isEmpty()) {
                    showEmptyState("No students in your school yet")
                    return@addOnSuccessListener
                }
                
                // Now get submissions from these students
                loadSubmissionsForStudents(studentIds)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ReviewChallenges", "Failed to load students by school", e)
                handleLoadError(e)
            }
    }
    
    /**
     * NEW: Load submissions for specific student IDs
     */
    private fun loadSubmissionsForStudents(studentIds: List<String>) {
        if (studentIds.isEmpty()) {
            showEmptyState("No students found")
            return
        }
        
        // Firestore whereIn limit is 10, so we need to batch if more students
        val batches = studentIds.chunked(10)
        val allSubmissions = mutableListOf<ChallengeSubmission>()
        var completedBatches = 0
        
        batches.forEach { batch ->
            firestore.collection("ChallengeSubmissions")
                .whereEqualTo("status", ChallengeSubmission.STATUS_PENDING)
                .whereIn("studentId", batch)
                .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    android.util.Log.d("ReviewChallenges", "Batch query found ${documents.size()} submissions")
                    
                    for (doc in documents) {
                        try {
                            val submission = ChallengeSubmission(
                                id = doc.id,
                                challengeId = doc.getString("challengeId") ?: "",
                                challengeTitle = doc.getString("challengeTitle") ?: "",
                                challengePoints = doc.getLong("challengePoints") ?: 0,
                                studentId = doc.getString("studentId") ?: "",
                                studentName = doc.getString("studentName") ?: "",
                                submittedAt = doc.getTimestamp("submittedAt") ?: com.google.firebase.Timestamp.now(),
                                status = doc.getString("status") ?: ChallengeSubmission.STATUS_PENDING,
                                photoUrl = doc.getString("photoUrl") ?: "",
                                reviewedBy = doc.getString("reviewedBy") ?: "",
                                reviewedAt = doc.getTimestamp("reviewedAt"),
                                adminFeedback = doc.getString("adminFeedback") ?: ""
                            )
                            allSubmissions.add(submission)
                        } catch (e: Exception) {
                            android.util.Log.e("ReviewChallenges", "Error parsing submission ${doc.id}", e)
                        }
                    }
                    
                    completedBatches++
                    if (completedBatches == batches.size) {
                        // All batches loaded, display results
                        displaySubmissions(allSubmissions)
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("ReviewChallenges", "Failed to load submission batch", e)
                    completedBatches++
                    if (completedBatches == batches.size) {
                        displaySubmissions(allSubmissions)
                    }
                }
        }
    }
    
    /**
     * NEW: Load all pending submissions (admin mode)
     */
    private fun loadAllPendingSubmissions(query: com.google.firebase.firestore.Query) {
        android.util.Log.d("ReviewChallenges", "Loading all pending submissions (admin mode)")
        
        query.get()
            .addOnSuccessListener { documents ->
                android.util.Log.d("ReviewChallenges", "✅ Query successful! Found ${documents.size()} submissions")
                
                val submissionsList = mutableListOf<ChallengeSubmission>()
                for (doc in documents) {
                    try {
                        val submission = ChallengeSubmission(
                            id = doc.id,
                            challengeId = doc.getString("challengeId") ?: "",
                            challengeTitle = doc.getString("challengeTitle") ?: "",
                            challengePoints = doc.getLong("challengePoints") ?: 0,
                            studentId = doc.getString("studentId") ?: "",
                            studentName = doc.getString("studentName") ?: "",
                            submittedAt = doc.getTimestamp("submittedAt") ?: com.google.firebase.Timestamp.now(),
                            status = doc.getString("status") ?: ChallengeSubmission.STATUS_PENDING,
                            photoUrl = doc.getString("photoUrl") ?: "",
                            reviewedBy = doc.getString("reviewedBy") ?: "",
                            reviewedAt = doc.getTimestamp("reviewedAt"),
                            adminFeedback = doc.getString("adminFeedback") ?: ""
                        )
                        submissionsList.add(submission)
                    } catch (e: Exception) {
                        android.util.Log.e("ReviewChallenges", "Error parsing submission ${doc.id}", e)
                    }
                }
                
                displaySubmissions(submissionsList)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ReviewChallenges", "❌ Failed to load submissions", e)
                handleLoadError(e)
            }
    }
    
    /**
     * NEW: Display submissions in RecyclerView
     */
    private fun displaySubmissions(submissionsList: List<ChallengeSubmission>) {
        submissions.clear()
        submissions.addAll(submissionsList)
        
        binding.progressBar.visibility = View.GONE
        binding.tvPendingCount.text = "Pending Reviews: ${submissions.size}"
        
        if (submissions.isEmpty()) {
            showEmptyState("✅ All caught up!\n\nNo pending challenge submissions\nto review at the moment")
        } else {
            android.util.Log.d("ReviewChallenges", "Displaying ${submissions.size} submissions")
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerViewSubmissions.visibility = View.VISIBLE
            submissionsAdapter.notifyDataSetChanged()
        }
    }
    
    /**
     * NEW: Show empty state with custom message
     */
    private fun showEmptyState(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.tvEmptyState.text = message
        binding.recyclerViewSubmissions.visibility = View.GONE
        binding.tvPendingCount.text = "Pending Reviews: 0"
    }
    
    /**
     * NEW: Handle load errors
     */
    private fun handleLoadError(e: Exception) {
        binding.progressBar.visibility = View.GONE
        binding.tvEmptyState.visibility = View.VISIBLE
        
        val errorMsg = if (e.message?.contains("index") == true) {
            "❌ Database index required\n\nClick the link in Android Studio Logcat\nto create the required index"
        } else if (e.message?.contains("permission") == true || e.message?.contains("PERMISSION_DENIED") == true) {
            "❌ Permission denied\n\nCheck Firestore security rules\nfor ChallengeSubmissions collection"
        } else {
            "❌ Error loading submissions\n\n${e.message}"
        }
        
        binding.tvEmptyState.text = errorMsg
        binding.recyclerViewSubmissions.visibility = View.GONE
    }

    private fun showApproveDialog(submission: ChallengeSubmission) {
        AlertDialog.Builder(this)
            .setTitle("Approve Challenge")
            .setMessage("Approve ${submission.studentName}'s submission for \"${submission.challengeTitle}\"?\n\nThis will award ${submission.challengePoints} points to the student.")
            .setPositiveButton("Approve") { dialog, _ ->
                approveSubmission(submission)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showRejectDialog(submission: ChallengeSubmission) {
        val input = EditText(this)
        input.hint = "Enter feedback for student (required)"
        input.setPadding(50, 30, 50, 30)

        AlertDialog.Builder(this)
            .setTitle("Reject Challenge")
            .setMessage("Provide feedback to ${submission.studentName} about why their submission for \"${submission.challengeTitle}\" was rejected:")
            .setView(input)
            .setPositiveButton("Reject") { dialog, _ ->
                val feedback = input.text.toString().trim()
                if (feedback.isEmpty()) {
                    android.widget.Toast.makeText(this, "Please provide feedback", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    rejectSubmission(submission, feedback)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun approveSubmission(submission: ChallengeSubmission) {
        val currentAdmin = auth.currentUser
        val adminName = currentAdmin?.displayName ?: "Admin"

        // Update submission status
        val submissionUpdate = hashMapOf(
            "status" to ChallengeSubmission.STATUS_APPROVED,
            "reviewedBy" to adminName,
            "reviewedAt" to Timestamp.now()
        )

        firestore.collection("ChallengeSubmissions")
            .document(submission.id)
            .update(submissionUpdate as Map<String, Any>)
            .addOnSuccessListener {
                // Award points to student
                firestore.collection("Users")
                    .document(submission.studentId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val currentPoints = doc.getLong("ecoPoints") ?: 0
                        val newPoints = currentPoints + submission.challengePoints

                        firestore.collection("Users")
                            .document(submission.studentId)
                            .update("ecoPoints", newPoints)
                            .addOnSuccessListener {
                                // Create activity feed entry
                                createActivityFeedEntry(submission)
                                
                                android.widget.Toast.makeText(
                                    this,
                                    "Challenge approved! ${submission.challengePoints} points awarded.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                
                                // Reload submissions
                                loadPendingSubmissions()
                            }
                    }
            }
            .addOnFailureListener {
                android.widget.Toast.makeText(this, "Failed to approve challenge", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectSubmission(submission: ChallengeSubmission, feedback: String) {
        val currentAdmin = auth.currentUser
        val adminName = currentAdmin?.displayName ?: "Admin"

        val submissionUpdate = hashMapOf(
            "status" to ChallengeSubmission.STATUS_REJECTED,
            "reviewedBy" to adminName,
            "reviewedAt" to Timestamp.now(),
            "adminFeedback" to feedback
        )

        firestore.collection("ChallengeSubmissions")
            .document(submission.id)
            .update(submissionUpdate as Map<String, Any>)
            .addOnSuccessListener {
                android.widget.Toast.makeText(
                    this,
                    "Challenge rejected. Feedback sent to student.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                
                // Reload submissions
                loadPendingSubmissions()
            }
            .addOnFailureListener {
                android.widget.Toast.makeText(this, "Failed to reject challenge", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun createActivityFeedEntry(submission: ChallengeSubmission) {
        val activityData = hashMapOf(
            "userId" to submission.studentId,
            "userName" to submission.studentName,
            "activityType" to ActivityFeed.TYPE_CHALLENGE_SUBMITTED,
            "activityTitle" to submission.challengeTitle,
            "points" to submission.challengePoints,
            "metadata" to mapOf("status" to "approved"),
            "timestamp" to Timestamp.now()
        )

        firestore.collection("ActivityFeed")
            .add(activityData)
    }
}
