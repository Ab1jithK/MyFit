package com.myfit.core.data.di

import com.myfit.core.data.db.entity.FoodItemEntity

// All values per 100g: calories (kcal), protein (g), carbs (g), fat (g)
object SeedFoods {
    val all = listOf(
        // Proteins
        FoodItemEntity("food_chicken_breast", "Chicken Breast",       165f, 31f,   0f,   3.6f),
        FoodItemEntity("food_turkey_breast",  "Turkey Breast",        157f, 24f,   0f,   7f),
        FoodItemEntity("food_beef_lean",      "Beef (Lean)",          215f, 26f,   0f,   12f),
        FoodItemEntity("food_ground_beef",    "Ground Beef (90%)",    176f, 20f,   0f,   10f),
        FoodItemEntity("food_pork_loin",      "Pork Loin",            242f, 22f,   0f,   16f),
        FoodItemEntity("food_salmon",         "Salmon",               208f, 20f,   0f,   13f),
        FoodItemEntity("food_tuna_canned",    "Tuna (Canned)",        116f, 25.5f, 0f,   0.8f),
        FoodItemEntity("food_tuna_fresh",     "Tuna (Fresh)",         144f, 23f,   0f,   5f),
        FoodItemEntity("food_shrimp",         "Shrimp",               99f,  24f,   0.2f, 0.3f),
        FoodItemEntity("food_eggs_whole",     "Eggs (Whole)",         143f, 13f,   0.7f, 10f),
        FoodItemEntity("food_egg_whites",     "Egg Whites",           52f,  11f,   0.7f, 0.2f),
        FoodItemEntity("food_greek_yogurt",   "Greek Yogurt",         59f,  10f,   3.6f, 0.4f),
        FoodItemEntity("food_cottage_cheese", "Cottage Cheese",       98f,  11f,   3.4f, 4.3f),
        FoodItemEntity("food_tofu",           "Tofu",                 76f,  8f,    1.9f, 4.2f),
        FoodItemEntity("food_tempeh",         "Tempeh",               193f, 19f,   9.4f, 11f),
        FoodItemEntity("food_edamame",        "Edamame",              121f, 11f,   9f,   5f),
        FoodItemEntity("food_whey_protein",   "Whey Protein",         400f, 80f,   10f,  5f),
        FoodItemEntity("food_casein_protein", "Casein Protein",       380f, 78f,   12f,  3f),
        // Carbohydrates
        FoodItemEntity("food_white_rice",     "White Rice (cooked)",  130f, 2.7f,  28f,  0.3f),
        FoodItemEntity("food_brown_rice",     "Brown Rice (cooked)",  111f, 2.6f,  23f,  0.9f),
        FoodItemEntity("food_oats",           "Oats (dry)",           389f, 17f,   66f,  7f),
        FoodItemEntity("food_sweet_potato",   "Sweet Potato",         86f,  1.6f,  20f,  0.1f),
        FoodItemEntity("food_potato",         "Potato (boiled)",      77f,  2f,    17f,  0.1f),
        FoodItemEntity("food_quinoa",         "Quinoa (cooked)",      120f, 4.4f,  21f,  1.9f),
        FoodItemEntity("food_pasta",          "Pasta (cooked)",       131f, 5f,    25f,  1.1f),
        FoodItemEntity("food_bread_white",    "Bread (White)",        265f, 9f,    49f,  3.2f),
        FoodItemEntity("food_bread_whole",    "Bread (Whole Wheat)",  247f, 13f,   41f,  3.4f),
        FoodItemEntity("food_rice_cakes",     "Rice Cakes",           387f, 8f,    82f,  3f),
        FoodItemEntity("food_corn",           "Corn",                 86f,  3.3f,  19f,  1.4f),
        FoodItemEntity("food_banana",         "Banana",               89f,  1.1f,  23f,  0.3f),
        FoodItemEntity("food_apple",          "Apple",                52f,  0.3f,  14f,  0.2f),
        FoodItemEntity("food_blueberries",    "Blueberries",          57f,  0.7f,  14f,  0.3f),
        FoodItemEntity("food_orange",         "Orange",               47f,  0.9f,  12f,  0.1f),
        FoodItemEntity("food_strawberries",   "Strawberries",         32f,  0.7f,  7.7f, 0.3f),
        FoodItemEntity("food_dates",          "Dates",                277f, 1.8f,  75f,  0.2f),
        FoodItemEntity("food_honey",          "Honey",                304f, 0.3f,  82f,  0f),
        // Fats
        FoodItemEntity("food_avocado",        "Avocado",              160f, 2f,    9f,   15f),
        FoodItemEntity("food_almonds",        "Almonds",              579f, 21f,   22f,  50f),
        FoodItemEntity("food_walnuts",        "Walnuts",              654f, 15f,   14f,  65f),
        FoodItemEntity("food_cashews",        "Cashews",              553f, 18f,   30f,  44f),
        FoodItemEntity("food_peanut_butter",  "Peanut Butter",        588f, 25f,   20f,  50f),
        FoodItemEntity("food_almond_butter",  "Almond Butter",        614f, 21f,   19f,  56f),
        FoodItemEntity("food_olive_oil",      "Olive Oil",            884f, 0f,    0f,   100f),
        FoodItemEntity("food_coconut_oil",    "Coconut Oil",          862f, 0f,    0f,   100f),
        FoodItemEntity("food_butter",         "Butter",               717f, 0.9f,  0.1f, 81f),
        FoodItemEntity("food_dark_chocolate", "Dark Chocolate (85%)", 598f, 12f,   46f,  43f),
        // Vegetables
        FoodItemEntity("food_broccoli",       "Broccoli",             34f,  2.8f,  7f,   0.4f),
        FoodItemEntity("food_spinach",        "Spinach",              23f,  2.9f,  3.6f, 0.4f),
        FoodItemEntity("food_kale",           "Kale",                 49f,  4.3f,  9f,   0.9f),
        FoodItemEntity("food_carrots",        "Carrots",              41f,  0.9f,  10f,  0.2f),
        FoodItemEntity("food_bell_pepper",    "Bell Pepper",          31f,  1f,    7f,   0.3f),
        FoodItemEntity("food_cucumber",       "Cucumber",             15f,  0.7f,  3.6f, 0.1f),
        FoodItemEntity("food_tomato",         "Tomato",               18f,  0.9f,  3.9f, 0.2f),
        FoodItemEntity("food_onion",          "Onion",                40f,  1.1f,  9.3f, 0.1f),
        FoodItemEntity("food_peas",           "Green Peas",           81f,  5.4f,  14f,  0.4f),
        // Dairy
        FoodItemEntity("food_milk_whole",     "Milk (Whole)",         61f,  3.2f,  4.8f, 3.3f),
        FoodItemEntity("food_milk_skimmed",   "Milk (Skimmed)",       34f,  3.4f,  5f,   0.2f),
        FoodItemEntity("food_cheddar",        "Cheddar Cheese",       402f, 25f,   1.3f, 33f),
        FoodItemEntity("food_cream_cheese",   "Cream Cheese",         342f, 6f,    4.1f, 34f),
        // Legumes
        FoodItemEntity("food_black_beans",    "Black Beans (cooked)", 132f, 8.9f,  24f,  0.5f),
        FoodItemEntity("food_lentils",        "Lentils (cooked)",     116f, 9f,    20f,  0.4f),
        FoodItemEntity("food_chickpeas",      "Chickpeas (cooked)",   164f, 8.9f,  27f,  2.6f),
        // Misc
        FoodItemEntity("food_protein_bar",    "Protein Bar",          385f, 30f,   40f,  10f)
    )
}
