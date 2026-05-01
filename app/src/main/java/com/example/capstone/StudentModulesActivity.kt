package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.databinding.ActivityStudentModulesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentModulesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentModulesBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val moduleList = mutableListOf<Module>()
    private var currentUserSchoolId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentModulesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.recyclerModules.layoutManager = LinearLayoutManager(this)
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                currentUserSchoolId = document.getString("schoolId") ?: ""

                if (currentUserSchoolId.isEmpty()) {
                    Toast.makeText(this, "Your account is not associated with a school", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                loadModules()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load user profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadModules() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("Modules")
            .whereEqualTo("schoolId", currentUserSchoolId)
            .orderBy("order")
            .get()
            .addOnSuccessListener { result ->
                moduleList.clear()
                for (document in result) {
                    try {
                        val module = document.toObject(Module::class.java)
                        moduleList.add(module.copy(id = document.id))
                    } catch (e: Exception) {
                        android.util.Log.e("StudentModules", "Error parsing module ${document.id}", e)
                    }
                }

                firestore.collection("Users")
                    .document(uid)
                    .collection("completions")
                    .get()
                    .addOnSuccessListener { completions ->
                        val completedIds = completions.documents.map { it.id }.toSet()
                        for (module in moduleList) {
                            module.isCompleted = completedIds.contains(module.id)
                        }
                        setupAdapter()
                    }
                    .addOnFailureListener {
                        setupAdapter()
                    }
            }
            .addOnFailureListener { e ->
                val msg = when {
                    e.message?.contains("PERMISSION_DENIED") == true -> "Permission denied. Check Firestore rules."
                    e.message?.contains("network") == true || e.message?.contains("UNAVAILABLE") == true -> "Network error. Check your connection."
                    else -> "Failed to load modules: ${e.message}"
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerModules.visibility = View.GONE
            }
    }

    private fun setupAdapter() {
        if (moduleList.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerModules.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerModules.visibility = View.VISIBLE
        }

        val adapter = ModuleAdapter(moduleList) { selectedModule ->
            val intent = Intent(this, ModuleDetailActivity::class.java)
            intent.putExtra("moduleId", selectedModule.id)
            intent.putExtra("title", selectedModule.title)
            intent.putExtra("content", selectedModule.content)
            intent.putExtra("points", selectedModule.points)
            startActivity(intent)
        }
        binding.recyclerModules.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (moduleList.isNotEmpty()) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            firestore.collection("Users")
                .document(uid)
                .collection("completions")
                .get()
                .addOnSuccessListener { completions ->
                    val completedIds = completions.documents.map { it.id }.toSet()
                    var changed = false
                    for (module in moduleList) {
                        val newStatus = completedIds.contains(module.id)
                        if (module.isCompleted != newStatus) {
                            module.isCompleted = newStatus
                            changed = true
                        }
                    }
                    if (changed) {
                        binding.recyclerModules.adapter?.notifyDataSetChanged()
                    }
                }
        }
    }
}
