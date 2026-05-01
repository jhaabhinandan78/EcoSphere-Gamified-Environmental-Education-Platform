package com.example.capstone

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

/**
 * Seeds the Firestore database with eco-education modules and their quiz questions.
 * Call DataSeeder.seedModules() once (e.g., from AdminContentFragment or a one-time button).
 * Each module document is created with a fixed ID so re-running is idempotent.
 */
object DataSeeder {

    private val db = FirebaseFirestore.getInstance()

    data class SeedQuestion(
        val question: String,
        val option1: String,
        val option2: String,
        val option3: String,
        val option4: String,
        val correctAnswer: String
    )

    data class SeedModule(
        val id: String,
        val title: String,
        val description: String,
        val content: String,
        val points: Long,
        val order: Int,
        val questions: List<SeedQuestion>
    )

    data class SeedChallenge(
        val id: String,
        val title: String,
        val description: String,
        val points: Long,
        val type: String = "one-time",
        val active: Boolean = true
    )

    data class SeedStudent(
        val uid: String,
        val name: String,
        val email: String,
        val phone: String,
        val dob: String,
        val gender: String,
        val ecoPoints: Long,
        val role: String = "student"
    )

    private val modules = listOf(

        // ── Module 1 ──────────────────────────────────────────────────────────
        SeedModule(
            id = "module_eco_awareness",
            title = "Introduction to Eco-Awareness",
            description = "Discover what eco-awareness means and why it matters for our planet.",
            order = 1,
            content = """
Eco-awareness is the understanding of how human actions affect the natural environment and the commitment to making choices that protect it. It is the first step toward a more sustainable world.

🌍 What Is the Environment?
The environment includes everything around us — air, water, soil, plants, animals, and the climate. All these elements are interconnected in complex ecosystems that support all life on Earth.

⚠️ Why Is Eco-Awareness Important?
Human activity has dramatically altered the planet over the past 200 years:
• Global temperatures have risen by ~1.2°C since pre-industrial times
• Over 1 million species are currently threatened with extinction
• Forests cover 30% less of the Earth than they did 100 years ago
• Oceans absorb 30% of human CO₂ emissions, causing acidification
• Freshwater scarcity affects over 2 billion people

🔗 The Three Pillars of Sustainability
Sustainability balances three interconnected areas:
1. Environmental – protecting natural resources and ecosystems
2. Social – ensuring equity, health, and well-being for all people
3. Economic – enabling prosperity without depleting natural capital

🌱 The Eco-Aware Mindset
Being eco-aware means:
• Thinking about the long-term consequences of your choices
• Recognising that your actions have a global impact
• Valuing nature not just for its economic use but for its intrinsic worth
• Staying informed and sharing knowledge with others

🚀 Getting Started
You don't need to be perfect — every positive action counts. Start small:
• Learn about local environmental issues
• Reduce single-use plastics in your daily routine
• Support businesses and policies that prioritise sustainability
• Inspire others by leading by example
            """.trimIndent(),
            points = 15,
            questions = listOf(
                SeedQuestion(
                    "What does eco-awareness primarily involve?",
                    "Studying weather patterns",
                    "Understanding how human actions affect the environment",
                    "Learning about space exploration",
                    "Tracking animal migration",
                    "Understanding how human actions affect the environment"
                ),
                SeedQuestion(
                    "By approximately how much have global temperatures risen since pre-industrial times?",
                    "0.2°C",
                    "0.5°C",
                    "1.2°C",
                    "3.0°C",
                    "1.2°C"
                ),
                SeedQuestion(
                    "Which of the following is NOT one of the three pillars of sustainability?",
                    "Environmental",
                    "Social",
                    "Economic",
                    "Political",
                    "Political"
                ),
                SeedQuestion(
                    "What percentage of human CO₂ emissions do the oceans absorb?",
                    "10%",
                    "20%",
                    "30%",
                    "50%",
                    "30%"
                ),
                SeedQuestion(
                    "Which is the best first step toward eco-awareness?",
                    "Buying an electric car immediately",
                    "Installing solar panels on your roof",
                    "Learning about local environmental issues",
                    "Moving to a rural area",
                    "Learning about local environmental issues"
                )
            )
        ),

        // ── Module 2 ──────────────────────────────────────────────────────────
        SeedModule(
            id = "module_climate_change",
            title = "Climate Change Basics",
            description = "Understand the science behind climate change and its global impact.",
            order = 2,
            content = """
Climate change refers to long-term shifts in global temperatures and weather patterns. While some climate change is natural, since the mid-20th century human activities — especially burning fossil fuels — have been the main driver.

🌡️ The Greenhouse Effect
The Earth's atmosphere traps some of the Sun's heat, keeping the planet warm enough to support life. Greenhouse gases (GHGs) like CO₂, methane, and nitrous oxide act like a blanket. More GHGs = thicker blanket = warmer planet.

🔥 Key Causes
• Burning coal, oil, and natural gas for energy
• Deforestation (trees absorb CO₂)
• Industrial processes and agriculture
• Transportation (cars, planes, ships)

🌊 Key Effects
• Rising sea levels threatening coastal cities
• More frequent and intense extreme weather events
• Melting glaciers and Arctic ice
• Disruption of ecosystems and biodiversity loss
• Food and water insecurity in vulnerable regions

🎯 What Can We Do?
• Transition to renewable energy (solar, wind, hydro)
• Improve energy efficiency in buildings and transport
• Protect and restore forests
• Adopt sustainable agriculture practices
• Support climate-friendly policies
            """.trimIndent(),
            points = 25,
            questions = listOf(
                SeedQuestion(
                    "What is the primary human cause of climate change?",
                    "Volcanic eruptions",
                    "Burning fossil fuels",
                    "Ocean currents",
                    "Solar flares",
                    "Burning fossil fuels"
                ),
                SeedQuestion(
                    "Which gas is the most significant greenhouse gas produced by human activity?",
                    "Oxygen",
                    "Nitrogen",
                    "Carbon dioxide (CO₂)",
                    "Argon",
                    "Carbon dioxide (CO₂)"
                ),
                SeedQuestion(
                    "What does the greenhouse effect do?",
                    "Cools the Earth by reflecting sunlight",
                    "Traps heat in the Earth's atmosphere",
                    "Destroys the ozone layer",
                    "Creates acid rain",
                    "Traps heat in the Earth's atmosphere"
                ),
                SeedQuestion(
                    "Which of the following is a direct effect of climate change?",
                    "Increased snowfall globally",
                    "Falling sea levels",
                    "Rising sea levels",
                    "Cooler average temperatures",
                    "Rising sea levels"
                ),
                SeedQuestion(
                    "Which energy source helps reduce climate change?",
                    "Coal",
                    "Natural gas",
                    "Solar energy",
                    "Petroleum",
                    "Solar energy"
                )
            )
        ),

        // ── Module 3 ──────────────────────────────────────────────────────────
        SeedModule(
            id = "module_biodiversity",
            title = "Biodiversity & Ecosystems",
            description = "Explore why biodiversity matters and how ecosystems function.",
            order = 3,
            content = """
Biodiversity refers to the variety of life on Earth — from genes and species to entire ecosystems. It is the foundation of healthy ecosystems that provide us with clean air, water, food, and medicine.

🌿 Why Biodiversity Matters
• Ecosystem services: pollination, water purification, soil fertility
• Food security: diverse crops are more resilient to disease
• Medicine: over 50% of modern drugs are derived from natural compounds
• Climate regulation: forests and oceans absorb CO₂
• Cultural and recreational value

🦋 Threats to Biodiversity
1. Habitat destruction – deforestation, urbanisation, wetland drainage
2. Overexploitation – overfishing, poaching, unsustainable harvesting
3. Invasive species – non-native species outcompeting local wildlife
4. Pollution – pesticides, plastics, and chemical runoff
5. Climate change – shifting habitats and extreme weather

🌍 Ecosystems at Risk
• Tropical rainforests (home to ~50% of all species)
• Coral reefs (the "rainforests of the sea")
• Wetlands (critical for water filtration and flood control)
• Polar regions (rapidly changing due to warming)

✅ Conservation Actions
• Establish and expand protected areas (national parks, marine reserves)
• Restore degraded habitats
• Reduce pesticide and plastic use
• Support sustainable agriculture and fishing
• Raise awareness and education
            """.trimIndent(),
            points = 25,
            questions = listOf(
                SeedQuestion(
                    "What does biodiversity refer to?",
                    "The number of humans on Earth",
                    "The variety of life on Earth",
                    "The amount of rainfall in a region",
                    "The diversity of human cultures",
                    "The variety of life on Earth"
                ),
                SeedQuestion(
                    "Which of the following is NOT a threat to biodiversity?",
                    "Habitat destruction",
                    "Invasive species",
                    "Reforestation",
                    "Overexploitation",
                    "Reforestation"
                ),
                SeedQuestion(
                    "Coral reefs are sometimes called the 'rainforests of the sea' because they:",
                    "Produce large amounts of rainfall",
                    "Are found in tropical forests",
                    "Support an enormous variety of marine species",
                    "Absorb carbon dioxide from the air",
                    "Support an enormous variety of marine species"
                ),
                SeedQuestion(
                    "What percentage of modern drugs are estimated to be derived from natural compounds?",
                    "Over 10%",
                    "Over 25%",
                    "Over 50%",
                    "Over 90%",
                    "Over 50%"
                ),
                SeedQuestion(
                    "Which action best helps conserve biodiversity?",
                    "Expanding urban areas into forests",
                    "Introducing new species to ecosystems",
                    "Establishing protected areas like national parks",
                    "Increasing pesticide use in agriculture",
                    "Establishing protected areas like national parks"
                )
            )
        ),

        // ── Module 4 ──────────────────────────────────────────────────────────
        SeedModule(
            id = "module_waste_management",
            title = "Waste Management & Recycling",
            description = "Learn how proper waste management protects the environment.",
            order = 4,
            content = """
Every year, the world generates over 2 billion tonnes of solid waste. How we manage this waste has a huge impact on our environment, health, and climate.

♻️ The Waste Hierarchy (Best to Worst)
1. Refuse – avoid creating waste in the first place
2. Reduce – use less material
3. Reuse – use items multiple times
4. Recycle – process materials into new products
5. Recover – extract energy from waste
6. Dispose – landfill or incineration (last resort)

🗑️ Types of Waste
• Organic/biodegradable: food scraps, garden waste → compost
• Recyclable: paper, glass, metals, certain plastics
• Hazardous: batteries, chemicals, e-waste → special disposal
• Residual: what's left after recycling and composting

🌊 The Plastic Problem
• Over 8 million tonnes of plastic enter the ocean every year
• Plastic takes 400–1,000 years to decompose
• Microplastics have been found in drinking water, seafood, and human blood
• Solutions: reduce single-use plastics, improve collection systems, innovate materials

🌱 Composting
Composting organic waste returns nutrients to the soil, reduces landfill methane emissions, and improves soil health. Even a small home compost bin makes a difference.

💡 Tips for Better Waste Management
• Carry a reusable bag, bottle, and coffee cup
• Buy products with minimal packaging
• Separate recyclables from general waste
• Compost food scraps
• Donate or repair items instead of discarding them
            """.trimIndent(),
            points = 20,
            questions = listOf(
                SeedQuestion(
                    "According to the waste hierarchy, what is the BEST way to deal with waste?",
                    "Recycle it",
                    "Dispose of it in a landfill",
                    "Refuse to create it in the first place",
                    "Incinerate it for energy",
                    "Refuse to create it in the first place"
                ),
                SeedQuestion(
                    "Approximately how long does plastic take to decompose?",
                    "10–50 years",
                    "100–200 years",
                    "400–1,000 years",
                    "5,000+ years",
                    "400–1,000 years"
                ),
                SeedQuestion(
                    "What is composting?",
                    "Burning organic waste to generate electricity",
                    "Breaking down organic waste into nutrient-rich soil",
                    "Sorting plastics for recycling",
                    "Treating hazardous chemicals",
                    "Breaking down organic waste into nutrient-rich soil"
                ),
                SeedQuestion(
                    "Which of the following is classified as hazardous waste?",
                    "Vegetable peels",
                    "Newspaper",
                    "Glass bottles",
                    "Old batteries",
                    "Old batteries"
                ),
                SeedQuestion(
                    "How much plastic enters the ocean every year?",
                    "Over 800,000 tonnes",
                    "Over 8 million tonnes",
                    "Over 80 million tonnes",
                    "Over 800 million tonnes",
                    "Over 8 million tonnes"
                )
            )
        ),

        // ── Module 5 ──────────────────────────────────────────────────────────
        SeedModule(
            id = "module_renewable_energy",
            title = "Renewable Energy Sources",
            description = "Discover clean energy alternatives that power a sustainable future.",
            order = 5,
            content = """
Renewable energy comes from naturally replenishing sources that are virtually inexhaustible. Shifting from fossil fuels to renewables is one of the most powerful tools we have to fight climate change.

☀️ Solar Energy
• Photovoltaic (PV) panels convert sunlight directly into electricity
• Solar thermal systems use sunlight to heat water or buildings
• Fastest-growing energy source globally
• Works even on cloudy days (though less efficiently)

💨 Wind Energy
• Wind turbines convert kinetic energy of wind into electricity
• Onshore wind is one of the cheapest electricity sources today
• Offshore wind farms harness stronger, more consistent winds
• No water consumption, no air pollution during operation

💧 Hydropower
• Uses flowing or falling water to spin turbines
• Currently the largest source of renewable electricity worldwide
• Large dams can impact river ecosystems; run-of-river designs are gentler

🌋 Geothermal Energy
• Taps heat from within the Earth
• Provides reliable baseload power (unlike solar/wind which are intermittent)
• Best suited to geologically active regions (Iceland, Kenya, Philippines)

🌿 Biomass & Bioenergy
• Energy from organic materials: wood, crop residues, animal waste
• Can be carbon-neutral if managed sustainably
• Biogas from decomposing organic matter is a clean cooking fuel

⚡ Energy Storage
Batteries (especially lithium-ion) and pumped hydro storage are key to balancing intermittent renewables with demand.

🌍 Global Progress
Renewables now account for over 30% of global electricity generation and the share is growing rapidly every year.
            """.trimIndent(),
            points = 30,
            questions = listOf(
                SeedQuestion(
                    "What does a photovoltaic (PV) panel do?",
                    "Heats water using sunlight",
                    "Converts sunlight directly into electricity",
                    "Stores electricity in batteries",
                    "Converts wind into electricity",
                    "Converts sunlight directly into electricity"
                ),
                SeedQuestion(
                    "Which renewable energy source is currently the largest provider of renewable electricity worldwide?",
                    "Solar",
                    "Wind",
                    "Hydropower",
                    "Geothermal",
                    "Hydropower"
                ),
                SeedQuestion(
                    "What is a key advantage of geothermal energy over solar and wind?",
                    "It is cheaper to install",
                    "It produces more electricity",
                    "It provides reliable baseload power",
                    "It can be used anywhere in the world",
                    "It provides reliable baseload power"
                ),
                SeedQuestion(
                    "Biomass energy can be considered carbon-neutral when:",
                    "It is burned at very high temperatures",
                    "It is managed sustainably so new growth absorbs the CO₂ released",
                    "It is mixed with solar energy",
                    "It is used only in developing countries",
                    "It is managed sustainably so new growth absorbs the CO₂ released"
                ),
                SeedQuestion(
                    "What share of global electricity generation do renewables currently account for?",
                    "Over 10%",
                    "Over 20%",
                    "Over 30%",
                    "Over 60%",
                    "Over 30%"
                )
            )
        ),

        // ── Module 6 ──────────────────────────────────────────────────────────
        SeedModule(
            id = "module_sustainable_living",
            title = "Sustainable Living",
            description = "Practical steps to reduce your environmental footprint every day.",
            order = 6,
            content = """
Sustainable living means making choices that meet our needs today without compromising the ability of future generations to meet theirs. Small daily actions, multiplied by billions of people, create enormous change.

👣 Your Carbon Footprint
A carbon footprint is the total greenhouse gas emissions caused by an individual, organisation, or product. The average person in a high-income country emits around 10–15 tonnes of CO₂ per year. The global target is under 2 tonnes per person by 2050.

🍽️ Sustainable Food Choices
• Eat more plant-based foods — meat (especially beef) has a very high carbon footprint
• Buy local and seasonal produce to reduce transport emissions
• Reduce food waste — plan meals, store food properly, compost scraps
• Choose sustainably sourced seafood (look for MSC certification)

🚗 Sustainable Transport
• Walk, cycle, or use public transport whenever possible
• If you drive, consider an electric or hybrid vehicle
• Reduce air travel — one long-haul flight can equal months of driving emissions
• Combine trips to reduce unnecessary journeys

🏠 Sustainable Home
• Switch to LED lighting (uses 75% less energy than incandescent bulbs)
• Insulate your home to reduce heating/cooling needs
• Use energy-efficient appliances (look for energy star ratings)
• Install solar panels if possible
• Fix leaky taps — a dripping tap wastes thousands of litres per year

🛍️ Sustainable Shopping
• Buy less, buy better — choose quality over quantity
• Support ethical and sustainable brands
• Buy second-hand clothing and furniture
• Avoid fast fashion — the fashion industry produces 10% of global carbon emissions

💬 Advocacy
Individual action matters, but systemic change is essential. Vote for climate-friendly policies, support environmental organisations, and talk to friends and family about sustainability.
            """.trimIndent(),
            points = 20,
            questions = listOf(
                SeedQuestion(
                    "What is a carbon footprint?",
                    "The physical footprint left by carbon particles",
                    "The total greenhouse gas emissions caused by an individual or activity",
                    "The amount of carbon stored in forests",
                    "A measurement of air quality in cities",
                    "The total greenhouse gas emissions caused by an individual or activity"
                ),
                SeedQuestion(
                    "Which food choice generally has the LOWEST carbon footprint?",
                    "Beef burger",
                    "Lamb chops",
                    "Farmed salmon",
                    "Lentil soup",
                    "Lentil soup"
                ),
                SeedQuestion(
                    "LED lighting uses approximately how much less energy than incandescent bulbs?",
                    "25% less",
                    "50% less",
                    "75% less",
                    "90% less",
                    "75% less"
                ),
                SeedQuestion(
                    "What percentage of global carbon emissions does the fashion industry produce?",
                    "1%",
                    "5%",
                    "10%",
                    "20%",
                    "10%"
                ),
                SeedQuestion(
                    "Which of the following is the most sustainable transport option?",
                    "Driving a petrol car alone",
                    "Taking a long-haul flight",
                    "Cycling or walking",
                    "Riding a motorbike",
                    "Cycling or walking"
                )
            )
        )
    )

