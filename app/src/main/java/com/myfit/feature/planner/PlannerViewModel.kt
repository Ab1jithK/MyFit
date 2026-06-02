package com.myfit.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.core.data.db.dao.ExerciseDao
import com.myfit.core.data.db.dao.WorkoutDao
import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.data.db.entity.TemplateExerciseEntity
import com.myfit.core.data.db.entity.WorkoutTemplateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TemplateWithExercises(
    val template: WorkoutTemplateEntity,
    val exercises: List<ExerciseEntity>
)

data class PlannerUiState(
    val selectedDay: Int = 0,
    val byDay: Map<Int, List<TemplateWithExercises>> = emptyMap(),
    val allExercises: List<ExerciseEntity> = emptyList(),
    val editingTemplate: TemplateWithExercises? = null,
    val showAddTemplate: Boolean = false,
    val exercisePickerQuery: String = "",
    val exerciseSearchResults: List<ExerciseEntity> = emptyList()
) {
    val selectedTemplates: List<TemplateWithExercises>
        get() = byDay[selectedDay] ?: emptyList()
}

val DAY_LABELS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao
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
            }
        }
        viewModelScope.launch {
            exerciseDao.getAllExercises().collectLatest { exercises ->
                _state.update { it.copy(allExercises = exercises, exerciseSearchResults = exercises) }
            }
        }
    }

    fun selectDay(day: Int) = _state.update { it.copy(selectedDay = day) }

    fun showAddTemplate() = _state.update { it.copy(showAddTemplate = true) }
    fun hideAddTemplate() = _state.update { it.copy(showAddTemplate = false) }

    fun addTemplate(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            workoutDao.insertTemplate(WorkoutTemplateEntity(
                id          = UUID.randomUUID().toString(),
                mesocycleId = null,
                dayOfWeek   = _state.value.selectedDay,
                name        = name.trim(),
                createdAt   = System.currentTimeMillis()
            ))
            hideAddTemplate()
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch { workoutDao.deleteTemplate(templateId) }
    }

    fun renameTemplate(template: WorkoutTemplateEntity, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { workoutDao.updateTemplate(template.copy(name = newName.trim())) }
    }

    fun openTemplateEditor(twx: TemplateWithExercises) {
        _state.update { it.copy(editingTemplate = twx, exercisePickerQuery = "", exerciseSearchResults = it.allExercises) }
    }

    fun closeTemplateEditor() = _state.update { it.copy(editingTemplate = null) }

    fun searchExercises(query: String) {
        _state.update { s ->
            val results = if (query.isBlank()) s.allExercises
            else s.allExercises.filter { it.name.contains(query, ignoreCase = true) }
            s.copy(exercisePickerQuery = query, exerciseSearchResults = results)
        }
    }

    fun addExerciseToTemplate(exercise: ExerciseEntity) {
        val tmpl = _state.value.editingTemplate ?: return
        if (tmpl.exercises.any { it.id == exercise.id }) return
        viewModelScope.launch {
            exerciseDao.insertTemplateExercise(TemplateExerciseEntity(
                id           = UUID.randomUUID().toString(),
                templateId   = tmpl.template.id,
                exerciseId   = exercise.id,
                position     = tmpl.exercises.size,
                targetSets   = null,
                targetRepsMin = null,
                targetRepsMax = null,
                targetRpe    = null
            ))
            val updated = tmpl.copy(exercises = tmpl.exercises + exercise)
            _state.update { it.copy(editingTemplate = updated) }
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
