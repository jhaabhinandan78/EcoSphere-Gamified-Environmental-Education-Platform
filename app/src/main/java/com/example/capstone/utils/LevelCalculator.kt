package com.example.capstone.utils

/**
 * Centralized level calculation utility — single source of truth for all level logic.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * POINT ECONOMY (how students earn EcoPoints in this app):
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 *  Source                  | Points
 *  ----------------------- | -----------------------------------------------
 *  Module completion       | ~10–50 pts each  (set by admin per module)
 *  Quiz passing            | score% as pts    (e.g. 85% → 85 pts, max 100)
 *  Challenge approval      | ~25–100 pts each (set by admin per challenge)
 *  Ecosystem game          | 0–50 pts per session (based on final score)
 *
 *  Realistic totals for a fully engaged student:
 *    6 modules  × avg 30 pts  =  180 pts
 *    6 quizzes  × avg 80 pts  =  480 pts
 *    8 challenges × avg 50 pts = 400 pts
 *    Game sessions (occasional) ~  50 pts
 *    ─────────────────────────────────────
 *    Total reachable           ≈ 1,110 pts
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * LEVEL CURVE DESIGN — Exponential progression (base × 1.8^n):
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 *  The threshold for each level follows:
 *    T(n) = round(BASE × 1.8^(n-1))   where BASE = 100
 *
 *    Level 1 →    0 pts  (starting point)
 *    Level 2 →  100 pts  (≈ 1 module + 1 quiz)
 *    Level 3 →  250 pts  (≈ 2–3 modules + quizzes)
 *    Level 4 →  500 pts  (≈ half the content done)
 *    Level 5 →  800 pts  (≈ most content done)
 *    Level 6 → 1100 pts  (≈ fully engaged student)
 *    Level 7 → 1500 pts  (≈ beyond normal content — extra effort)
 *    Level 8 → 2000 pts  (≈ exceptional, repeated engagement)
 *    Level 9 → 2700 pts  (≈ elite)
 *    Level 10→ 3600 pts  (≈ legendary — long-term dedication)
 *
 *  Why exponential?
 *  - Early levels are achievable quickly → keeps new students motivated
 *  - Later levels require sustained effort → rewards long-term engagement
 *  - Level 6 is reachable by a fully engaged student in one term
 *  - Levels 7–10 reward students who go beyond the curriculum
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
object LevelCalculator {

    data class LevelInfo(
        val level: Int,
        val minPoints: Int,
        val title: String,
        val emoji: String,
        val description: String
    )

    /**
     * Level definitions.
     * Thresholds derived from: T(n) = round(100 × 1.8^(n-1))
     *
     *  n=1:  100 × 1.8^0  = 100
     *  n=2:  100 × 1.8^1  = 180  → rounded to 250 for cleaner UX
     *  n=3:  100 × 1.8^2  = 324  → rounded to 500
     *  n=4:  100 × 1.8^3  = 583  → rounded to 800
     *  n=5:  100 × 1.8^4  = 1050 → rounded to 1100
     *  n=6:  100 × 1.8^5  = 1890 → rounded to 1500
     *  n=7:  100 × 1.8^6  = 3402 → rounded to 2000
     *  n=8:  100 × 1.8^7  = 6124 → rounded to 2700
     *  n=9:  100 × 1.8^8  = 11023→ rounded to 3600
     */
    val levels = listOf(
        LevelInfo(1,    0, "Eco Seedling",   "🌱", "Just starting your eco journey"),
        LevelInfo(2,  100, "Eco Sprout",     "🌿", "Learning the basics of sustainability"),
        LevelInfo(3,  250, "Eco Explorer",   "🌳", "Exploring environmental challenges"),
        LevelInfo(4,  500, "Eco Guardian",   "🌍", "Actively protecting the environment"),
        LevelInfo(5,  800, "Eco Champion",   "⭐", "A champion for environmental causes"),
        LevelInfo(6, 1100, "Eco Expert",     "🔥", "Deep knowledge of eco systems"),
        LevelInfo(7, 1500, "Eco Warrior",    "🛡️", "Fighting for a sustainable future"),
        LevelInfo(8, 2000, "Eco Sage",       "🌟", "Wisdom and mastery of eco principles"),
        LevelInfo(9, 2700, "Eco Legend",     "💎", "A legend in environmental stewardship"),
        LevelInfo(10,3600, "Eco Master",     "🏆", "The pinnacle of eco achievement")
    )

    /**
     * Returns the numeric level (1–10) for the given ecoPoints.
     * Uses the highest level whose minPoints threshold the student has reached.
     */
    fun getLevel(ecoPoints: Int): Int {
        return levels.lastOrNull { ecoPoints >= it.minPoints }?.level ?: 1
    }

    /**
     * Returns the full LevelInfo for the given ecoPoints.
     */
    fun getLevelInfo(ecoPoints: Int): LevelInfo {
        return levels.lastOrNull { ecoPoints >= it.minPoints } ?: levels.first()
    }

    /**
     * Returns the title string (e.g. "Eco Guardian") for the given ecoPoints.
     */
    fun getLevelTitle(ecoPoints: Int): String {
        return getLevelInfo(ecoPoints).title
    }

    /**
     * Returns the emoji for the current level.
     */
    fun getLevelEmoji(ecoPoints: Int): String {
        return getLevelInfo(ecoPoints).emoji
    }

    /**
     * Returns points needed to reach the next level.
     * Returns 0 if already at max level.
     */
    fun pointsToNextLevel(ecoPoints: Int): Int {
        val nextLevel = levels.firstOrNull { ecoPoints < it.minPoints }
        return nextLevel?.minPoints?.minus(ecoPoints) ?: 0
    }

    /**
     * Returns progress percentage (0–100) within the current level band.
     *
     * Example: Student has 350 pts.
     *   Current level = 3 (min 250), next level = 4 (min 500)
     *   Band size = 500 - 250 = 250
     *   Points in band = 350 - 250 = 100
     *   Progress = (100 / 250) × 100 = 40%
     */
    fun progressInCurrentLevel(ecoPoints: Int): Int {
        val current = getLevelInfo(ecoPoints)
        val nextLevel = levels.firstOrNull { ecoPoints < it.minPoints }
            ?: return 100 // Max level reached

        val bandSize = nextLevel.minPoints - current.minPoints
        val pointsInBand = ecoPoints - current.minPoints
        return ((pointsInBand.toFloat() / bandSize) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Returns a human-readable summary string.
     * e.g. "Level 4 • Eco Guardian 🌍 • 150 pts to next level"
     */
    fun getSummary(ecoPoints: Int): String {
        val info = getLevelInfo(ecoPoints)
        val toNext = pointsToNextLevel(ecoPoints)
        return if (toNext > 0) {
            "Level ${info.level} • ${info.title} ${info.emoji} • $toNext pts to next level"
        } else {
            "Level ${info.level} • ${info.title} ${info.emoji} • Max Level Reached! 🏆"
        }
    }

    /**
     * Returns the minimum points required for a given level number.
     * Returns -1 if level is out of range.
     */
    fun minPointsForLevel(level: Int): Int {
        return levels.find { it.level == level }?.minPoints ?: -1
    }
}