    // ═══════════════════════════════════════════════════════════════════════
    // CHALLENGES
    // ═══════════════════════════════════════════════════════════════════════

    private val challenges = listOf(

        SeedChallenge(
            id = "challenge_reusable_bag",
            title = "Bring Your Own Bag",
            description = "Use a reusable bag for shopping instead of plastic bags. Take a photo of yourself with your reusable bag at a store.",
            points = 10,
            type = "one-time",
            active = true
        ),

        SeedChallenge(
            id = "challenge_plant_tree",
            title = "Plant a Tree or Sapling",
            description = "Plant a tree, sapling, or even a small plant in your garden or community. Upload a photo of you planting it.",
            points = 25,
            type = "one-time",
            active = true
        ),

        SeedChallenge(
            id = "challenge_zero_waste_day",
            title = "Zero Waste Day",
            description = "Go an entire day without creating any single-use plastic waste. Document your day with photos of your waste-free meals and activities.",
            points = 20,
            type = "one-time",
            active = true
        ),

        SeedChallenge(
            id = "challenge_public_transport",
            title = "Use Public Transport",
            description = "Use public transportation, cycle, or walk instead of driving a personal vehicle for a day. Share a photo of your bus/train ticket or bicycle.",
            points = 15,
            type = "one-time",
            active = true
        ),

        SeedChallenge(
            id = "challenge_compost_bin",
            title = "Start Composting",
            description = "Set up a small compost bin at home for organic waste. Take a photo of your compost setup with some organic waste in it.",
            points = 30,
            type = "one-time",
            active = true
        ),

        SeedChallenge(
            id = "challenge_beach_cleanup",
            title = "Community Cleanup",
            description = "Participate in or organize a cleanup drive in your neighborhood, park, or beach. Upload a photo of the collected waste.",
            points = 35,
            type = "one-time",
            active = true
        ),

        SeedChallenge(
            id = "challenge_meatless_monday",
            title = "Meatless Monday",
            description = "Go completely plant-based for an entire day. Share photos of your delicious plant-based meals.",
            points = 15,
            type = "one-time",
            active = true
        ),

        SeedChallenge(
            id = "challenge_energy_audit",
            title = "Home Energy Audit",
            description = "Switch off all unnecessary lights and appliances for a day. Document your energy-saving actions with photos.",
            points = 20,
            type = "one-time",
            active = true
        )
    )

