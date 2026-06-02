package com.myfit.feature.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.core.data.db.dao.NutritionDao
import com.myfit.core.data.db.entity.NutritionLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val dao: NutritionDao
) : ViewModel() {

    private val _state = MutableStateFlow(NutritionUiState())
    val state = _state.asStateFlow()

    init {
        // Observe all foods reactively — fires again when seed completes after DB creation
        viewModelScope.launch {
            dao.getAllFoodItems().collectLatest { allFoods ->
                if (allFoods.isEmpty()) return@collectLatest
                refreshWithFoods(allFoods)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { refreshLog() }
    }

    private suspend fun refreshLog() {
        val midnight = todayMidnightMs()
        val rawLog   = dao.getTodayLog(midnight)
        val allFoods = _state.value.quickItems.map { it.food } +
                       rawLog.map { log -> _state.value.todayEntries.find { it.log.id == log.id }?.food }
                           .filterNotNull()
        // Re-fetch log foods from DB to be accurate
        val logFoodIds = rawLog.map { it.foodId }.distinct()
        val logFoods   = if (logFoodIds.isNotEmpty()) dao.getFoodsByIds(logFoodIds) else emptyList()
        val byId       = logFoods.associateBy { it.id }
        val entries    = rawLog.mapNotNull { log -> byId[log.foodId]?.let { NutritionLogEntry(log, it) } }
        _state.update { it.copy(todayEntries = entries, dailyMacros = DailyMacros.from(entries)) }
    }

    private suspend fun refreshWithFoods(allFoods: List<com.myfit.core.data.db.entity.FoodItemEntity>) {
        val topIds    = dao.getTopLoggedFoodIds()
        val quickItems = if (topIds.isNotEmpty()) {
            val byId = allFoods.associateBy { it.id }
            topIds.mapNotNull { byId[it] }.map { QuickItem(it, defaultGrams = 100f) }
        } else {
            val byId = allFoods.associateBy { it.id }
            DEFAULT_QUICK_IDS.mapNotNull { byId[it] }.map { QuickItem(it, defaultGrams = 100f) }
                .ifEmpty { allFoods.take(10).map { QuickItem(it, defaultGrams = 100f) } }
        }

        val midnight   = todayMidnightMs()
        val rawLog     = dao.getTodayLog(midnight)
        val byId       = allFoods.associateBy { it.id }
        val entries    = rawLog.mapNotNull { log -> byId[log.foodId]?.let { NutritionLogEntry(log, it) } }

        _state.update {
            it.copy(
                quickItems   = quickItems,
                todayEntries = entries,
                dailyMacros  = DailyMacros.from(entries),
                isLoading    = false
            )
        }
    }

    fun selectQuickItem(item: QuickItem) {
        _state.update { it.copy(pendingItem = item) }
    }

    fun dismissConfirm() {
        _state.update { it.copy(pendingItem = null) }
    }

    fun confirmLog(item: QuickItem, grams: Float, mealSlot: String?) {
        _state.update { it.copy(pendingItem = null) }
        viewModelScope.launch {
            dao.logNutrition(
                NutritionLogEntity(
                    id        = UUID.randomUUID().toString(),
                    foodId    = item.food.id,
                    loggedAt  = System.currentTimeMillis(),
                    quantityG = grams,
                    mealSlot  = mealSlot
                )
            )
            refresh()
        }
    }

    private fun todayMidnightMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private companion object {
        val DEFAULT_QUICK_IDS = listOf(
            "food_chicken_breast", "food_white_rice", "food_oats",
            "food_eggs_whole", "food_greek_yogurt", "food_salmon",
            "food_sweet_potato", "food_broccoli", "food_whey_protein", "food_banana"
        )
    }
}
