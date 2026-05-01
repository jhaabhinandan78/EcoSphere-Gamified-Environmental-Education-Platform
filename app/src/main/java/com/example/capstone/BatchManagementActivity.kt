package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.databinding.ActivityBatchManagementBinding
import com.example.capstone.models.Batch
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Activity for teachers to manage their batches/classes
 * Teachers can create batches and view batch details
 */
class BatchManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBatchManagementBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var batchAdapter: BatchAdapter
    
    private var teacherSchoolId: String? = null
    private var teacherName: String? = null
    private var teacherId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityBatchManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        teacherId = auth.currentUser?.uid
        
        setupToolbar()
        setupRecyclerView()
        loadTeacherInfo()
        
        binding.fabCreateBatch.setOnClickListener {
            showCreateBatchDialog()
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        batchAdapter = BatchAdapter(emptyList()) { batch ->
            showBatchDetails(batch)
        }
        
        binding.rvBatches.apply {
            layoutManager = LinearLayoutManager(this@BatchManagementActivity)
            adapter = batchAdapter
        }
    }
    
    /**
     * Load teacher information from Firestore
     */
    private fun loadTeacherInfo() {
        if (teacherId == null) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        showLoading(true)
        
        firestore.collection("Users")
            .document(teacherId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    teacherSchoolId = document.getString("schoolId")
                    teacherName = document.getString("name")
                    
                    if (teacherSchoolId != null) {
                        loadBatches()
                    } else {
                        showLoading(false)
                        Toast.makeText(
                            this,
                            "No school associated with your account",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    /**
     * Load batches created by this teacher
     */
    private fun loadBatches() {
        if (teacherSchoolId == null) return
        
        showLoading(true)
        
        firestore.collection("Batches")
            .whereEqualTo("schoolId", teacherSchoolId)
            .whereEqualTo("teacherId", teacherId)
            .get()
            .addOnSuccessListener { documents ->
                showLoading(false)
                
                val batches = documents.toObjects(Batch::class.java)
                
                if (batches.isEmpty()) {
                    showEmptyState(true)
                } else {
                    showEmptyState(false)
                    batchAdapter.updateBatches(batches)
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error loading batches: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    /**
     * Show dialog to create a new batch
     */
    private fun showCreateBatchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_batch, null)
        val etBatchName = dialogView.findViewById<TextInputEditText>(R.id.etBatchName)
        val etAcademicYear = dialogView.findViewById<TextInputEditText>(R.id.etAcademicYear)
        
        // Pre-fill academic year with current year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        etAcademicYear.setText("$currentYear-${currentYear + 1}")
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<View>(R.id.btnCreate).setOnClickListener {
            val batchName = etBatchName.text.toString().trim()
            val academicYear = etAcademicYear.text.toString().trim()
            
            if (batchName.isEmpty()) {
                Toast.makeText(this, "Please enter batch name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (academicYear.isEmpty()) {
                Toast.makeText(this, "Please enter academic year", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            dialog.dismiss()
            createBatch(batchName, academicYear)
        }
        
        dialog.show()
    }
    
    /**
     * Create a new batch in Firestore
     */
    private fun createBatch(batchName: String, academicYear: String) {
        if (teacherSchoolId == null || teacherName == null || teacherId == null) {
            Toast.makeText(this, "Missing teacher information", Toast.LENGTH_SHORT).show()
            return
        }
        
        showLoading(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Generate unique batch ID
                val batchId = firestore.collection("Batches").document().id
                
                val batch = Batch(
                    batchId = batchId,
                    batchName = batchName,
                    schoolId = teacherSchoolId!!,
                    teacherId = teacherId!!,
                    teacherName = teacherName!!,
                    academicYear = academicYear,
                    isActive = true,
                    createdAt = Timestamp.now(),
                    studentCount = 0
                )
                
                // Save batch to Firestore
                firestore.collection("Batches")
                    .document(batchId)
                    .set(batch)
                    .await()
                
                // Update teacher's assignedBatches array
                firestore.collection("Users")
                    .document(teacherId!!)
                    .update("assignedBatches", FieldValue.arrayUnion(batchId))
                    .await()
                
                // Update school's totalBatches counter
                firestore.collection("Schools")
                    .document(teacherSchoolId!!)
                    .update("totalBatches", FieldValue.increment(1))
                    .await()
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@BatchManagementActivity,
                        "Batch created successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Reload batches
                    loadBatches()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@BatchManagementActivity,
                        "Error creating batch: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Show batch details dialog
     */
    private fun showBatchDetails(batch: Batch) {
        val message = """
            Batch Name: ${batch.batchName}
            Academic Year: ${batch.academicYear}
            Teacher: ${batch.teacherName}
            Students: ${batch.studentCount}
            Status: ${if (batch.isActive) "Active" else "Inactive"}
            
            Batch ID: ${batch.batchId}
            
            Students can register using your school code and selecting this batch.
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Batch Details")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Show/hide loading indicator
     */
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvBatches.visibility = if (show) View.GONE else View.VISIBLE
        binding.fabCreateBatch.isEnabled = !show
    }
    
    /**
     * Show/hide empty state
     */
    private fun showEmptyState(show: Boolean) {
        binding.llEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvBatches.visibility = if (show) View.GONE else View.VISIBLE
    }
}
