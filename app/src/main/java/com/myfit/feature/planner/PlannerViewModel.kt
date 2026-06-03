package com.myfit.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.core.data.db.dao.ExerciseDao
import com.myfit.core.data.db.dao.PlanOverrideDao
import com.myfit.core.data.db.dao.WorkoutDao
import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.data.db.entity.PlanOverrideEntity
import com.myfit.core.data.db.entity.TemplateExerciseEntity
import com.myfit.core.data.db.entity.WorkoutTemplateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject

data class TemplateWithExercises(
    val template: WorkoutTemplateEntity,
    val exercises: List<ExerciseEntity>
)

data class WeekDay(
    val date: LocalDate,
    val dayIndex: Int,          // 0=Mon … 6=Sun
    val templates: List<TemplateWithExercises>,
    val isRestDay: Boolean,
    val restNote: String?
)

data class PlannerUiState(
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val weekDays: List<WeekDay> = emptyList(),
    val selectedDayIndex: Int = LocalDate.now().dayOfWeek.value - 1,    // 0-based Mon
    val byDay: Map<Int, List<TemplateWithExercises>> = emptyMap(),
    val allExercises: List<ExerciseEntity> = emptyList(),
    val editingTemplate: TemplateWithExercises? = null,
    val showAddTemplate: Boolean = false,
    val exercisePickerQuery: String = "",
    val exercisePickerFilter: String = "ALL",
    val exerciseSearchResults: List<ExerciseEntity> = emptyList(),
    val stagedExerciseIds: Set<String> = emptySet()
) {
    val selectedDay: Int get() = selectedDayIndex
    val selectedTemplates: List<TemplateWithExercises> get() = byDay[selectedDayIndex] ?: emptyList()
    val selectedWeekDay: WeekDay? get() = weekDays.getOrNull(selectedDayIndex)
}