    // ═══════════════════════════════════════════════════════════════════════
    // SAMPLE STUDENTS (for testing admin dashboard)
    // ═══════════════════════════════════════════════════════════════════════

    private val sampleStudents = listOf(
        SeedStudent(
            uid = "student_alice_123",
            name = "Alice Johnson",
            email = "alice.johnson@email.com",
            phone = "+1-555-0101",
            dob = "1998-03-15",
            gender = "Female",
            ecoPoints = 485
        ),
        SeedStudent(
            uid = "student_bob_456",
            name = "Bob Chen",
            email = "bob.chen@email.com",
            phone = "+1-555-0102",
            dob = "1999-07-22",
            gender = "Male",
            ecoPoints = 320
        ),
        SeedStudent(
            uid = "student_carol_789",
            name = "Carol Martinez",
            email = "carol.martinez@email.com",
            phone = "+1-555-0103",
            dob = "2000-11-08",
            gender = "Female",
            ecoPoints = 275
        ),
        SeedStudent(
            uid = "student_david_012",
            name = "David Kim",
            email = "david.kim@email.com",
            phone = "+1-555-0104",
            dob = "1997-05-30",
            gender = "Male",
            ecoPoints = 150
        ),
        SeedStudent(
            uid = "student_emma_345",
            name = "Emma Thompson",
            email = "emma.thompson@email.com",
            phone = "+1-555-0105",
            dob = "2001-01-12",
            gender = "Female",
            ecoPoints = 95
        )
    )

