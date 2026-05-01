package com.example.capstone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.capstone.databinding.ActivityAnnouncementsBinding
import com.example.capstone.models.Announcement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AnnouncementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnouncementsBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val announcements = mutableListOf<Announcement>()
    private lateinit var adapter: AnnouncementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Swipe refresh colors
        binding.swipeRefreshLayout.setColorSchemeColors(
            0xFF4CAF50.toInt(),
            0xFF388E3C.toInt(),
            0xFF2E7D32.toInt()
        )
        binding.swipeRefreshLayout.setOnRefreshListener { loadAnnouncements() }

        setupRecyclerView()
        loadAnnouncements()
    }

    private fun setupRecyclerView() {
        adapter = AnnouncementsAdapter(announcements)
        binding.recyclerViewAnnouncements.apply {
            layoutManager = LinearLayoutManager(this@AnnouncementsActivity)
            adapter = this@AnnouncementsActivity.adapter
        }
    }

    private fun loadAnnouncements() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.recyclerViewAnnouncements.visibility = View.GONE

        val userId = auth.currentUser?.uid ?: return
        
        // First, get the student's schoolId
        firestore.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                if (!userDoc.exists()) {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }
                
                val studentSchoolId = userDoc.getString("schoolId") ?: ""
               ""
                if (studentSchoolId.isEmpty()) {
                    // Student has no school assigned
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }
                
                // Load announcements filtered by schoolId
                firestore.collection("Announcements")
                    .whereEqualTo("schoolId", studentSchoolId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .addOnSuccessListener { documents ->
                        binding.swipeRefreshLayout.isRefreshing = false
                        announcements.clear()

                        for (doc in documents) {
                            try {
                                val announcement = Announcement(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "",
                                    message = doc.getString("message") ?: "",
                                    createdBy = doc.getString("createdBy") ?: "Teacher",
                                    createdAt = doc.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now(),
                                    readBy = doc.get("readBy") as? List<String> ?: emptyList(),
                                    schoolId = doc.getString("schoolId") ?: ""
                                )
                                if (announcement.title.isNotEmpty()) {
                                    announcements.add(announcement)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("Announcements", "Error parsing doc ${doc.id}", e)
                            }
                        }

                        binding.progressBar.visibility = View.GONE

                        if (announcements.isEmpty()) {
                            binding.tvEmptyState.visibility = View.VISIBLE
                            binding.recyclerViewAnnouncements.visibility = View.GONE
                        } else {
                            binding.tvEmptyState.visibility = View.GONE
                            binding.recyclerViewAnnouncements.visibility = View.VISIBLE
                            adapter.notifyDataSetChanged()
                            markAllAsRead()
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.progressBar.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.recyclerViewAnnouncements.visibility = View.GONE
                        android.util.Log.e("Announcements", "Failed to load", e)
                    }
            }
            .addOnFailureListener { e ->
                binding.swipeRefreshLayout.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                android.util.Log.e("Announcements", "Failed to load user", e)
            }
    }

    private fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        android.util.Log.d("Announcements", "Marking ${announcements.size} announcements as read for user: $userId")
        
        for (announcement in announcements) {
            if (!announcement.isReadBy(userId)) {
                android.util.Log.d("Announcements", "Marking announcement ${announcement.id} as read")
                firestore.collection("Announcements")
                    .document(announcement.id)
                    .update("readBy", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                    .addOnSuccessListener {
                        android.util.Log.d("Announcements", "✅ Successfully marked ${announcement.id} as read")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("Announcements", "❌ Failed to mark ${announcement.id} as read: ${e.message}")
                    }
            } else {
                android.util.Log.d("Announcements", "Announcement ${announcement.id} already read by user")
            }
        }
    }
}

class AnnouncementsAdapter(
    private val announcements: List<Announcement>
) : RecyclerView.Adapter<AnnouncementsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvAnnouncementTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvAnnouncementMessage)
        val tvMeta: TextView = view.findViewById(R.id.tvAnnouncementMeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val announcement = announcements[position]
        holder.tvTitle.text = announcement.title
        holder.tvMessage.text = announcement.message

        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(announcement.createdAt.toDate())
        holder.tvMeta.text = "By ${announcement.createdBy} • $dateStr"
    }

    override fun getItemCount() = announcements.size
}