val DAY_LABELS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
private val ISO = DateTimeFormatter.ISO_LOCAL_DATE

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val overrideDao: PlanOverrideDao
) : ViewModel() {

    private val _state = MutableStateFlow(PlannerUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            workoutDao.getAllTemplates().collectLatest { templates ->
                val byDay = mutableMapOf<Int, MutableList<TemplateWithExercises>>()
                templates.forEach { tmpl ->
                    val day = tmpl.dayOfWeek ?: return@forEach
                    val exercises = exerciseDao.getExercisesForTemplateOnce(tmpl.id)
                    byDay.getOrPut(day) { mutableListOf() }.add(TemplateWithExercises(tmpl, exercises))
                }
                _state.update { it.copy(byDay = byDay) }
                refreshWeekDays()
            }
        }
        viewModelScope.launch {
            exerciseDao.getAllExercises().collectLatest { exercises ->
                _state.update { it.copy(allExercises = exercises, exerciseSearchResults = exercises) }
            }
        }
    }

    // ── Week navigation ───────────────────────────────────────────────────────

    fun prevWeek() {
        _state.update { it.copy(weekStart = it.weekStart.minusWeeks(1)) }
        viewModelScope.launch { refreshWeekDays() }
    }

    fun nextWeek() {
        _state.update { it.copy(weekStart = it.weekStart.plusWeeks(1)) }
        viewModelScope.launch { refreshWeekDays() }
    }

    fun goToCurrentWeek() {
        _state.update { it.copy(
            weekStart      = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            selectedDayIndex = LocalDate.now().dayOfWeek.value - 1
        ) }
        viewModelScope.launch { refreshWeekDays() }
    }

    private suspend fun refreshWeekDays() {
        val s     = _state.value
        val start = s.weekStart
        val end   = start.plusDays(6)
        val overrides = overrideDao.getOverridesForRange(start.format(ISO), end.format(ISO))
            .associateBy { it.date }

        val days = (0..6).map { i ->
            val date      = start.plusDays(i.toLong())
            val dateStr   = date.format(ISO)
            val override  = overrides[dateStr]
            WeekDay(
                date       = date,
                dayIndex   = i,
                templates  = s.byDay[i] ?: emptyList(),
                isRestDay  = override?.isRestDay == true,
                restNote   = override?.note
            )
        }
        _state.update { it.copy(weekDays = days) }
    }

    fun selectDay(index: Int) = _state.update { it.copy(selectedDayIndex = index) }

    // ── Rest day override ─────────────────────────────────────────────────────

    fun toggleRestDay(note: String? = null) {
        val s    = _state.value
        val date = s.weekStart.plusDays(s.selectedDayIndex.toLong())
        val isCurrentlyRest = s.selectedWeekDay?.isRestDay == true
        viewModelScope.launch {
            if (isCurrentlyRest) {
                overrideDao.clearOverride(date.format(ISO))
            } else {
                overrideDao.setOverride(PlanOverrideEntity(
                    date      = date.format(ISO),
                    isRestDay = true,
                    note      = note
                ))
            }
            refreshWeekDays()
        }
    }

    // ── Template CRUD ─────────────────────────────────────────────────────────

    fun showAddTemplate() = _state.update { it.copy(showAddTemplate = true, stagedExerciseIds = emptySet(), exercisePickerQuery = "", exercisePickerFilter = "ALL") }
    fun hideAddTemplate() = _state.update { it.copy(showAddTemplate = false, stagedExerciseIds = emptySet()) }

    fun addTemplate(name: String) {
        if (name.isBlank()) return
        val staged = _state.value.stagedExerciseIds
        viewModelScope.launch {
            val templateId = UUID.randomUUID().toString()
            workoutDao.insertTemplate(WorkoutTemplateEntity(
                id          = templateId,
                mesocycleId = null,
                dayOfWeek   = _state.value.selectedDayIndex,
                name        = name.trim(),
                createdAt   = System.currentTimeMillis()
            ))
            staged.forEachIndexed { pos, exId ->
                exerciseDao.insertTemplateExercise(TemplateExerciseEntity(
                    id            = UUID.randomUUID().toString(),
                    templateId    = templateId,
                    exerciseId    = exId,
                    position      = pos,
                    targetSets    = null,
                    targetRepsMin = null,
                    targetRepsMax = null,
                    targetRpe     = null
                ))
            }
            hideAddTemplate()
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch { workoutDao.deleteTemplate(templateId) }
    }

    // ── Template editor ───────────────────────────────────────────────────────

    fun openTemplateEditor(twx: TemplateWithExercises) {
        _state.update { it.copy(
            editingTemplate       = twx,
            exercisePickerQuery   = "",
            exercisePickerFilter  = "ALL",
            exerciseSearchResults = it.allExercises,
            stagedExerciseIds     = emptySet()
        ) }
    }

    fun closeTemplateEditor() = _state.update { it.copy(editingTemplate = null, stagedExerciseIds = emptySet()) }

    // ── Exercise search ───────────────────────────────────────────────────────

    fun setExerciseFilter(category: String) {
        _state.update { s ->
            val byQuery = if (s.exercisePickerQuery.isBlank()) s.allExercises
                          else s.allExercises.filter { it.name.contains(s.exercisePickerQuery, ignoreCase = true) }
            s.copy(exercisePickerFilter = category, exerciseSearchResults = applyMuscleFilter(byQuery, category))
        }
    }

    fun searchExercises(query: String) {
        _state.update { s ->
            val byQuery = if (query.isBlank()) s.allExercises
                          else s.allExercises.filter { it.name.contains(query, ignoreCase = true) }
            s.copy(exercisePickerQuery = query, exerciseSearchResults = applyMuscleFilter(byQuery, s.exercisePickerFilter))
        }
    }

    private fun applyMuscleFilter(exercises: List<ExerciseEntity>, category: String): List<ExerciseEntity> {
        val muscles = when (category) {
            "CHEST"     -> setOf("chest")
            "BACK"      -> setOf("lats", "rear_delt", "traps", "erectors")
            "SHOULDERS" -> setOf("front_delt", "side_delt", "rear_delt")
            "LEGS"      -> setOf("quadriceps", "hamstrings", "glutes", "calves", "adductors")
            "ARMS"      -> setOf("biceps", "triceps", "brachialis")
            "CORE"      -> setOf("core", "obliques")
            else        -> emptySet()
        }
        return if (muscles.isEmpty()) exercises else exercises.filter { it.primaryMuscle in muscles }
    }

    // ── Staged exercise selection (multi-select + confirm) ────────────────────

    fun toggleStageExercise(exercise: ExerciseEntity) {
        _state.update { s ->
            val already = s.stagedExerciseIds
            s.copy(stagedExerciseIds = if (exercise.id in already) already - exercise.id else already + exercise.id)
        }
    }

    fun confirmStagedExercises() {
        val tmpl   = _state.value.editingTemplate ?: return
        val staged = _state.value.stagedExerciseIds
        val toAdd  = _state.value.allExercises.filter { it.id in staged && tmpl.exercises.none { e -> e.id == it.id } }
        if (toAdd.isEmpty()) { _state.update { it.copy(stagedExerciseIds = emptySet()) }; return }
        viewModelScope.launch {
            toAdd.forEachIndexed { i, ex ->
                exerciseDao.insertTemplateExercise(TemplateExerciseEntity(
                    id            = UUID.randomUUID().toString(),
                    templateId    = tmpl.template.id,
                    exerciseId    = ex.id,
                    position      = tmpl.exercises.size + i,
                    targetSets    = null,
                    targetRepsMin = null,
                    targetRepsMax = null,
                    targetRpe     = null
                ))
            }
            val updated = tmpl.copy(exercises = tmpl.exercises + toAdd)
            _state.update { it.copy(editingTemplate = updated, stagedExerciseIds = emptySet()) }
        }
    }

    fun removeExerciseFromTemplate(templateId: String, exerciseId: String) {
        viewModelScope.launch {
            val rows = exerciseDao.getTemplateExercisesOnce(templateId)
            rows.find { it.exerciseId == exerciseId }?.let { exerciseDao.deleteTemplateExercise(it.id) }
            val tmpl = _state.value.editingTemplate ?: return@launch
            _state.update { it.copy(editingTemplate = tmpl.copy(exercises = tmpl.exercises.filter { e -> e.id != exerciseId })) }
        }
    }
}