    /**
     * Seeds sample students into Firestore for testing admin dashboard.
     * Uses set() with fixed document IDs so it is safe to call multiple times.
     * @param onComplete Called with (successCount, failCount) when all operations finish.
     */
    fun seedSampleStudents(onComplete: (success: Int, failed: Int) -> Unit = { _, _ -> }) {
        var pending = sampleStudents.size
        var successCount = 0
        var failCount = 0

        if (sampleStudents.isEmpty()) {
            android.util.Log.w("DataSeeder", "No sample students to seed")
            onComplete(0, 0)
            return
        }

        android.util.Log.d("DataSeeder", "Starting to seed ${sampleStudents.size} sample students...")

        for (student in sampleStudents) {
            val studentData = hashMapOf(
                "name" to student.name,
                "email" to student.email,
                "phone" to student.phone,
                "dob" to student.dob,
                "gender" to student.gender,
                "ecoPoints" to student.ecoPoints,
                "role" to student.role,
                "profilePictureUrl" to ""
            )

            db.collection("Users")
                .document(student.uid)
                .set(studentData)
                .addOnSuccessListener {
                    successCount++
                    pending--
                    android.util.Log.d("DataSeeder", "✅ Seeded student: ${student.name} ($successCount/${sampleStudents.size})")
                    if (pending == 0) {
                        android.util.Log.d("DataSeeder", "✅ All students seeded! Success: $successCount, Failed: $failCount")
                        onComplete(successCount, failCount)
                    }
                }
                .addOnFailureListener { e ->
                    failCount++
                    pending--
                    android.util.Log.e("DataSeeder", "❌ Failed to seed student: ${student.name}", e)
                    if (pending == 0) {
                        android.util.Log.d("DataSeeder", "Seeding complete. Success: $successCount, Failed: $failCount")
                        onComplete(successCount, failCount)
                    }
                }
        }
    }

