package com.example.capstone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ActivityHelpFaqBinding
import com.google.android.material.card.MaterialCardView

class HelpFaqActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpFaqBinding

    data class FaqItem(
        val question: String,
        val answer: String,
        var isExpanded: Boolean = false
    )

    private val faqList = listOf(
        FaqItem(
            "How do I earn EcoPoints?",
            "You can earn EcoPoints by:\n• Completing learning modules\n• Passing module quizzes (80%+)\n• Completing eco challenges\n• Playing the Save the Ecosystem game"
        ),
        FaqItem(
            "What happens if I fail a quiz?",
            "Don't worry! You can retake quizzes as many times as needed. You need 80% to pass and unlock module completion. Each attempt helps you learn!"
        ),
        FaqItem(
            "How do challenges work?",
            "Challenges are real-world eco-actions. Complete the activity, take a photo as proof, add a note, and submit. You'll earn EcoPoints once submitted!"
        ),
        FaqItem(
            "Can I replay the Ecosystem Game?",
            "Yes! You can play the game multiple times to try different strategies and improve your score. Each playthrough earns EcoPoints based on your final ecosystem health."
        ),
        FaqItem(
            "How does the leaderboard work?",
            "The leaderboard ranks all students by total EcoPoints earned. Complete more modules and challenges to climb higher!"
        ),
        FaqItem(
            "What is the Eco Assistant?",
            "The Eco Assistant is an AI chatbot that answers your environmental questions instantly. Ask anything about climate change, recycling, sustainability, and more!"
        ),
        FaqItem(
            "How do I track my progress?",
            "Tap 'My Progress' on the dashboard to see:\n• Total EcoPoints\n• Modules completed\n• Challenges completed\n• Quiz pass rate\n• Recent achievements"
        ),
        FaqItem(
            "Can I complete a module without passing the quiz?",
            "No. You must pass the quiz (80%+) before you can complete a module and earn its EcoPoints. This ensures you've learned the material!"
        ),
        FaqItem(
            "What if I have a technical issue?",
            "Try these steps:\n1. Check your internet connection\n2. Restart the app\n3. Clear app cache in Settings\n4. If the problem persists, contact us via the Contact page"
        ),
        FaqItem(
            "How do I change my profile information?",
            "Go to Settings from the menu (three dots) on the dashboard. You can update your name and other preferences there."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.recyclerFaq.layoutManager = LinearLayoutManager(this)
        binding.recyclerFaq.adapter = FaqAdapter(faqList)
    }

    inner class FaqAdapter(private val items: List<FaqItem>) :
        RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

        inner class FaqViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: MaterialCardView = view.findViewById(R.id.cardFaq)
            val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
            val tvAnswer: TextView = view.findViewById(R.id.tvAnswer)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_faq, parent, false)
            return FaqViewHolder(view)
        }

        override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
            val item = items[position]
            holder.tvQuestion.text = item.question
            holder.tvAnswer.text = item.answer
            holder.tvAnswer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

            holder.card.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(position)
            }
        }

        override fun getItemCount() = items.size
    }
}
