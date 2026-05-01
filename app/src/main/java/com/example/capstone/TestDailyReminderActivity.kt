package com.example.capstone

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.capstone.workers.DailyReminderWorker
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ExecutionException

/**
 * Test Activity for Daily Reminder Feature
 * This activity allows you to test the daily reminder notification immediately
 * without waiting for the scheduled time.
 */
class TestDailyReminderActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnTestNow: Button
    private lateinit var btnCheckSettings: Button
    private lateinit var btnCheckWorkManager: Button
    
    companion object {
        private const val TAG = "TestDailyReminder"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_daily_reminder)

        setupToolbar()
        setupViews()
        setupClickListeners()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Test Daily Reminder"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupViews() {
        tvStatus = findViewById(R.id.tvStatus)
        btnTestNow = findViewById(R.id.btnTestNow)
        btnCheckSettings = findViewById(R.id.btnCheckSettings)
        btnCheckWorkManager = findViewById(R.id.btnCheckWorkManager)
    }

    private fun setupClickListeners() {
        btnTestNow.setOnClickListener {
            testDailyReminderNow()
        }

        btnCheckSettings.setOnClickListener {
            checkPlatformSettings()
        }

        btnCheckWorkManager.setOnClickListener {
            checkWorkManagerStatus()
        }
    }

    private fun testDailyReminderNow() {
        updateStatus("🔄 Testing daily reminder notification...")
        Log.d(TAG, "Testing daily reminder now")

        // Create a one-time work request to test immediately
        val testWorkRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .addTag("test_daily_reminder")
            .build()

        WorkManager.getInstance(this).enqueue(testWorkRequest)

        // Observe the work status
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(testWorkRequest.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.ENQUEUED -> {
                            updateStatus("⏳ Work enqueued, waiting to run...")
                            Log.d(TAG, "Work enqueued")
                        }
                        WorkInfo.State.RUNNING -> {
                            updateStatus("🔄 Worker is running...")
                            Log.d(TAG, "Work running")
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            updateStatus("✅ Test notification sent! Check your notification tray.")
                            Toast.makeText(this, "✅ Check your notification tray!", Toast.LENGTH_LONG).show()
                            Log.d(TAG, "Work succeeded")
                        }
                        WorkInfo.State.FAILED -> {
                            updateStatus("❌ Worker failed. Check Logcat for details.")
                            Toast.makeText(this, "❌ Failed. Check Logcat.", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Work failed")
                        }
                        WorkInfo.State.CANCELLED -> {
                            updateStatus("⚠️ Work was cancelled")
                            Log.w(TAG, "Work cancelled")
                        }
                        else -> {
                            updateStatus("Status: ${workInfo.state}")
                        }
                    }
                }
            })
    }

    private fun checkPlatformSettings() {
        updateStatus("🔄 Checking platform settings...")
        Log.d(TAG, "Checking platform settings")

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("PlatformSettings")
            .document("config")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val notifications = document.data?.get("notifications") as? Map<String, Any>
                    val enableDailyReminders = notifications?.get("enableDailyReminders") as? Boolean ?: false
                    val reminderTime = notifications?.get("dailyReminderTime") as? String ?: "Not set"
                    val reminderMessage = notifications?.get("reminderMessage") as? String ?: "Not set"

                    val status = """
                        ✅ Platform Settings Found:
                        
                        • Daily Reminders: ${if (enableDailyReminders) "✅ Enabled" else "❌ Disabled"}
                        • Reminder Time: $reminderTime
                        • Message: $reminderMessage
                        
                        ${if (!enableDailyReminders) "⚠️ Daily reminders are disabled!" else ""}
                    """.trimIndent()

                    updateStatus(status)
                    Log.d(TAG, "Settings: enabled=$enableDailyReminders, time=$reminderTime")
                } else {
                    updateStatus("❌ Platform settings document not found!")
                    Log.e(TAG, "Settings document not found")
                }
            }
            .addOnFailureListener { e ->
                updateStatus("❌ Failed to load settings: ${e.message}")
                Log.e(TAG, "Failed to load settings", e)
            }
    }

    private fun checkWorkManagerStatus() {
        updateStatus("🔄 Checking WorkManager status...")
        Log.d(TAG, "Checking WorkManager status")

        // Use LiveData to observe WorkManager status
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData("daily_reminder")
            .observe(this, Observer { workInfos ->
                if (workInfos == null || workInfos.isEmpty()) {
                    updateStatus("⚠️ No scheduled work found!\n\nDaily reminder is not scheduled.")
                    Log.w(TAG, "No work found")
                } else {
                    val workInfo = workInfos[0]
                    val status = """
                        ✅ WorkManager Status:
                        
                        • State: ${workInfo.state}
                        • Run Attempt: ${workInfo.runAttemptCount}
                        • Tags: ${workInfo.tags.joinToString(", ")}
                        • ID: ${workInfo.id}
                        
                        ${if (workInfo.state == WorkInfo.State.ENQUEUED) "✅ Work is scheduled and waiting" else ""}
                        ${if (workInfo.state == WorkInfo.State.RUNNING) "🔄 Work is currently running" else ""}
                        ${if (workInfo.state == WorkInfo.State.SUCCEEDED) "✅ Last run was successful" else ""}
                        ${if (workInfo.state == WorkInfo.State.FAILED) "❌ Last run failed" else ""}
                    """.trimIndent()

                    updateStatus(status)
                    Log.d(TAG, "Work state: ${workInfo.state}")
                }
            })
    }

    private fun updateStatus(message: String) {
        tvStatus.text = message
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