    /**
     * Seeds all challenges into Firestore.
     * Uses set() with fixed document IDs so it is safe to call multiple times.
     * @param onComplete Called with (successCount, failCount) when all operations finish.
     */
    fun seedChallenges(onComplete: (success: Int, failed: Int) -> Unit = { _, _ -> }) {
        var pending = challenges.size
        var successCount = 0
        var failCount = 0

        if (challenges.isEmpty()) {
            android.util.Log.w("DataSeeder", "No challenges to seed")
            onComplete(0, 0)
            return
        }

        android.util.Log.d("DataSeeder", "Starting to seed ${challenges.size} challenges...")

        for (challenge in challenges) {
            val challengeData = hashMapOf(
                "id" to challenge.id,
                "title" to challenge.title,
                "description" to challenge.description,
                "points" to challenge.points,
                "type" to challenge.type,
                "active" to challenge.active
            )

            db.collection("Challenges")
                .document(challenge.id)
                .set(challengeData)
                .addOnSuccessListener {
                    successCount++
                    pending--
                    android.util.Log.d("DataSeeder", "✅ Seeded challenge: ${challenge.title} ($successCount/${challenges.size})")
                    if (pending == 0) {
                        android.util.Log.d("DataSeeder", "✅ All challenges seeded! Success: $successCount, Failed: $failCount")
                        onComplete(successCount, failCount)
                    }
                }
                .addOnFailureListener { e ->
                    failCount++
                    pending--
                    android.util.Log.e("DataSeeder", "❌ Failed to seed challenge: ${challenge.title}", e)
                    if (pending == 0) {
                        android.util.Log.d("DataSeeder", "Seeding complete. Success: $successCount, Failed: $failCount")
                        onComplete(successCount, failCount)
                    }
                }
        }
    }

