package com.example.capstone

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.databinding.ActivityInAppNotificationsBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InAppNotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInAppNotificationsBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: InAppNotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInAppNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = InAppNotificationAdapter { notification ->
            markAsRead(notification.id)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadNotifications() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE

        val userId = auth.currentUser?.uid ?: return

        firestore.collection("PushNotifications")
            .whereEqualTo("type", "admin_push")
            .whereEqualTo("status", "sent")
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE

                if (documents.isEmpty) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE

                    val notifications = documents.mapNotNull { doc ->
                        try {
                            InAppNotification(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                sentAt = doc.getTimestamp("sentAt") ?: Timestamp.now(),
                                isRead = isNotificationRead(userId, doc.id)
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.sentAt.seconds }

                    adapter.submitList(notifications)
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }
    }

    private fun isNotificationRead(userId: String, notificationId: String): Boolean {
        val prefs = getSharedPreferences("notifications", MODE_PRIVATE)
        return prefs.getBoolean("read_${userId}_$notificationId", false)
    }

    private fun markAsRead(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        val prefs = getSharedPreferences("notifications", MODE_PRIVATE)
        prefs.edit().putBoolean("read_${userId}_$notificationId", true).apply()
        loadNotifications()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val sentAt: Timestamp,
    val isRead: Boolean
)
