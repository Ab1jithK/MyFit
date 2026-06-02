package com.myfit.wear.di

import com.myfit.wear.data.entity.ExerciseEntity

object WearSeedExercises {
    val all = listOf(
        ExerciseEntity("ex_squat",         "Barbell Back Squat",    "quadriceps", "[\"glutes\",\"hamstrings\"]",  "barbell",    "squat"),
        ExerciseEntity("ex_front_squat",   "Barbell Front Squat",   "quadriceps", "[\"glutes\"]",                 "barbell",    "squat"),
        ExerciseEntity("ex_rdl",           "Romanian Deadlift",     "hamstrings", "[\"glutes\",\"erectors\"]",    "barbell",    "hinge"),
        ExerciseEntity("ex_deadlift",      "Conventional Deadlift", "hamstrings", "[\"glutes\",\"traps\"]",       "barbell",    "hinge"),
        ExerciseEntity("ex_bench",         "Barbell Bench Press",   "chest",      "[\"triceps\",\"front_delt\"]", "barbell",    "push"),
        ExerciseEntity("ex_incline_bench", "Incline Barbell Press", "chest",      "[\"front_delt\"]",             "barbell",    "push"),
        ExerciseEntity("ex_ohp",           "Overhead Press",        "front_delt", "[\"triceps\",\"upper_chest\"]","barbell",    "push"),
        ExerciseEntity("ex_row",           "Barbell Row",           "lats",       "[\"rear_delt\",\"biceps\"]",   "barbell",    "pull"),
        ExerciseEntity("ex_pullup",        "Pull-Up",               "lats",       "[\"biceps\",\"rear_delt\"]",   "bodyweight", "pull"),
        ExerciseEntity("ex_chinup",        "Chin-Up",               "lats",       "[\"biceps\"]",                 "bodyweight", "pull"),
        ExerciseEntity("ex_dip",           "Parallel Bar Dip",      "chest",      "[\"triceps\"]",                "bodyweight", "push"),
        ExerciseEntity("ex_lunge",         "Walking Lunge",         "quadriceps", "[\"glutes\"]",                 "bodyweight", "squat"),
        ExerciseEntity("ex_hip_thrust",    "Hip Thrust",            "glutes",     "[\"hamstrings\"]",             "barbell",    "hinge"),
        ExerciseEntity("ex_leg_press",     "Leg Press",             "quadriceps", "[\"glutes\"]",                 "machine",    "squat"),
        ExerciseEntity("ex_leg_curl",      "Leg Curl",              "hamstrings", "[]",                           "machine",    "hinge"),
        ExerciseEntity("ex_lat_pulldown",  "Lat Pulldown",          "lats",       "[\"biceps\"]",                 "cable",      "pull"),
        ExerciseEntity("ex_cable_row",     "Cable Row",             "lats",       "[\"rear_delt\"]",              "cable",      "pull"),
        ExerciseEntity("ex_db_press",      "Dumbbell Bench Press",  "chest",      "[\"triceps\"]",                "dumbbell",   "push"),
        ExerciseEntity("ex_db_row",        "Dumbbell Row",          "lats",       "[\"biceps\"]",                 "dumbbell",   "pull"),
        ExerciseEntity("ex_curl",          "Barbell Curl",          "biceps",     "[]",                           "barbell",    "pull"),
        ExerciseEntity("ex_tricep_ext",    "Tricep Pushdown",       "triceps",    "[]",                           "cable",      "push"),
        ExerciseEntity("ex_face_pull",     "Face Pull",             "rear_delt",  "[\"rotator_cuff\"]",           "cable",      "pull"),
        ExerciseEntity("ex_lateral_raise", "Lateral Raise",         "side_delt",  "[]",                           "dumbbell",   "push"),
        ExerciseEntity("ex_calf_raise",    "Standing Calf Raise",   "calves",     "[]",                           "machine",    "carry"),
        ExerciseEntity("ex_plank",         "Plank",                 "core",       "[]",                           "bodyweight", "carry")
    )
}
