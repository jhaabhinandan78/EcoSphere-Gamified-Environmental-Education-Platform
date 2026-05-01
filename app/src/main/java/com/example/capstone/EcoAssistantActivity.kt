package com.example.capstone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ActivityEcoAssistantBinding
import java.text.SimpleDateFormat
import java.util.*

class EcoAssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEcoAssistantBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    data class ChatMessage(
        val text: String,
        val isUser: Boolean,
        val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEcoAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupClickListeners()
        
        // Welcome message
        addBotMessage("👋 Hi! I'm your Eco Assistant. Ask me anything about the environment, climate change, sustainability, or how to live more eco-friendly!")
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.recyclerChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerChat.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Quick question buttons
        binding.btnQuick1.setOnClickListener {
            processUserMessage("Why is the Amazon burning?")
        }
        binding.btnQuick2.setOnClickListener {
            processUserMessage("How do I reduce food waste?")
        }
        binding.btnQuick3.setOnClickListener {
            processUserMessage("What is climate change?")
        }
    }

    private fun sendMessage() {
        val userInput = binding.etMessage.text.toString().trim()
        if (userInput.isEmpty()) return

        binding.etMessage.text?.clear()
        processUserMessage(userInput)
    }

    private fun processUserMessage(userInput: String) {
        addUserMessage(userInput)
        
        // Simulate thinking delay
        binding.recyclerChat.postDelayed({
            val response = getResponse(userInput.lowercase())
            addBotMessage(response)
        }, 500)
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerChat.scrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(text: String) {
        messages.add(ChatMessage(text, false))
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerChat.scrollToPosition(messages.size - 1)
    }

    private fun getResponse(input: String): String {
        return when {
            // Amazon/Deforestation
            input.contains("amazon") && (input.contains("burn") || input.contains("fire")) -> 
                "🌳 The Amazon rainforest faces fires mainly due to:\n\n" +
                "1. **Deforestation** - Land is cleared for agriculture and cattle ranching\n" +
                "2. **Slash-and-burn farming** - Traditional method that can get out of control\n" +
                "3. **Climate change** - Drier conditions make fires more likely\n" +
                "4. **Illegal logging** - Weakens forest resilience\n\n" +
                "The Amazon produces 20% of Earth's oxygen and is home to millions of species. Protecting it is crucial for our planet!"

            // Food waste
            input.contains("food waste") || (input.contains("reduce") && input.contains("food")) ->
                "🍽️ Here's how to reduce food waste:\n\n" +
                "1. **Plan meals** - Make a shopping list and stick to it\n" +
                "2. **Store properly** - Keep fruits/veggies fresh longer\n" +
                "3. **Use leftovers** - Get creative with yesterday's meals\n" +
                "4. **Compost scraps** - Turn waste into nutrient-rich soil\n" +
                "5. **Check expiry dates** - Use 'first in, first out' method\n" +
                "6. **Freeze extras** - Preserve food before it spoils\n\n" +
                "💡 Did you know? 1/3 of all food produced globally is wasted!"

            // Climate change
            input.contains("climate change") || input.contains("global warming") ->
                "🌡️ Climate change is the long-term shift in Earth's temperatures and weather patterns.\n\n" +
                "**Main causes:**\n" +
                "• Burning fossil fuels (coal, oil, gas)\n" +
                "• Deforestation\n" +
                "• Industrial processes\n" +
                "• Agriculture (especially livestock)\n\n" +
                "**Effects:**\n" +
                "• Rising temperatures\n" +
                "• Melting ice caps\n" +
                "• Extreme weather events\n" +
                "• Rising sea levels\n" +
                "• Species extinction\n\n" +
                "We can fight it by using renewable energy, reducing waste, and protecting forests!"

            // Plastic pollution
            input.contains("plastic") && (input.contains("ocean") || input.contains("pollution")) ->
                "🌊 Plastic pollution is a major threat to our oceans:\n\n" +
                "• 8 million tons of plastic enter oceans yearly\n" +
                "• Takes 400-1000 years to decompose\n" +
                "• Harms marine life (turtles, fish, birds)\n" +
                "• Creates microplastics in our food chain\n\n" +
                "**What you can do:**\n" +
                "✓ Use reusable bags, bottles, and containers\n" +
                "✓ Avoid single-use plastics\n" +
                "✓ Recycle properly\n" +
                "✓ Support plastic-free products\n" +
                "✓ Participate in beach cleanups"

            // Recycling
            input.contains("recycle") || input.contains("recycling") ->
                "♻️ Recycling helps reduce waste and conserve resources!\n\n" +
                "**What can be recycled:**\n" +
                "✓ Paper and cardboard\n" +
                "✓ Glass bottles and jars\n" +
                "✓ Metal cans (aluminum, steel)\n" +
                "✓ Certain plastics (check numbers)\n\n" +
                "**Tips:**\n" +
                "• Clean items before recycling\n" +
                "• Remove caps and labels\n" +
                "• Don't bag recyclables\n" +
                "• Check local recycling rules\n\n" +
                "Remember: Reduce and Reuse come before Recycle!"

            // Renewable energy
            input.contains("renewable") || input.contains("solar") || input.contains("wind energy") ->
                "⚡ Renewable energy comes from natural sources that replenish:\n\n" +
                "**Types:**\n" +
                "☀️ Solar - From sunlight\n" +
                "💨 Wind - From wind turbines\n" +
                "💧 Hydro - From flowing water\n" +
                "🌋 Geothermal - From Earth's heat\n" +
                "🌿 Biomass - From organic materials\n\n" +
                "**Benefits:**\n" +
                "• Clean (no pollution)\n" +
                "• Infinite supply\n" +
                "• Reduces carbon emissions\n" +
                "• Creates jobs\n\n" +
                "Renewables now provide 30%+ of global electricity!"

            // Water conservation
            input.contains("water") && (input.contains("save") || input.contains("conserve")) ->
                "💧 Water conservation tips:\n\n" +
                "1. **Fix leaks** - A dripping tap wastes 1000s of liters/year\n" +
                "2. **Shorter showers** - Save 5-10 liters per minute\n" +
                "3. **Turn off taps** - While brushing teeth or soaping\n" +
                "4. **Full loads** - Run dishwasher/washing machine when full\n" +
                "5. **Collect rainwater** - Use for plants\n" +
                "6. **Water plants wisely** - Early morning or evening\n\n" +
                "Only 1% of Earth's water is drinkable - every drop counts!"

            // Carbon footprint
            input.contains("carbon footprint") || input.contains("reduce carbon") ->
                "👣 Your carbon footprint is the total greenhouse gases you produce.\n\n" +
                "**How to reduce it:**\n" +
                "🚶 Walk, bike, or use public transport\n" +
                "🌱 Eat more plant-based foods\n" +
                "💡 Use energy-efficient appliances\n" +
                "♻️ Reduce, reuse, recycle\n" +
                "🏠 Insulate your home\n" +
                "✈️ Fly less, vacation locally\n" +
                "🛍️ Buy local and seasonal\n" +
                "🌳 Support reforestation projects\n\n" +
                "Average person: 10-15 tons CO₂/year\n" +
                "Target: Under 2 tons by 2050"

            // Composting
            input.contains("compost") ->
                "🌱 Composting turns organic waste into nutrient-rich soil!\n\n" +
                "**What to compost:**\n" +
                "✓ Fruit and vegetable scraps\n" +
                "✓ Coffee grounds and tea bags\n" +
                "✓ Eggshells\n" +
                "✓ Yard waste (leaves, grass)\n" +
                "✓ Shredded paper\n\n" +
                "**Don't compost:**\n" +
                "✗ Meat or dairy\n" +
                "✗ Oils or fats\n" +
                "✗ Pet waste\n\n" +
                "**Benefits:**\n" +
                "• Reduces landfill waste\n" +
                "• Enriches soil\n" +
                "• Reduces methane emissions\n" +
                "• Saves money on fertilizer"

            // Biodiversity
            input.contains("biodiversity") || input.contains("species") && input.contains("extinct") ->
                "🦋 Biodiversity is the variety of life on Earth.\n\n" +
                "**Why it matters:**\n" +
                "• Provides food, medicine, materials\n" +
                "• Maintains ecosystems\n" +
                "• Regulates climate\n" +
                "• Purifies air and water\n\n" +
                "**Threats:**\n" +
                "• Habitat destruction\n" +
                "• Climate change\n" +
                "• Pollution\n" +
                "• Overfishing/hunting\n" +
                "• Invasive species\n\n" +
                "1 million species face extinction. We must protect them!"

            // Electric vehicles
            input.contains("electric") && input.contains("car") || input.contains("ev") ->
                "🚗 Electric vehicles (EVs) are better for the environment:\n\n" +
                "**Benefits:**\n" +
                "• Zero tailpipe emissions\n" +
                "• Lower carbon footprint (even with electricity)\n" +
                "• Quieter operation\n" +
                "• Lower running costs\n\n" +
                "**Challenges:**\n" +
                "• Higher upfront cost\n" +
                "• Charging infrastructure\n" +
                "• Battery production impact\n\n" +
                "As renewable energy grows, EVs become even cleaner!"

            // Sustainable fashion
            input.contains("fashion") || input.contains("clothes") && input.contains("sustainable") ->
                "👕 Sustainable fashion tips:\n\n" +
                "• Buy less, choose quality\n" +
                "• Shop second-hand/vintage\n" +
                "• Support ethical brands\n" +
                "• Repair instead of replace\n" +
                "• Donate or recycle old clothes\n" +
                "• Choose natural fibers\n" +
                "• Avoid fast fashion\n\n" +
                "Fashion industry = 10% of global carbon emissions!\n" +
                "Your choices matter."

            // General help
            input.contains("help") || input.contains("what can you") ->
                "🤖 I can help you with:\n\n" +
                "🌍 Climate change and global warming\n" +
                "🌳 Deforestation and forests\n" +
                "♻️ Recycling and waste management\n" +
                "💧 Water conservation\n" +
                "⚡ Renewable energy\n" +
                "🌊 Ocean pollution\n" +
                "🍽️ Food waste reduction\n" +
                "👣 Carbon footprint\n" +
                "🌱 Sustainable living tips\n" +
                "🦋 Biodiversity\n\n" +
                "Just ask me anything about the environment!"

            // Thank you
            input.contains("thank") ->
                "You're welcome! 🌿 Keep learning and taking action for our planet. Every small step counts!"

            // Greetings
            input.contains("hi") || input.contains("hello") || input.contains("hey") ->
                "Hello! 👋 I'm here to answer your environmental questions. What would you like to know?"

            // Default response
            else ->
                "🤔 I'm not sure about that specific question, but I can help with:\n\n" +
                "• Climate change\n" +
                "• Recycling & waste\n" +
                "• Water & energy conservation\n" +
                "• Sustainable living\n" +
                "• Pollution & biodiversity\n\n" +
                "Try asking: 'How do I reduce food waste?' or 'What is climate change?'"
        }
    }

    class ChatAdapter(private val messages: List<ChatMessage>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_TYPE_USER = 1
        private val VIEW_TYPE_BOT = 2

        override fun getItemViewType(position: Int): Int {
            return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_USER) {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_user, parent, false)
                UserViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_bot, parent, false)
                BotViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = messages[position]
            if (holder is UserViewHolder) {
                holder.bind(message)
            } else if (holder is BotViewHolder) {
                holder.bind(message)
            }
        }

        override fun getItemCount() = messages.size

        class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
            private val tvTime: TextView = view.findViewById(R.id.tvTime)

            fun bind(message: ChatMessage) {
                tvMessage.text = message.text
                tvTime.text = message.timestamp
            }
        }

        class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
            private val tvTime: TextView = view.findViewById(R.id.tvTime)

            fun bind(message: ChatMessage) {
                tvMessage.text = message.text
                tvTime.text = message.timestamp
            }
        }
    }
}
