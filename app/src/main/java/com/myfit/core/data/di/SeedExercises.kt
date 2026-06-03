package com.myfit.core.data.di

import com.myfit.core.data.db.entity.ExerciseEntity

object SeedExercises {
    val all = listOf(
        // ── CHEST ──
        ExerciseEntity("ex_bench",           "Barbell Bench Press",        "chest",      "[\"triceps\",\"front_delt\"]",  "barbell",    "push"),
        ExerciseEntity("ex_incline_bench",   "Incline Barbell Press",      "chest",      "[\"front_delt\"]",              "barbell",    "push"),
        ExerciseEntity("ex_decline_bench",   "Decline Bench Press",        "chest",      "[\"triceps\"]",                 "barbell",    "push"),
        ExerciseEntity("ex_db_press",        "Dumbbell Bench Press",       "chest",      "[\"triceps\"]",                 "dumbbell",   "push"),
        ExerciseEntity("ex_incline_db",      "Incline Dumbbell Press",     "chest",      "[\"front_delt\"]",              "dumbbell",   "push"),
        ExerciseEntity("ex_dip",             "Parallel Bar Dip",           "chest",      "[\"triceps\"]",                 "bodyweight", "push"),
        ExerciseEntity("ex_cable_fly",       "Cable Fly",                  "chest",      "[]",                            "cable",      "push"),
        ExerciseEntity("ex_pec_deck",        "Pec Deck",                   "chest",      "[]",                            "machine",    "push"),
        ExerciseEntity("ex_pushup",          "Push-Up",                    "chest",      "[\"triceps\",\"front_delt\"]",  "bodyweight", "push"),
        ExerciseEntity("ex_db_fly",          "Dumbbell Fly",               "chest",      "[]",                            "dumbbell",   "push"),

        // ── BACK ──
        ExerciseEntity("ex_deadlift",        "Conventional Deadlift",      "hamstrings", "[\"glutes\",\"traps\"]",        "barbell",    "hinge"),
        ExerciseEntity("ex_row",             "Barbell Row",                "lats",       "[\"rear_delt\",\"biceps\"]",    "barbell",    "pull"),
        ExerciseEntity("ex_pullup",          "Pull-Up",                    "lats",       "[\"biceps\",\"rear_delt\"]",    "bodyweight", "pull"),
        ExerciseEntity("ex_chinup",          "Chin-Up",                    "lats",       "[\"biceps\"]",                  "bodyweight", "pull"),
        ExerciseEntity("ex_lat_pulldown",    "Lat Pulldown",               "lats",       "[\"biceps\"]",                  "cable",      "pull"),
        ExerciseEntity("ex_cable_row",       "Cable Row",                  "lats",       "[\"rear_delt\"]",               "cable",      "pull"),
        ExerciseEntity("ex_db_row",          "Dumbbell Row",               "lats",       "[\"biceps\"]",                  "dumbbell",   "pull"),
        ExerciseEntity("ex_tbar_row",        "T-Bar Row",                  "lats",       "[\"rear_delt\",\"biceps\"]",    "barbell",    "pull"),
        ExerciseEntity("ex_straight_pull",   "Straight-Arm Pulldown",      "lats",       "[]",                            "cable",      "pull"),
        ExerciseEntity("ex_back_ext",        "Back Extension",             "erectors",   "[\"glutes\"]",                  "machine",    "hinge"),
        ExerciseEntity("ex_face_pull",       "Face Pull",                  "rear_delt",  "[\"rotator_cuff\"]",            "cable",      "pull"),
        ExerciseEntity("ex_shrug",           "Barbell Shrug",              "traps",      "[]",                            "barbell",    "carry"),

        // ── SHOULDERS ──
        ExerciseEntity("ex_ohp",             "Overhead Press",             "front_delt", "[\"triceps\",\"upper_chest\"]", "barbell",    "push"),
        ExerciseEntity("ex_db_ohp",          "Dumbbell Shoulder Press",    "front_delt", "[\"triceps\"]",                 "dumbbell",   "push"),
        ExerciseEntity("ex_lateral_raise",   "Lateral Raise",              "side_delt",  "[]",                            "dumbbell",   "push"),
        ExerciseEntity("ex_cable_lateral",   "Cable Lateral Raise",        "side_delt",  "[]",                            "cable",      "push"),
        ExerciseEntity("ex_rear_delt_fly",   "Rear Delt Fly",              "rear_delt",  "[]",                            "dumbbell",   "pull"),
        ExerciseEntity("ex_front_raise",     "Front Raise",                "front_delt", "[]",                            "dumbbell",   "push"),
        ExerciseEntity("ex_arnold_press",    "Arnold Press",               "front_delt", "[\"side_delt\",\"triceps\"]",   "dumbbell",   "push"),
        ExerciseEntity("ex_upright_row",     "Upright Row",                "side_delt",  "[\"traps\",\"biceps\"]",        "barbell",    "pull"),

        // ── LEGS ──
        ExerciseEntity("ex_squat",           "Barbell Back Squat",         "quadriceps", "[\"glutes\",\"hamstrings\"]",   "barbell",    "squat"),
        ExerciseEntity("ex_front_squat",     "Barbell Front Squat",        "quadriceps", "[\"glutes\"]",                  "barbell",    "squat"),
        ExerciseEntity("ex_rdl",             "Romanian Deadlift",          "hamstrings", "[\"glutes\",\"erectors\"]",     "barbell",    "hinge"),
        ExerciseEntity("ex_leg_press",       "Leg Press",                  "quadriceps", "[\"glutes\"]",                  "machine",    "squat"),
        ExerciseEntity("ex_leg_curl",        "Leg Curl",                   "hamstrings", "[]",                            "machine",    "hinge"),
        ExerciseEntity("ex_leg_ext",         "Leg Extension",              "quadriceps", "[]",                            "machine",    "squat"),
        ExerciseEntity("ex_hip_thrust",      "Hip Thrust",                 "glutes",     "[\"hamstrings\"]",              "barbell",    "hinge"),
        ExerciseEntity("ex_lunge",           "Walking Lunge",              "quadriceps", "[\"glutes\"]",                  "bodyweight", "squat"),
        ExerciseEntity("ex_bulgarian",       "Bulgarian Split Squat",      "quadriceps", "[\"glutes\"]",                  "dumbbell",   "squat"),
        ExerciseEntity("ex_hack_squat",      "Hack Squat",                 "quadriceps", "[\"glutes\"]",                  "machine",    "squat"),
        ExerciseEntity("ex_nordic_curl",     "Nordic Curl",                "hamstrings", "[]",                            "bodyweight", "hinge"),
        ExerciseEntity("ex_calf_raise",      "Standing Calf Raise",        "calves",     "[]",                            "machine",    "carry"),
        ExerciseEntity("ex_seated_calf",     "Seated Calf Raise",          "calves",     "[]",                            "machine",    "carry"),
        ExerciseEntity("ex_sumo_dl",         "Sumo Deadlift",              "hamstrings", "[\"glutes\",\"adductors\"]",    "barbell",    "hinge"),

        // ── BICEPS ──
        ExerciseEntity("ex_curl",            "Barbell Curl",               "biceps",     "[]",                            "barbell",    "pull"),
        ExerciseEntity("ex_db_curl",         "Dumbbell Curl",              "biceps",     "[]",                            "dumbbell",   "pull"),
        ExerciseEntity("ex_hammer_curl",     "Hammer Curl",                "biceps",     "[\"brachialis\"]",              "dumbbell",   "pull"),
        ExerciseEntity("ex_incline_curl",    "Incline Dumbbell Curl",      "biceps",     "[]",                            "dumbbell",   "pull"),
        ExerciseEntity("ex_cable_curl",      "Cable Curl",                 "biceps",     "[]",                            "cable",      "pull"),
        ExerciseEntity("ex_preacher_curl",   "Preacher Curl",              "biceps",     "[]",                            "machine",    "pull"),

        // ── TRICEPS ──
        ExerciseEntity("ex_tricep_ext",      "Tricep Pushdown",            "triceps",    "[]",                            "cable",      "push"),
        ExerciseEntity("ex_skull_crusher",   "Skull Crusher",              "triceps",    "[]",                            "barbell",    "push"),
        ExerciseEntity("ex_overhead_tri",    "Overhead Tricep Extension",  "triceps",    "[]",                            "dumbbell",   "push"),
        ExerciseEntity("ex_close_bench",     "Close Grip Bench Press",     "triceps",    "[\"chest\"]",                   "barbell",    "push"),
        ExerciseEntity("ex_tri_kickback",    "Tricep Kickback",            "triceps",    "[]",                            "dumbbell",   "push"),

        // ── CORE ──
        ExerciseEntity("ex_ab_wheel",        "Ab Wheel Rollout",           "core",       "[\"lats\"]",                    "bodyweight", "carry"),
        ExerciseEntity("ex_cable_crunch",    "Cable Crunch",               "core",       "[]",                            "cable",      "carry"),
        ExerciseEntity("ex_hanging_leg",     "Hanging Leg Raise",          "core",       "[\"hip_flexors\"]",             "bodyweight", "carry"),
        ExerciseEntity("ex_plank",           "Plank",                      "core",       "[]",                            "bodyweight", "carry"),
        ExerciseEntity("ex_side_plank",      "Side Plank",                 "core",       "[\"obliques\"]",                "bodyweight", "carry"),
        ExerciseEntity("ex_russian_twist",   "Russian Twist",              "core",       "[\"obliques\"]",                "bodyweight", "carry"),
        ExerciseEntity("ex_sit_up",          "Sit-Up",                     "core",       "[]",                            "bodyweight", "carry"),
        ExerciseEntity("ex_leg_raise",       "Lying Leg Raise",            "core",       "[]",                            "bodyweight", "carry")
    )
}
