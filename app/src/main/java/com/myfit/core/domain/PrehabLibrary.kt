package com.myfit.core.domain

data class PrehabMove(
    val name: String,
    val sets: Int,
    val repsOrSeconds: String,
    val cue: String
)

object PrehabLibrary {

    /** Returns up to 4 mobility moves for a given primary muscle name. */
    fun movesFor(muscle: String): List<PrehabMove> =
        library[muscle.lowercase()] ?: library["general"] ?: emptyList()

    private val library: Map<String, List<PrehabMove>> = mapOf(

        "quadriceps" to listOf(
            PrehabMove("Leg Swing",             2, "15 reps/side", "Tall posture, full ROM hip flexion"),
            PrehabMove("Couch Stretch",         2, "60s/side",     "Posterior pelvic tilt, squeeze glute"),
            PrehabMove("Walking Lunge",         2, "10 reps",      "Knee tracks over 2nd toe"),
            PrehabMove("Box Step-Up",           2, "8 reps/side",  "Drive through heel, control descent")
        ),

        "hamstrings" to listOf(
            PrehabMove("Standing Hip Hinge",    2, "10 reps",      "Neutral spine, feel hamstring load"),
            PrehabMove("Nordic Curl Negative",  2, "5 reps",       "3-count eccentric, catch at 45 deg"),
            PrehabMove("90/90 Hip Switch",      2, "8 reps/side",  "Sit tall, rotate pelvis over rear leg"),
            PrehabMove("SL RDL Bodyweight",     2, "8 reps/side",  "Hip hinge, maintain balance")
        ),

        "glutes" to listOf(
            PrehabMove("Glute Bridge",          2, "15 reps",      "Drive hips, hold squeeze 2s at top"),
            PrehabMove("Clamshell",             2, "12 reps/side", "Resist band, keep pelvis still"),
            PrehabMove("Lateral Band Walk",     2, "10 steps/side","Tension throughout, no hip drop"),
            PrehabMove("Hip 90/90 Stretch",     2, "45s/side",     "Tall spine, breathe into restriction")
        ),

        "chest" to listOf(
            PrehabMove("Band Pull-Apart",       3, "20 reps",      "Retract scapulae, control return"),
            PrehabMove("Doorway Pec Stretch",   2, "45s/side",     "Slight lean, feel anterior delt"),
            PrehabMove("Scapular Push-Up",      2, "10 reps",      "Protract/retract, elbows locked"),
            PrehabMove("Arm Circle",            2, "10 + 10",      "Full shoulder ROM, controlled")
        ),

        "lats" to listOf(
            PrehabMove("Dead Hang",             2, "30s",          "Passive hang, decompress spine"),
            PrehabMove("Cat-Camel",             2, "10 cycles",    "Full flexion/extension through T-spine"),
            PrehabMove("Overhead Lat Stretch",  2, "45s/side",     "Side-bend, reach through fingertips"),
            PrehabMove("Band Straight-Arm PD",  2, "12 reps",      "Initiate with lat, no elbow bend")
        ),

        "front_delt" to listOf(
            PrehabMove("Shoulder Pass-Through", 2, "10 reps",      "Narrow grip, full overhead arc"),
            PrehabMove("Wall Slide",            2, "10 reps",      "Maintain contact, upward rotation"),
            PrehabMove("Cuban Rotation",        2, "10 reps",      "90/90 start, rotate to vertical"),
            PrehabMove("Band Dislocate",        2, "8 reps",       "Wide grip, control the stretch")
        ),

        "rear_delt" to listOf(
            PrehabMove("Face Pull Light",       3, "15 reps",      "External rotation at top, thumbs back"),
            PrehabMove("Prone Y-T-W",           2, "8 each",       "Chest on bench, full scapular movement"),
            PrehabMove("Cross-Body Stretch",    2, "45s/side",     "Pull elbow across, posterior capsule"),
            PrehabMove("Band External Rot",     2, "15 reps/side", "Elbow at 90 deg, rotate out only")
        ),

        "biceps" to listOf(
            PrehabMove("Supination Curl Empty", 2, "12 reps",      "Full supination at top, control"),
            PrehabMove("Wrist Circles",         1, "20 each way",  "Full ROM, slow and controlled"),
            PrehabMove("Wall Bicep Stretch",    2, "30s/side",     "Arm behind, rotate body away")
        ),

        "triceps" to listOf(
            PrehabMove("Overhead Tri Stretch",  2, "45s/side",     "Elbow behind ear, tall posture"),
            PrehabMove("Diamond Push-Up Slow",  2, "8 reps",       "3-count descent, elbows close"),
            PrehabMove("Band Pushdown Light",   2, "15 reps",      "Elbow at side, full extension")
        ),

        "core" to listOf(
            PrehabMove("Dead Bug",              2, "8 reps/side",  "Lumbar flat, exhale on extension"),
            PrehabMove("McGill Curl-Up",        2, "8 reps",       "Hands under lumbar, minimal range"),
            PrehabMove("Pallof Press",          2, "10 reps/side", "No trunk rotation, squeeze core"),
            PrehabMove("Bird Dog",              2, "8 reps/side",  "Neutral spine, slow and controlled")
        ),

        "calves" to listOf(
            PrehabMove("Calf Stretch Step",     2, "60s/side",     "Heel below step, knee straight"),
            PrehabMove("Ankle Circle",          2, "15 each way",  "Full ROM, slow"),
            PrehabMove("SL Heel Raise Slow",    2, "10 reps",      "3-count up, 3-count down")
        ),

        "general" to listOf(
            PrehabMove("World's Greatest",      2, "5 reps/side",  "Hip flexor + thoracic rotation"),
            PrehabMove("Inchworm",              2, "8 reps",       "Hamstring + shoulder warm-up"),
            PrehabMove("Jumping Jack",          1, "30 reps",      "Elevate heart rate, full body"),
            PrehabMove("Hip Circle",            2, "10 each way",  "Tall posture, large ROM")
        )
    )
}
