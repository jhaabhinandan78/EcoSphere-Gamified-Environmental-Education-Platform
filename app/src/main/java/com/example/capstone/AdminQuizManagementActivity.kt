package com.example.capstone

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.capstone.databinding.ActivityAdminQuizManagementBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class AdminQuizManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminQuizManagementBinding
    private lateinit var quizAdapter: AdminQuizAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val quizQuestions = mutableListOf<AdminQuizQuestion>()
    private val allQuestions = mutableListOf<AdminQuizQuestion>() // master list for filtering
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var currentUserSchoolId: String = ""
    private var isAdmin: Boolean = false
    // Pre-selected module when launched from Module Management
    private var preSelectedModuleId: String? = null
    private var preSelectedModuleTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminQuizManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // Receive pre-selected module from Module Management
        preSelectedModuleId = intent.getStringExtra("moduleId")
        preSelectedModuleTitle = intent.getStringExtra("moduleTitle")

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        loadUserProfile()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            // Show module name in title if launched from a specific module
            title = if (preSelectedModuleTitle != null) {
                "Quiz: $preSelectedModuleTitle"
            } else {
                "Quiz Management"
            }
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(
            0xFF00897B.toInt(),
            0xFF4DB6AC.toInt(),
            0xFF00695C.toInt()
        )
        swipeRefreshLayout.setOnRefreshListener {
            android.util.Log.d("QuizMgmt", "Swipe refresh triggered")
            loadAllQuizQuestions()
        }
    }

    private fun setupRecyclerView() {
        quizAdapter = AdminQuizAdapter(
            questions = quizQuestions,
            onEditClick = { question -> showEditQuestionDialog(question) },
            onDeleteClick = { question -> showDeleteConfirmation(question) }
        )
        binding.recyclerViewQuestions.apply {
            layoutManager = LinearLayoutManager(this@AdminQuizManagementActivity)
            adapter = quizAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddQuestion.setOnClickListener {
            showAddQuestionDialog()
        }

        // Search/filter by module
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterQuestions(s?.toString() ?: "")
            }
        })
    }

    private fun loadUserProfile() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        
        if (userId == null) {
            Snackbar.make(binding.root, "❌ User not authenticated", Snackbar.LENGTH_LONG).show()
            return
        }
        
        firestore.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                currentUserSchoolId = document.getString("schoolId") ?: ""
                isAdmin = document.getString("role") == "admin"
                
                if (currentUserSchoolId.isEmpty() && !isAdmin) {
                    Snackbar.make(
                        binding.root,
                        "❌ Your account is not associated with a school. Please contact your administrator.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }
                
                android.util.Log.d("QuizMgmt", "User profile loaded: schoolId=$currentUserSchoolId, isAdmin=$isAdmin")
                loadAllQuizQuestions()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizMgmt", "Failed to load user profile", e)
                Snackbar.make(
                    binding.root,
                    "❌ Failed to load user profile: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).setAction("Retry") { loadUserProfile() }
                .show()
            }
    }

    private fun filterQuestions(query: String) {
        val filtered = if (query.isEmpty()) {
            allQuestions.toList()
        } else {
            allQuestions.filter { q ->
                q.question.contains(query, ignoreCase = true) ||
                q.moduleTitle.contains(query, ignoreCase = true)
            }
        }
        quizQuestions.clear()
        quizQuestions.addAll(filtered)
        quizAdapter.notifyDataSetChanged()

        binding.tvTotalQuestions.text = if (query.isEmpty()) {
            "Total Questions: ${allQuestions.size}"
        } else {
            "Showing ${filtered.size} of ${allQuestions.size} questions"
        }

        updateEmptyState(filtered.isEmpty(), query)
    }

    private fun loadAllQuizQuestions() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.recyclerViewQuestions.visibility = View.GONE
        binding.tvTotalQuestions.text = "Loading questions..."

        android.util.Log.d("QuizMgmt", "Loading all quiz questions...")

        // If launched from a specific module, only load that module's questions
        if (preSelectedModuleId != null) {
            loadQuestionsForModule(preSelectedModuleId!!, preSelectedModuleTitle ?: "Module")
            return
        }

        // Build query based on user role
        val query = if (isAdmin) {
            android.util.Log.d("QuizMgmt", "Loading questions from all modules (admin)")
            firestore.collection("Modules")
        } else {
            android.util.Log.d("QuizMgmt", "Loading questions from school modules: $currentUserSchoolId")
            firestore.collection("Modules")
                .whereEqualTo("schoolId", currentUserSchoolId)
        }

        query.get()
            .addOnSuccessListener { modules ->
                android.util.Log.d("QuizMgmt", "Found ${modules.size()} modules")

                val totalModules = modules.size()

                if (totalModules == 0) {
                    swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visibility = View.GONE
                    allQuestions.clear()
                    quizQuestions.clear()
                    quizAdapter.notifyDataSetChanged()
                    binding.tvTotalQuestions.text = "Total Questions: 0"
                    updateEmptyState(true, "")
                    return@addOnSuccessListener
                }

                val tempQuestions = mutableListOf<AdminQuizQuestion>()
                var processedModules = 0
                var failedModules = 0

                for (module in modules) {
                    val moduleId = module.id
                    val moduleTitle = module.getString("title") ?: "Unknown Module"

                    firestore.collection("Modules")
                        .document(moduleId)
                        .collection("questions")
                        .get()
                        .addOnSuccessListener { questions ->
                            android.util.Log.d("QuizMgmt", "Module '$moduleTitle': ${questions.size()} questions")

                            for (question in questions) {
                                try {
                                    val quizQuestion = AdminQuizQuestion(
                                        id = question.id,
                                        moduleId = moduleId,
                                        moduleTitle = moduleTitle,
                                        question = question.getString("question") ?: "",
                                        option1 = question.getString("option1") ?: "",
                                        option2 = question.getString("option2") ?: "",
                                        option3 = question.getString("option3") ?: "",
                                        option4 = question.getString("option4") ?: "",
                                        correctAnswer = question.getString("correctAnswer") ?: "",
                                        schoolId = question.getString("schoolId") ?: ""
                                    )
                                    if (quizQuestion.question.isNotEmpty()) {
                                        tempQuestions.add(quizQuestion)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("QuizMgmt", "Error parsing question ${question.id}", e)
                                }
                            }

                            processedModules++
                            if (processedModules == totalModules) {
                                onAllModulesLoaded(tempQuestions, failedModules)
                            }
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("QuizMgmt", "Failed to load questions for module $moduleId", e)
                            failedModules++
                            processedModules++
                            if (processedModules == totalModules) {
                                onAllModulesLoaded(tempQuestions, failedModules)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizMgmt", "Failed to load modules", e)
                swipeRefreshLayout.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                binding.tvTotalQuestions.text = "Failed to load"

                val msg = when {
                    e.message?.contains("PERMISSION_DENIED") == true -> "Permission denied. Check Firestore rules."
                    e.message?.contains("network") == true || e.message?.contains("UNAVAILABLE") == true -> "Network error. Check your connection."
                    else -> "Failed to load questions: ${e.message}"
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
                    .setAction("Retry") { loadAllQuizQuestions() }
                    .show()
            }
    }

    private fun loadQuestionsForModule(moduleId: String, moduleTitle: String) {
        firestore.collection("Modules")
            .document(moduleId)
            .collection("questions")
            .get()
            .addOnSuccessListener { questions ->
                swipeRefreshLayout.isRefreshing = false
                binding.progressBar.visibility = View.GONE

                val tempQuestions = mutableListOf<AdminQuizQuestion>()
                for (question in questions) {
                    try {
                        val quizQuestion = AdminQuizQuestion(
                            id = question.id,
                            moduleId = moduleId,
                            moduleTitle = moduleTitle,
                            question = question.getString("question") ?: "",
                            option1 = question.getString("option1") ?: "",
                            option2 = question.getString("option2") ?: "",
                            option3 = question.getString("option3") ?: "",
                            option4 = question.getString("option4") ?: "",
                            correctAnswer = question.getString("correctAnswer") ?: "",
                            schoolId = question.getString("schoolId") ?: ""
                        )
                        if (quizQuestion.question.isNotEmpty()) {
                            tempQuestions.add(quizQuestion)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("QuizMgmt", "Error parsing question ${question.id}", e)
                    }
                }
                onAllModulesLoaded(tempQuestions, 0)
            }
            .addOnFailureListener { e ->
                swipeRefreshLayout.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Failed to load questions: ${e.message}", Snackbar.LENGTH_LONG)
                    .setAction("Retry") { loadAllQuizQuestions() }
                    .show()
            }
    }

    private fun onAllModulesLoaded(tempQuestions: List<AdminQuizQuestion>, failedModules: Int) {
        swipeRefreshLayout.isRefreshing = false
        binding.progressBar.visibility = View.GONE

        // Sort by module title then question
        val sorted = tempQuestions.sortedWith(compareBy({ it.moduleTitle }, { it.question }))

        allQuestions.clear()
        allQuestions.addAll(sorted)
        quizQuestions.clear()
        quizQuestions.addAll(sorted)
        quizAdapter.notifyDataSetChanged()

        android.util.Log.d("QuizMgmt", "Loaded ${allQuestions.size} total questions, $failedModules modules failed")

        binding.tvTotalQuestions.text = "Total Questions: ${allQuestions.size}"

        if (failedModules > 0) {
            Snackbar.make(binding.root, "⚠️ $failedModules module(s) failed to load", Snackbar.LENGTH_LONG)
                .setAction("Retry") { loadAllQuizQuestions() }
                .show()
        }

        updateEmptyState(allQuestions.isEmpty(), "")
    }

    private fun updateEmptyState(isEmpty: Boolean, query: String) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerViewQuestions.visibility = View.GONE
            // Update empty state message based on context
            binding.tvEmptyStateMessage.text = if (query.isNotEmpty()) {
                "No questions found for \"$query\""
            } else {
                "No quiz questions found\nTap + to add your first question"
            }
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerViewQuestions.visibility = View.VISIBLE
        }
    }

    private fun showAddQuestionDialog() {
        // If launched from a specific module, skip module selection
        if (preSelectedModuleId != null) {
            showQuestionDetailsDialog(preSelectedModuleId!!, preSelectedModuleTitle ?: "Module", null)
            return
        }

        // Build query based on user role
        val query = if (isAdmin) {
            firestore.collection("Modules")
        } else {
            firestore.collection("Modules")
                .whereEqualTo("schoolId", currentUserSchoolId)
        }
        
        query.get()
            .addOnSuccessListener { modules ->
                if (modules.isEmpty) {
                    Snackbar.make(binding.root, "No modules found. Please seed modules first.", Snackbar.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val moduleNames = modules.map { it.getString("title") ?: it.id }.toTypedArray()
                val moduleIds = modules.map { it.id }.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle("Select Module")
                    .setItems(moduleNames) { _, which ->
                        showQuestionDetailsDialog(moduleIds[which], moduleNames[which], null)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, "Failed to load modules: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun showEditQuestionDialog(question: AdminQuizQuestion) {
        showQuestionDetailsDialog(question.moduleId, question.moduleTitle, question)
    }

    private fun showQuestionDetailsDialog(
        moduleId: String,
        moduleTitle: String,
        existingQuestion: AdminQuizQuestion?
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_question, null)

        val etQuestion = dialogView.findViewById<EditText>(R.id.etQuestion)
        val etOption1 = dialogView.findViewById<EditText>(R.id.etOption1)
        val etOption2 = dialogView.findViewById<EditText>(R.id.etOption2)
        val etOption3 = dialogView.findViewById<EditText>(R.id.etOption3)
        val etOption4 = dialogView.findViewById<EditText>(R.id.etOption4)
        val etCorrectAnswer = dialogView.findViewById<EditText>(R.id.etCorrectAnswer)

        // Pre-fill if editing
        existingQuestion?.let { q ->
            etQuestion.setText(q.question)
            etOption1.setText(q.option1)
            etOption2.setText(q.option2)
            etOption3.setText(q.option3)
            etOption4.setText(q.option4)
            etCorrectAnswer.setText(q.correctAnswer)
        }

        val title = if (existingQuestion == null) "Add Question — $moduleTitle" else "Edit Question"

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save", null) // null to override auto-dismiss
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // Override positive button to prevent auto-dismiss on validation failure
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val question = etQuestion.text.toString().trim()
            val option1 = etOption1.text.toString().trim()
            val option2 = etOption2.text.toString().trim()
            val option3 = etOption3.text.toString().trim()
            val option4 = etOption4.text.toString().trim()
            val correctAnswer = etCorrectAnswer.text.toString().trim()

            if (validateQuestionData(etQuestion, etOption1, etOption2, etOption3, etOption4, etCorrectAnswer,
                    question, option1, option2, option3, option4, correctAnswer)) {
                dialog.dismiss()
                if (existingQuestion == null) {
                    addNewQuestion(moduleId, question, option1, option2, option3, option4, correctAnswer)
                } else {
                    updateQuestion(existingQuestion, question, option1, option2, option3, option4, correctAnswer)
                }
            }
        }
    }

    private fun validateQuestionData(
        etQuestion: EditText, etOption1: EditText, etOption2: EditText,
        etOption3: EditText, etOption4: EditText, etCorrectAnswer: EditText,
        question: String, option1: String, option2: String,
        option3: String, option4: String, correctAnswer: String
    ): Boolean {
        // Clear previous errors
        etQuestion.error = null
        etOption1.error = null
        etOption2.error = null
        etOption3.error = null
        etOption4.error = null
        etCorrectAnswer.error = null

        return when {
            question.isEmpty() -> {
                etQuestion.error = "Question is required"
                etQuestion.requestFocus()
                false
            }
            option1.isEmpty() -> {
                etOption1.error = "Option A is required"
                etOption1.requestFocus()
                false
            }
            option2.isEmpty() -> {
                etOption2.error = "Option B is required"
                etOption2.requestFocus()
                false
            }
            option3.isEmpty() -> {
                etOption3.error = "Option C is required"
                etOption3.requestFocus()
                false
            }
            option4.isEmpty() -> {
                etOption4.error = "Option D is required"
                etOption4.requestFocus()
                false
            }
            correctAnswer.isEmpty() -> {
                etCorrectAnswer.error = "Correct answer is required"
                etCorrectAnswer.requestFocus()
                false
            }
            !listOf(option1, option2, option3, option4).contains(correctAnswer) -> {
                etCorrectAnswer.error = "Must exactly match one of the four options"
                etCorrectAnswer.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun addNewQuestion(
        moduleId: String,
        question: String,
        option1: String,
        option2: String,
        option3: String,
        option4: String,
        correctAnswer: String
    ) {
        val questionsRef = firestore.collection("Modules").document(moduleId).collection("questions")

        // Use auto-generated Firestore ID to avoid collision
        val newDocRef = questionsRef.document()

        val questionData = hashMapOf(
            "id" to newDocRef.id,
            "question" to question,
            "option1" to option1,
            "option2" to option2,
            "option3" to option3,
            "option4" to option4,
            "correctAnswer" to correctAnswer,
            "schoolId" to currentUserSchoolId
        )

        android.util.Log.d("QuizMgmt", "Adding new question to module $moduleId (schoolId=$currentUserSchoolId)")

        newDocRef.set(questionData)
            .addOnSuccessListener {
                android.util.Log.d("QuizMgmt", "✅ Question added: ${newDocRef.id}")
                Snackbar.make(binding.root, "✅ Question added successfully", Snackbar.LENGTH_SHORT).show()
                loadAllQuizQuestions()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizMgmt", "❌ Failed to add question", e)
                Snackbar.make(binding.root, "❌ Failed to add question: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun updateQuestion(
        existingQuestion: AdminQuizQuestion,
        question: String,
        option1: String,
        option2: String,
        option3: String,
        option4: String,
        correctAnswer: String
    ) {
        val questionData = hashMapOf(
            "id" to existingQuestion.id,
            "question" to question,
            "option1" to option1,
            "option2" to option2,
            "option3" to option3,
            "option4" to option4,
            "correctAnswer" to correctAnswer,
            "schoolId" to existingQuestion.schoolId
        )

        android.util.Log.d("QuizMgmt", "Updating question: ${existingQuestion.id}")

        firestore.collection("Modules")
            .document(existingQuestion.moduleId)
            .collection("questions")
            .document(existingQuestion.id)
            .set(questionData)
            .addOnSuccessListener {
                android.util.Log.d("QuizMgmt", "✅ Question updated: ${existingQuestion.id}")
                Snackbar.make(binding.root, "✅ Question updated successfully", Snackbar.LENGTH_SHORT).show()
                loadAllQuizQuestions()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizMgmt", "❌ Failed to update question", e)
                Snackbar.make(binding.root, "❌ Failed to update question: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun showDeleteConfirmation(question: AdminQuizQuestion) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Delete Question")
            .setMessage("Are you sure you want to delete this question?\n\n\"${question.question}\"\n\nFrom module: ${question.moduleTitle}")
            .setPositiveButton("Delete") { _, _ -> deleteQuestion(question) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteQuestion(question: AdminQuizQuestion) {
        android.util.Log.d("QuizMgmt", "Deleting question: ${question.id} from module ${question.moduleId}")

        firestore.collection("Modules")
            .document(question.moduleId)
            .collection("questions")
            .document(question.id)
            .delete()
            .addOnSuccessListener {
                android.util.Log.d("QuizMgmt", "✅ Question deleted: ${question.id}")
                Snackbar.make(binding.root, "✅ Question deleted successfully", Snackbar.LENGTH_SHORT).show()
                loadAllQuizQuestions()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizMgmt", "❌ Failed to delete question", e)
                Snackbar.make(binding.root, "❌ Failed to delete question: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class AdminQuizQuestion(
    val id: String,
    val moduleId: String,
    val moduleTitle: String,
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val correctAnswer: String,
    val schoolId: String = ""
)