    /**
     * Seeds all modules and their quiz questions into Firestore.
     * Uses set() with the fixed document ID so it is safe to call multiple times.
     * @param onComplete Called with (successCount, failCount) when all operations finish.
     */
    fun seedModules(onComplete: (success: Int, failed: Int) -> Unit = { _, _ -> }) {
        var pending = modules.size
        var successCount = 0
        var failCount = 0

        if (modules.isEmpty()) {
            android.util.Log.w("DataSeeder", "No modules to seed")
            onComplete(0, 0)
            return
        }

        android.util.Log.d("DataSeeder", "Starting to seed ${modules.size} modules...")

        for (module in modules) {
            val moduleData = hashMapOf(
                "id" to module.id,
                "title" to module.title,
                "description" to module.description,
                "content" to module.content,
                "points" to module.points,
                "order" to module.order.toLong()
            )

            db.collection("Modules")
                .document(module.id)
                .set(moduleData)
                .addOnSuccessListener {
                    android.util.Log.d("DataSeeder", "✅ Seeded module: ${module.title}")
                    seedQuestions(module) { qSuccess ->
                        if (qSuccess) {
                            successCount++
                            android.util.Log.d("DataSeeder", "✅ Seeded ${module.questions.size} questions for: ${module.title} ($successCount/${modules.size})")
                        } else {
                            failCount++
                            android.util.Log.e("DataSeeder", "❌ Failed to seed questions for: ${module.title}")
                        }
                        pending--
                        if (pending == 0) {
                            android.util.Log.d("DataSeeder", "✅ All modules seeded! Success: $successCount, Failed: $failCount")
                            onComplete(successCount, failCount)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    failCount++
                    pending--
                    android.util.Log.e("DataSeeder", "❌ Failed to seed module: ${module.title}", e)
                    if (pending == 0) {
                        android.util.Log.d("DataSeeder", "Seeding complete. Success: $successCount, Failed: $failCount")
                        onComplete(successCount, failCount)
                    }
                }
        }
    }

    private fun seedQuestions(module: SeedModule, onDone: (Boolean) -> Unit) {
        val questionsRef = db.collection("Modules")
            .document(module.id)
            .collection("questions")

        var pending = module.questions.size
        var allOk = true

        if (module.questions.isEmpty()) {
            android.util.Log.d("DataSeeder", "No questions for module: ${module.title}")
            onDone(true)
            return
        }

        module.questions.forEachIndexed { index, q ->
            val qData = hashMapOf(
                "id" to "q${index + 1}",
                "question" to q.question,
                "option1" to q.option1,
                "option2" to q.option2,
                "option3" to q.option3,
                "option4" to q.option4,
                "correctAnswer" to q.correctAnswer
            )

            questionsRef.document("q${index + 1}")
                .set(qData)
                .addOnSuccessListener {
                    pending--
                    if (pending == 0) onDone(allOk)
                }
                .addOnFailureListener { e ->
                    allOk = false
                    pending--
                    android.util.Log.e("DataSeeder", "❌ Failed to seed question ${index + 1} for ${module.title}", e)
                    if (pending == 0) onDone(allOk)
                }
        }
    }
    
    /**
     * Seeds all data (modules, challenges, sample students, and test schools) into Firestore.
     * This is a convenience method for setting up the complete test environment.
     * @param onComplete Called with (totalSuccess, totalFailed) when all operations finish.
     */
    fun seedAllData(onComplete: (success: Int, failed: Int) -> Unit = { _, _ -> }) {
        var totalSuccess = 0
        var totalFailed = 0
        var completedOperations = 0
        val totalOperations = 3 // modules, challenges, students (schools removed - use web portal)
        
        android.util.Log.d("DataSeeder", "========================================")
        android.util.Log.d("DataSeeder", "Starting to seed ALL data...")
        android.util.Log.d("DataSeeder", "========================================")
        
        // Seed modules
        seedModules { success, failed ->
            totalSuccess += success
            totalFailed += failed
            completedOperations++
            android.util.Log.d("DataSeeder", "Modules seeding complete ($completedOperations/$totalOperations)")
            if (completedOperations == totalOperations) {
                android.util.Log.d("DataSeeder", "========================================")
                android.util.Log.d("DataSeeder", "✅ ALL DATA SEEDED!")
                android.util.Log.d("DataSeeder", "Total Success: $totalSuccess, Total Failed: $totalFailed")
                android.util.Log.d("DataSeeder", "========================================")
                onComplete(totalSuccess, totalFailed)
            }
        }
        
        // Seed challenges
        seedChallenges { success, failed ->
            totalSuccess += success
            totalFailed += failed
            completedOperations++
            android.util.Log.d("DataSeeder", "Challenges seeding complete ($completedOperations/$totalOperations)")
            if (completedOperations == totalOperations) {
                android.util.Log.d("DataSeeder", "========================================")
                android.util.Log.d("DataSeeder", "✅ ALL DATA SEEDED!")
                android.util.Log.d("DataSeeder", "Total Success: $totalSuccess, Total Failed: $totalFailed")
                android.util.Log.d("DataSeeder", "========================================")
                onComplete(totalSuccess, totalFailed)
            }
        }
        
        // Seed sample students
        seedSampleStudents { success, failed ->
            totalSuccess += success
            totalFailed += failed
            completedOperations++
            android.util.Log.d("DataSeeder", "Students seeding complete ($completedOperations/$totalOperations)")
            if (completedOperations == totalOperations) {
                android.util.Log.d("DataSeeder", "========================================")
                android.util.Log.d("DataSeeder", "✅ ALL DATA SEEDED!")
                android.util.Log.d("DataSeeder", "Total Success: $totalSuccess, Total Failed: $totalFailed")
                android.util.Log.d("DataSeeder", "NOTE: Schools should be registered via web portal")
                android.util.Log.d("DataSeeder", "========================================")
                onComplete(totalSuccess, totalFailed)
            }
        }
    }
}
