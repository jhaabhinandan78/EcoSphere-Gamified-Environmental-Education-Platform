package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.databinding.ActivityQuizBinding
import com.example.capstone.utils.ActivityLogger
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var adapter: QuizAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val questionList = mutableListOf<QuizQuestion>()

    private lateinit var moduleId: String
    private lateinit var moduleTitle: String

    private var passThreshold = 80 // Default, will be loaded from PlatformSettings
    private var maxAttemptsPerDay = 3 // Default, will be loaded from PlatformSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moduleId = intent.getStringExtra("moduleId") ?: ""
        moduleTitle = intent.getStringExtra("moduleTitle") ?: "Module Quiz"

        // Setup toolbar
        setSupportActionBar(binding.toolbarQuiz)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$moduleTitle Quiz"
        binding.toolbarQuiz.setNavigationOnClickListener { finish() }

        binding.recyclerQuizQuestions.layoutManager = LinearLayoutManager(this)
        adapter = QuizAdapter(questionList)
        binding.recyclerQuizQuestions.adapter = adapter

        binding.btnSubmitQuiz.setOnClickListener {
            submitQuiz()
        }

        // Load settings and check attempt limit before loading questions
        loadPlatformSettings()
    }

    private fun loadPlatformSettings() {
        android.util.Log.d("QuizActivity", "Loading platform settings...")
        
        db.collection("PlatformSettings")
            .document("config")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val general = document.data?.get("general") as? Map<String, Any>
                    passThreshold = (general?.get("quizPassingScore") as? Long ?: 80L).toInt()
                    maxAttemptsPerDay = (general?.get("maxQuizAttempts") as? Long ?: 3L).toInt()
                    
                    android.util.Log.d("QuizActivity", "✅ Settings loaded: passThreshold=$passThreshold, maxAttemptsPerDay=$maxAttemptsPerDay")
                } else {
                    android.util.Log.w("QuizActivity", "⚠️ PlatformSettings not found, using defaults")
                }
                
                // After loading settings, check attempt limit
                checkAttemptLimit()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizActivity", "❌ Failed to load settings: ${e.message}")
                Toast.makeText(this, "Failed to load quiz settings, using defaults", Toast.LENGTH_SHORT).show()
                
                // Continue with defaults
                checkAttemptLimit()
            }
    }

    private fun checkAttemptLimit() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        android.util.Log.d("QuizActivity", "Checking attempt limit for module: $moduleId")

        val userRef = db.collection("Users").document(uid)
        val quizAttemptRef = userRef.collection("quizAttempts").document(moduleId)

        quizAttemptRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val passed = document.getBoolean("passed") ?: false
                    
                    if (passed) {
                        android.util.Log.d("QuizActivity", "✅ Quiz already passed, allowing access")
                        loadQuestions()
                        return@addOnSuccessListener
                    }
                    
                    // Check attempts today
                    val attemptsToday = document.getLong("attemptsToday") ?: 0L
                    val lastAttemptDate = document.getTimestamp("lastAttemptDate")
                    
                    val isToday = lastAttemptDate?.let { isSameDay(it.toDate(), Date()) } ?: false
                    
                    android.util.Log.d("QuizActivity", "Attempts today: $attemptsToday, isToday: $isToday, maxAllowed: $maxAttemptsPerDay")
                    
                    if (isToday && attemptsToday >= maxAttemptsPerDay) {
                        // Max attempts reached for today
                        showMaxAttemptsDialog(attemptsToday.toInt())
                    } else {
                        // Allow quiz
                        loadQuestions()
                    }
                } else {
                    // First attempt
                    android.util.Log.d("QuizActivity", "First attempt for this quiz")
                    loadQuestions()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizActivity", "❌ Failed to check attempts: ${e.message}")
                Toast.makeText(this, "Failed to check quiz attempts", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun showMaxAttemptsDialog(attemptsUsed: Int) {
        android.util.Log.d("QuizActivity", "⚠️ Max attempts reached: $attemptsUsed/$maxAttemptsPerDay")
        
        AlertDialog.Builder(this)
            .setTitle("⚠️ Maximum Attempts Reached")
            .setMessage(
                "You have used all $maxAttemptsPerDay attempts for today.\n\n" +
                "Attempts used: $attemptsUsed/$maxAttemptsPerDay\n\n" +
                "Please come back tomorrow to try again with fresh attempts."
            )
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .show()
    }

    private fun loadQuestions() {
        if (moduleId.isBlank()) {
            Toast.makeText(this, "Invalid module id", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        android.util.Log.d("QuizActivity", "Loading questions for module: $moduleId")

        db.collection("Modules")
            .document(moduleId)
            .collection("questions")
            .get()
            .addOnSuccessListener { result ->
                questionList.clear()

                for (doc in result.documents) {
                    val q = doc.toObject(QuizQuestion::class.java)
                    if (q != null) {
                        questionList.add(q.copy(id = doc.id))
                    }
                }

                adapter.notifyDataSetChanged()

                if (questionList.isEmpty()) {
                    Toast.makeText(this, "No quiz questions found", Toast.LENGTH_SHORT).show()
                }
                
                android.util.Log.d("QuizActivity", "✅ Loaded ${questionList.size} questions")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizActivity", "❌ Failed to load questions: ${e.message}")
                Toast.makeText(this, "Failed to load quiz: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun submitQuiz() {
        if (questionList.isEmpty()) {
            Toast.makeText(this, "Quiz is empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (!adapter.allQuestionsAnswered()) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedAnswers = adapter.getSelectedAnswers()

        var correctCount = 0
        for (i in questionList.indices) {
            val selected = selectedAnswers[i]
            val correct = questionList[i].correctAnswer

            if (selected == correct) {
                correctCount++
            }
        }

        val totalQuestions = questionList.size
        val percentage = ((correctCount.toDouble() / totalQuestions.toDouble()) * 100).toInt()
        val passed = percentage >= passThreshold

        android.util.Log.d("QuizActivity", "Quiz submitted: $correctCount/$totalQuestions = $percentage% (pass threshold: $passThreshold%)")

        saveQuizResult(correctCount, totalQuestions, percentage, passed)
    }

    private fun saveQuizResult(
        correctCount: Int,
        totalQuestions: Int,
        percentage: Int,
        passed: Boolean
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = db.collection("Users").document(uid)
        val quizAttemptRef = userRef.collection("quizAttempts").document(moduleId)

        db.runTransaction { transaction ->

            val existingAttempt = transaction.get(quizAttemptRef)

            val previousPassed = existingAttempt.getBoolean("passed") ?: false
            val previousRewarded = existingAttempt.getBoolean("rewarded") ?: false

            // reward only if user passes now and was never rewarded before
            val shouldReward = passed && !previousPassed && !previousRewarded
            val finalRewarded = shouldReward || previousRewarded

            val earnedPoints = if (shouldReward) percentage.toLong() else 0L

            // Handle attempt tracking
            val lastAttemptDate = existingAttempt.getTimestamp("lastAttemptDate")
            val previousAttemptsToday = existingAttempt.getLong("attemptsToday") ?: 0L
            
            val isToday = lastAttemptDate?.let { isSameDay(it.toDate(), Date()) } ?: false
            val newAttemptsToday = if (isToday) previousAttemptsToday + 1 else 1L
            
            android.util.Log.d("QuizActivity", "Attempt tracking: isToday=$isToday, previous=$previousAttemptsToday, new=$newAttemptsToday")

            val quizData = hashMapOf(
                "moduleId" to moduleId,
                "moduleTitle" to moduleTitle,
                "score" to correctCount,
                "totalQuestions" to totalQuestions,
                "percentage" to percentage,
                "passed" to passed,
                "rewarded" to finalRewarded,
                "earnedPoints" to earnedPoints,
                "timestamp" to FieldValue.serverTimestamp(),
                "lastAttemptDate" to Timestamp.now(),
                "attemptsToday" to newAttemptsToday,
                "totalAttempts" to FieldValue.increment(1)
            )

            transaction.set(quizAttemptRef, quizData)

            if (shouldReward) {
                transaction.update(userRef, "ecoPoints", FieldValue.increment(earnedPoints))
            }

        }.addOnSuccessListener {
            // Log quiz completion to activity feed
            if (passed) {
                ActivityLogger.logQuizCompleted(moduleTitle, percentage, percentage.toLong())
            }
            showResultDialog(correctCount, totalQuestions, percentage, passed)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to save result: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showResultDialog(
        correctCount: Int,
        totalQuestions: Int,
        percentage: Int,
        passed: Boolean
    ) {
        // Check remaining attempts for today
        val uid = auth.currentUser?.uid
        if (uid == null) {
            finish()
            return
        }

        val quizAttemptRef = db.collection("Users").document(uid)
            .collection("quizAttempts").document(moduleId)

        quizAttemptRef.get()
            .addOnSuccessListener { document ->
                val attemptsToday = document.getLong("attemptsToday") ?: 1L
                val remainingAttempts = maxAttemptsPerDay - attemptsToday.toInt()
                
                android.util.Log.d("QuizActivity", "Result: passed=$passed, attemptsToday=$attemptsToday, remaining=$remainingAttempts")

                val message = if (passed) {
                    "Correct Answers: $correctCount / $totalQuestions\n" +
                            "Score: $percentage%\n" +
                            "Result: Passed ✅\n\n" +
                            "You can now go back and complete the module."
                } else {
                    if (remainingAttempts > 0) {
                        "Correct Answers: $correctCount / $totalQuestions\n" +
                                "Score: $percentage%\n" +
                                "Result: Failed ❌\n\n" +
                                "You need at least $passThreshold% to unlock module completion.\n\n" +
                                "Remaining attempts today: $remainingAttempts/$maxAttemptsPerDay"
                    } else {
                        "Correct Answers: $correctCount / $totalQuestions\n" +
                                "Score: $percentage%\n" +
                                "Result: Failed ❌\n\n" +
                                "You have used all $maxAttemptsPerDay attempts for today.\n" +
                                "Come back tomorrow to try again!"
                    }
                }

                val buttonText = if (passed) {
                    "Back to Module"
                } else if (remainingAttempts > 0) {
                    "Retry ($remainingAttempts left)"
                } else {
                    "OK"
                }

                AlertDialog.Builder(this)
                    .setTitle("Quiz Result")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(buttonText) { _, _ ->
                        if (passed || remainingAttempts <= 0) {
                            finish()
                        } else {
                            recreate()
                        }
                    }
                    .show()
            }
            .addOnFailureListener {
                // Fallback if we can't check attempts
                val message = if (passed) {
                    "Correct Answers: $correctCount / $totalQuestions\n" +
                            "Score: $percentage%\n" +
                            "Result: Passed ✅\n\n" +
                            "You can now go back and complete the module."
                } else {
                    "Correct Answers: $correctCount / $totalQuestions\n" +
                            "Score: $percentage%\n" +
                            "Result: Failed ❌\n\n" +
                            "You need at least $passThreshold% to unlock module completion."
                }

                AlertDialog.Builder(this)
                    .setTitle("Quiz Result")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(if (passed) "Back to Module" else "OK") { _, _ ->
                        finish()
                    }
                    .show()
            }
    }
}