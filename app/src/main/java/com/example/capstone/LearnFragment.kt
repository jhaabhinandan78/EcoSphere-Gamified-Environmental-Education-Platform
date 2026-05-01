package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.capstone.databinding.FragmentLearnBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LearnFragment : Fragment(), NavigationAware {

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!
    private var navigationController: NavigationController? = null
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    override fun setNavigationController(controller: NavigationController) {
        navigationController = controller
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadModuleProgress()
        setupClickListeners()
    }
    
    private fun loadModuleProgress() {
        val userId = auth.currentUser?.uid ?: return

        // Step 1: Get student's schoolId
        firestore.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                if (_binding == null) return@addOnSuccessListener

                val schoolId = userDoc.getString("schoolId") ?: ""

                if (schoolId.isEmpty()) {
                    if (_binding == null) return@addOnSuccessListener
                    binding.progressModules.progress = 0
                    binding.tvModuleProgress.text = "0/0"
                    binding.tvCompletedCount.text = "0 Completed"
                    binding.tvRemainingTime.text = "No school assigned"
                    binding.btnStartLearning.text = "Start Learning"
                    return@addOnSuccessListener
                }

                // Step 2: Get total modules for this school
                firestore.collection("Modules")
                    .whereEqualTo("schoolId", schoolId)
                    .get()
                    .addOnSuccessListener { modulesResult ->
                        if (_binding == null) return@addOnSuccessListener

                        val totalModules = modulesResult.size()

                        // Step 3: Get completed modules for this user
                        firestore.collection("Users")
                            .document(userId)
                            .collection("completions")
                            .get()
                            .addOnSuccessListener { completions ->
                                if (_binding == null) return@addOnSuccessListener

                                // Only count completions that match actual school modules
                                val schoolModuleIds = modulesResult.documents.map { it.id }.toSet()
                                val completedCount = completions.documents.count { it.id in schoolModuleIds }

                                val remainingModules = totalModules - completedCount
                                val progressPercentage = if (totalModules > 0)
                                    (completedCount * 100) / totalModules else 0

                                // Update progress bar
                                binding.progressModules.progress = progressPercentage

                                // Update progress text (e.g., "4/6")
                                binding.tvModuleProgress.text = "$completedCount/$totalModules"

                                // Update completed count text
                                binding.tvCompletedCount.text = "$completedCount Completed"

                                // Estimate reading time: ~200 words per minute, avg module ~600 words = ~3 min
                                // Plus quiz time: ~5 min per module
                                // Total: ~8 min per module (conservative estimate)
                                val minutesPerModule = 8
                                val remainingMinutes = remainingModules * minutesPerModule
                                val remainingHours = remainingMinutes / 60
                                val remainingMins = remainingMinutes % 60

                                val timeText = when {
                                    totalModules == 0 -> "No modules yet"
                                    remainingModules == 0 -> "All done! 🎉"
                                    remainingHours == 0 -> "~${remainingMins}m remaining"
                                    remainingMins == 0 -> "~${remainingHours}h remaining"
                                    else -> "~${remainingHours}h ${remainingMins}m remaining"
                                }
                                binding.tvRemainingTime.text = timeText

                                // Update button text based on progress
                                val buttonText = when {
                                    totalModules == 0 -> "No Modules Yet"
                                    completedCount == 0 -> "Start Learning"
                                    completedCount < totalModules -> "Continue Learning"
                                    else -> "Review Modules"
                                }
                                binding.btnStartLearning.text = buttonText
                            }
                            .addOnFailureListener {
                                if (_binding == null) return@addOnFailureListener
                                binding.progressModules.progress = 0
                                binding.tvModuleProgress.text = "0/$totalModules"
                                binding.tvCompletedCount.text = "0 Completed"
                                binding.tvRemainingTime.text = "~${totalModules / 2}h remaining"
                                binding.btnStartLearning.text = "Start Learning"
                            }
                    }
                    .addOnFailureListener {
                        if (_binding == null) return@addOnFailureListener
                        binding.progressModules.progress = 0
                        binding.tvModuleProgress.text = "0/0"
                        binding.tvCompletedCount.text = "0 Completed"
                        binding.tvRemainingTime.text = "~3h remaining"
                        binding.btnStartLearning.text = "Start Learning"
                    }
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                binding.progressModules.progress = 0
                binding.tvModuleProgress.text = "0/--"
                binding.tvCompletedCount.text = "0 Completed"
                binding.tvRemainingTime.text = "~3h remaining"
                binding.btnStartLearning.text = "Start Learning"
            }
    }

    private fun setupClickListeners() {
        // Start Learning button - navigate to modules list
        binding.btnStartLearning.setOnClickListener {
            navigationController?.navigateToActivity(StudentModulesActivity::class.java)
        }

        // Eco Assistant card - navigate to chatbot
        binding.cardEcoAssistant.setOnClickListener {
            navigationController?.navigateToActivity(EcoAssistantActivity::class.java)
        }

        // Ecosystem Game card - navigate to game
        binding.cardEcosystemGame.setOnClickListener {
            navigationController?.navigateToActivity(EcosystemGameActivity::class.java)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh progress when returning to learn tab
        loadModuleProgress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
