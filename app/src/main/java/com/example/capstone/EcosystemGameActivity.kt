package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.databinding.ActivityEcosystemGameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class EcosystemGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEcosystemGameBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Game state
    private var ecosystemHealth = 50
    private var forestCoverage = 50
    private var waterQuality = 50
    private var wildlifePopulation = 50
    private var pollutionLevel = 50
    private var currentScenario = 0
    private var totalScore = 0

    private val scenarios = listOf(
        Scenario(
            "🏭 Industrial Pollution",
            "A factory wants to set up near a river. They promise jobs but may pollute the water.",
            "Allow factory with strict regulations",
            "Reject factory completely",
            "Suggest eco-friendly alternatives",
            impact1 = Impact(-10, 0, -15, -5, 15),
            impact2 = Impact(5, 0, 10, 5, -10),
            impact3 = Impact(10, 5, 5, 10, -5)
        ),
        Scenario(
            "🌳 Deforestation Crisis",
            "Loggers want to cut down 30% of the forest for timber. Local communities depend on the forest.",
            "Allow limited logging",
            "Ban all logging",
            "Promote sustainable forestry",
            impact1 = Impact(-5, -20, -5, -15, 5),
            impact2 = Impact(10, 15, 5, 10, -5),
            impact3 = Impact(15, 10, 10, 15, -10)
        ),
        Scenario(
            "🐾 Endangered Species",
            "A rare species is on the brink of extinction. Conservation requires significant resources.",
            "Minimal protection",
            "Full conservation program",
            "Community-based protection",
            impact1 = Impact(-5, 0, 0, -10, 5),
            impact2 = Impact(15, 10, 5, 20, -10),
            impact3 = Impact(10, 5, 5, 15, -5)
        ),
        Scenario(
            "💧 Water Scarcity",
            "The region faces water shortage. Different groups want water for different purposes.",
            "Prioritize agriculture",
            "Prioritize drinking water",
            "Implement water conservation",
            impact1 = Impact(5, -5, -10, 0, 5),
            impact2 = Impact(10, 0, 5, 5, 0),
            impact3 = Impact(15, 5, 15, 10, -10)
        ),
        Scenario(
            "♻️ Waste Management",
            "The city generates tons of waste daily. Current landfills are full.",
            "Build new landfill",
            "Implement recycling program",
            "Waste-to-energy plant",
            impact1 = Impact(-5, -5, -10, -5, 10),
            impact2 = Impact(10, 5, 5, 5, -15),
            impact3 = Impact(15, 5, 10, 10, -10)
        ),
        Scenario(
            "🌊 Ocean Pollution",
            "Plastic waste is killing marine life. Cleanup is expensive but necessary.",
            "Minimal cleanup",
            "Full cleanup campaign",
            "Ban single-use plastics",
            impact1 = Impact(-5, 0, -5, -10, 5),
            impact2 = Impact(10, 5, 15, 15, -15),
            impact3 = Impact(15, 5, 10, 10, -20)
        ),
        Scenario(
            "🌾 Sustainable Agriculture",
            "Farmers use chemical pesticides that harm the ecosystem but increase yield.",
            "Continue current practices",
            "Ban all chemicals",
            "Promote organic farming",
            impact1 = Impact(-10, -5, -15, -10, 15),
            impact2 = Impact(5, 5, 10, 5, -10),
            impact3 = Impact(15, 10, 15, 15, -15)
        ),
        Scenario(
            "🏙️ Urban Expansion",
            "The city needs to expand. This requires converting green spaces into buildings.",
            "Allow unrestricted expansion",
            "Preserve all green spaces",
            "Smart city planning",
            impact1 = Impact(-15, -20, -10, -15, 20),
            impact2 = Impact(10, 15, 5, 10, -10),
            impact3 = Impact(15, 10, 10, 15, -5)
        )
    )

    data class Scenario(
        val title: String,
        val description: String,
        val option1: String,
        val option2: String,
        val option3: String,
        val impact1: Impact,
        val impact2: Impact,
        val impact3: Impact
    )

    data class Impact(
        val health: Int,
        val forest: Int,
        val water: Int,
        val wildlife: Int,
        val pollution: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEcosystemGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnOption1.setOnClickListener { makeDecision(scenarios[currentScenario].impact1) }
        binding.btnOption2.setOnClickListener { makeDecision(scenarios[currentScenario].impact2) }
        binding.btnOption3.setOnClickListener { makeDecision(scenarios[currentScenario].impact3) }

        loadScenario()
        updateUI()
    }

    private fun loadScenario() {
        if (currentScenario >= scenarios.size) {
            endGame()
            return
        }

        val scenario = scenarios[currentScenario]
        binding.tvScenarioNumber.text = "Scenario ${currentScenario + 1}/${scenarios.size}"
        binding.tvScenarioTitle.text = scenario.title
        binding.tvScenarioDescription.text = scenario.description
        binding.btnOption1.text = scenario.option1
        binding.btnOption2.text = scenario.option2
        binding.btnOption3.text = scenario.option3
    }

    private fun makeDecision(impact: Impact) {
        // Apply impacts
        ecosystemHealth = (ecosystemHealth + impact.health).coerceIn(0, 100)
        forestCoverage = (forestCoverage + impact.forest).coerceIn(0, 100)
        waterQuality = (waterQuality + impact.water).coerceIn(0, 100)
        wildlifePopulation = (wildlifePopulation + impact.wildlife).coerceIn(0, 100)
        pollutionLevel = (pollutionLevel + impact.pollution).coerceIn(0, 100)

        // Calculate score for this decision
        val decisionScore = (impact.health + impact.forest + impact.water + impact.wildlife - impact.pollution)
        totalScore += decisionScore

        // Show impact
        showImpactDialog(impact)

        // Move to next scenario
        currentScenario++
        loadScenario()
        updateUI()
    }

    private fun showImpactDialog(impact: Impact) {
        val message = buildString {
            append("Impact of your decision:\n\n")
            if (impact.health != 0) append("Ecosystem Health: ${if (impact.health > 0) "+" else ""}${impact.health}\n")
            if (impact.forest != 0) append("Forest Coverage: ${if (impact.forest > 0) "+" else ""}${impact.forest}\n")
            if (impact.water != 0) append("Water Quality: ${if (impact.water > 0) "+" else ""}${impact.water}\n")
            if (impact.wildlife != 0) append("Wildlife: ${if (impact.wildlife > 0) "+" else ""}${impact.wildlife}\n")
            if (impact.pollution != 0) append("Pollution: ${if (impact.pollution > 0) "+" else ""}${impact.pollution}\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Decision Made!")
            .setMessage(message)
            .setPositiveButton("Continue") { _, _ -> }
            .show()
    }

    private fun updateUI() {
        binding.progressHealth.progress = ecosystemHealth
        binding.tvHealthValue.text = "$ecosystemHealth%"

        binding.progressForest.progress = forestCoverage
        binding.tvForestValue.text = "$forestCoverage%"

        binding.progressWater.progress = waterQuality
        binding.tvWaterValue.text = "$waterQuality%"

        binding.progressWildlife.progress = wildlifePopulation
        binding.tvWildlifeValue.text = "$wildlifePopulation%"

        binding.progressPollution.progress = 100 - pollutionLevel
        binding.tvPollutionValue.text = "$pollutionLevel%"

        binding.tvScore.text = "$totalScore"
    }

    private fun endGame() {
        binding.cardScenario.visibility = View.GONE

        val finalScore = (ecosystemHealth + forestCoverage + waterQuality + wildlifePopulation + (100 - pollutionLevel)) / 5
        val ecoPointsEarned = (finalScore / 10).toLong()

        val resultMessage = when {
            finalScore >= 80 -> "🏆 Excellent! You're an Eco Champion!\n\nYou made wise decisions that protected the ecosystem."
            finalScore >= 60 -> "🌟 Good Job! The ecosystem is recovering.\n\nYour decisions helped balance development and conservation."
            finalScore >= 40 -> "🌿 Fair Effort. The ecosystem survived.\n\nSome decisions could have been better for the environment."
            else -> "⚠️ The ecosystem is struggling.\n\nYour decisions prioritized short-term gains over long-term sustainability."
        }

        AlertDialog.Builder(this)
            .setTitle("Game Over!")
            .setMessage("$resultMessage\n\nFinal Score: $finalScore/100\nEcoPoints Earned: +$ecoPointsEarned")
            .setCancelable(false)
            .setPositiveButton("Claim Rewards") { _, _ ->
                saveGameResult(ecoPointsEarned)
            }
            .show()
    }

    private fun saveGameResult(points: Long) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("Users").document(uid)
            .update("ecoPoints", FieldValue.increment(points))
            .addOnSuccessListener {
                Toast.makeText(this, "🌱 +$points EcoPoints earned!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save score", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
