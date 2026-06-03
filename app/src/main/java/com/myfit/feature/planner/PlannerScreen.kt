package com.myfit.feature.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.view.ViewGroup
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myfit.core.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val MONTH_FMT  = DateTimeFormatter.ofPattern("MMM d")
private val SHORT_DAY  = listOf("M","T","W","T","F","S","S")
private val CATEGORIES = listOf("ALL","CHEST","BACK","SHOULDERS","LEGS","ARMS","CORE")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(vm: PlannerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    // Add template sheet
    if (state.showAddTemplate) {
        AddTemplateSheet(
            dayLabel       = DAY_LABELS[state.selectedDayIndex],
            searchQuery    = state.exercisePickerQuery,
            searchFilter   = state.exercisePickerFilter,
            searchResults  = state.exerciseSearchResults,
            allExercises   = state.allExercises,
            stagedIds      = state.stagedExerciseIds,
            onSearchChange = vm::searchExercises,
            onFilterChange = vm::setExerciseFilter,
            onToggleStage  = vm::toggleStageExercise,
            onSave         = vm::addTemplate,
            onDismiss      = vm::hideAddTemplate
        )
    }

    // Template editor sheet
    state.editingTemplate?.let { twx ->
        TemplateEditorSheet(
            twx              = twx,
            searchQuery      = state.exercisePickerQuery,
            searchFilter     = state.exercisePickerFilter,
            searchResults    = state.exerciseSearchResults,
            stagedIds        = state.stagedExerciseIds,
            onSearchChange   = vm::searchExercises,
            onFilterChange   = vm::setExerciseFilter,
            onToggleStage    = vm::toggleStageExercise,
            onConfirmStaged  = vm::confirmStagedExercises,
            onRemoveExercise = { exId -> vm.removeExerciseFromTemplate(twx.template.id, exId) },
            onDismiss        = vm::closeTemplateEditor
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Black).statusBarsPadding()
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("WEEK PLAN", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
            TextButton(onClick = vm::goToCurrentWeek) {
                Text("TODAY", style = MaterialTheme.typography.labelMedium, color = Active)
            }
        }

        // ── Week calendar ─────────────────────────────────────────────────────
        WeekCalendar(
            weekStart    = state.weekStart,
            weekDays     = state.weekDays,
            selectedDay  = state.selectedDayIndex,
            today        = LocalDate.now(),
            onPrevWeek   = vm::prevWeek,
            onNextWeek   = vm::nextWeek,
            onSelectDay  = vm::selectDay
        )

        HorizontalDivider(color = Border, thickness = 0.5.dp)

        // ── Selected day toolbar ──────────────────────────────────────────────
        val selectedWeekDay = state.selectedWeekDay
        val isRest          = selectedWeekDay?.isRestDay == true

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "${DAY_LABELS[state.selectedDayIndex]}${selectedWeekDay?.let { " · ${it.date.format(MONTH_FMT)}" } ?: ""}",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Rest day chip — prominent toggle
                Box(
                    modifier = Modifier
                        .background(
                            if (isRest) Caution.copy(alpha = 0.18f) else Surface2,
                            RoundedCornerShape(20.dp)
                        )
                        .border(1.dp, if (isRest) Caution else Border, RoundedCornerShape(20.dp))
                        .clickable { vm.toggleRestDay() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        if (isRest) "😴 REST DAY  ✕" else "😴 REST DAY",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isRest) Caution else TextSecondary
                    )
                }
                if (!isRest) {
                    Box(
                        modifier = Modifier
                            .background(Performance.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .border(1.dp, Performance.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { vm.showAddTemplate() }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text("+ ADD", style = MaterialTheme.typography.labelMedium, color = Performance)
                    }
                }
            }
        }

        HorizontalDivider(color = Border, thickness = 0.5.dp)

        if (isRest) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😴", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Rest day", style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { vm.toggleRestDay() },
                        shape   = RoundedCornerShape(4.dp),
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = Caution)
                    ) {
                        Text("REMOVE REST DAY", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } else if (state.selectedTemplates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No workouts on ${DAY_LABELS[state.selectedDayIndex]}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDisabled
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = vm::showAddTemplate,
                        shape   = RoundedCornerShape(4.dp),
                        colors  = ButtonDefaults.buttonColors(containerColor = Active)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Black)
                        Spacer(Modifier.width(8.dp))
                        Text("ADD WORKOUT", color = Black, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = vm::toggleRestDay,
                        shape   = RoundedCornerShape(4.dp),
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("😴  MARK AS REST DAY", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.selectedTemplates, key = { it.template.id }) { twx ->
                    TemplateCard(
                        twx      = twx,
                        onEdit   = { vm.openTemplateEditor(twx) },
                        onDelete = { vm.deleteTemplate(twx.template.id) }
                    )
                }
            }
        }
    }
}

// ── Week calendar ─────────────────────────────────────────────────────────────

@Composable
private fun WeekCalendar(
    weekStart: java.time.LocalDate,
    weekDays: List<WeekDay>,
    selectedDay: Int,
    today: java.time.LocalDate,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectDay: (Int) -> Unit
) {
    val weekEnd    = weekStart.plusDays(6)
    val rangeLabel = "${weekStart.format(MONTH_FMT)} – ${weekEnd.format(MONTH_FMT)}"

    Column(modifier = Modifier.fillMaxWidth().background(Surface1).padding(vertical = 8.dp)) {
        // Week navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevWeek) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous week", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            Text(rangeLabel, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            IconButton(onClick = onNextWeek) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next week", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // Day cells
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (0..6).forEach { i ->
                val weekDay   = weekDays.getOrNull(i)
                val date      = weekStart.plusDays(i.toLong())
                val isToday   = date == today
                val isSelected = i == selectedDay
                val hasWorkout = (weekDay?.templates?.isNotEmpty()) == true
                val isRest    = weekDay?.isRestDay == true

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(when {
                            isSelected -> Active.copy(alpha = 0.2f)
                            isToday    -> Surface2
                            else       -> Color.Transparent
                        })
                        .border(
                            width = 1.dp,
                            color = when {
                                isSelected -> Active
                                isToday    -> Active.copy(alpha = 0.4f)
                                else       -> Color.Transparent
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectDay(i) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        SHORT_DAY[i],
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Active else TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            isSelected -> Active
                            isToday    -> TextPrimary
                            else       -> TextSecondary
                        }
                    )
                    Spacer(Modifier.height(3.dp))
                    // Status dot
                    Box(Modifier.size(5.dp).background(
                        when {
                            isRest     -> Caution
                            hasWorkout -> Performance
                            else       -> Color.Transparent
                        },
                        CircleShape
                    ))
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(5.dp).background(Performance, CircleShape))
            Spacer(Modifier.width(4.dp))
            Text("workout", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(5.dp).background(Caution, CircleShape))
            Spacer(Modifier.width(4.dp))
            Text("rest", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
        }
    }
}

// ── Template card ─────────────────────────────────────────────────────────────

@Composable
private fun TemplateCard(twx: TemplateWithExercises, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = Surface1,
            title  = { Text("Delete ${twx.template.name}?", color = TextPrimary) },
            text   = { Text("Removes template. Logged sessions unaffected.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("DELETE", color = Warning)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Surface1, RoundedCornerShape(4.dp))
            .border(1.dp, Border, RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                twx.template.name.uppercase(),
                style    = MaterialTheme.typography.headlineSmall,
                color    = TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Active, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Warning, modifier = Modifier.size(18.dp))
                }
            }
        }
        if (twx.exercises.isEmpty()) {
            Text("No exercises — tap edit to add", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
        } else {
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(twx.exercises) { ex ->
                    Box(
                        modifier = Modifier
                            .background(Surface2, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(ex.name.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ── Add template — 2-step fullscreen Dialog (no sheet physics) ───────────────

@Composable
private fun AddTemplateSheet(
    dayLabel: String,
    searchQuery: String,
    searchFilter: String,
    searchResults: List<com.myfit.core.data.db.entity.ExerciseEntity>,
    allExercises: List<com.myfit.core.data.db.entity.ExerciseEntity>,
    stagedIds: Set<String>,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onToggleStage: (com.myfit.core.data.db.entity.ExerciseEntity) -> Unit,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    val stagedExercises = remember(stagedIds, allExercises) { allExercises.filter { it.id in stagedIds } }

    Dialog(
        onDismissRequest = { if (step == 1 && stagedIds.isEmpty()) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Force Dialog window to MATCH_PARENT — without this maxHeight is unconstrained
        val dialogView = LocalView.current
        SideEffect {
            (dialogView.parent as? DialogWindowProvider)?.window
                ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        // systemBarsPadding ensures maxHeight = true safe-area height
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().systemBarsPadding()
        ) {
            val panelHeight = maxHeight - 64.dp   // 64dp scrim gap at top

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black.copy(alpha = 0.65f))
                    .clickable { if (step == 1 && stagedIds.isEmpty()) onDismiss() }
            )

            // Panel: exact bounded height — weight(1f) resolves correctly
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(panelHeight)
                    .background(Surface1, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (step == 1) {
                    StepOnePickExercises(
                        dayLabel       = dayLabel,
                        searchQuery    = searchQuery,
                        searchFilter   = searchFilter,
                        searchResults  = searchResults,
                        stagedIds      = stagedIds,
                        stagedExercises = stagedExercises,
                        onSearchChange = onSearchChange,
                        onFilterChange = onFilterChange,
                        onToggleStage  = onToggleStage,
                        onNext         = { step = 2 },
                        onCancel       = onDismiss
                    )
                } else {
                    StepTwoNameWorkout(
                        dayLabel        = dayLabel,
                        stagedExercises = stagedExercises,
                        name            = name,
                        onNameChange    = { name = it },
                        onBack          = { step = 1 },
                        onConfirm       = { onSave(name.ifBlank { "$dayLabel WORKOUT" }) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.StepOnePickExercises(
    dayLabel: String,
    searchQuery: String,
    searchFilter: String,
    searchResults: List<com.myfit.core.data.db.entity.ExerciseEntity>,
    stagedIds: Set<String>,
    stagedExercises: List<com.myfit.core.data.db.entity.ExerciseEntity>,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onToggleStage: (com.myfit.core.data.db.entity.ExerciseEntity) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    // Top bar (drag target hint)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text("ADD TO $dayLabel", style = MaterialTheme.typography.labelSmall, color = Active)
            Text("Pick exercises", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary)
        }
    }

    // Search + chips (fixed)
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        ExercisePickerHeader(searchQuery, searchFilter, onSearchChange, onFilterChange)
    }
    Spacer(Modifier.height(4.dp))

    // Scrollable list
    LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
        items(searchResults) { ex ->
            ExercisePickerRow(ex, isAdded = false, isStaged = ex.id in stagedIds, onToggle = { onToggleStage(ex) })
        }
    }

    // Pinned bottom
    HorizontalDivider(color = Border)
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)) {
        if (stagedIds.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                items(stagedExercises) { ex ->
                    Box(
                        modifier = Modifier
                            .background(Active.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .border(1.dp, Active.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(ex.name, style = MaterialTheme.typography.labelSmall, color = Active)
                    }
                }
            }
        }
        Button(
            onClick  = onNext,
            enabled  = stagedIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(4.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = Active,
                disabledContainerColor = Surface2
            )
        ) {
            Text(
                if (stagedIds.isEmpty()) "SELECT EXERCISES ABOVE"
                else "NEXT — NAME WORKOUT (${stagedIds.size} selected)  →",
                style = MaterialTheme.typography.headlineSmall,
                color = if (stagedIds.isNotEmpty()) Black else TextDisabled
            )
        }
    }
}

@Composable
private fun ColumnScope.StepTwoNameWorkout(
    dayLabel: String,
    stagedExercises: List<com.myfit.core.data.db.entity.ExerciseEntity>,
    name: String,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    // Top bar with back
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
        }
        Column {
            Text("ADD TO $dayLabel", style = MaterialTheme.typography.labelSmall, color = Active)
            Text("Name this workout", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }
    }

    // Content (scrollable if needed, fills space)
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            "${stagedExercises.size} EXERCISE${if (stagedExercises.size != 1) "S" else ""} SELECTED",
            style = MaterialTheme.typography.labelSmall,
            color = TextDisabled
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(stagedExercises) { ex ->
                Column(
                    modifier = Modifier
                        .background(Surface2, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(ex.name.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    Text(ex.primaryMuscle.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("WORKOUT NAME", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value         = name,
            onValueChange = onNameChange,
            placeholder   = { Text("e.g. PUSH A, LEG DAY, UPPER", color = TextDisabled) },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            shape         = RoundedCornerShape(4.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Performance, unfocusedBorderColor = Border,
                focusedTextColor     = TextPrimary, unfocusedTextColor = TextPrimary,
                cursorColor          = Performance
            )
        )
        Spacer(Modifier.height(6.dp))
        Text("Leave blank to auto-name", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
    }

    // Pinned ADD button — ALWAYS visible at bottom
    HorizontalDivider(color = Border)
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)) {
        Button(
            onClick  = onConfirm,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape    = RoundedCornerShape(4.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Performance)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Black)
            Spacer(Modifier.width(10.dp))
            Text(
                "ADD TO $dayLabel${if (name.isNotBlank()) "  —  ${name.uppercase()}" else ""}",
                style    = MaterialTheme.typography.headlineSmall,
                color    = Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Template editor sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateEditorSheet(
    twx: TemplateWithExercises,
    searchQuery: String,
    searchFilter: String,
    searchResults: List<com.myfit.core.data.db.entity.ExerciseEntity>,
    stagedIds: Set<String>,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onToggleStage: (com.myfit.core.data.db.entity.ExerciseEntity) -> Unit,
    onConfirmStaged: () -> Unit,
    onRemoveExercise: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val addedIds = twx.exercises.map { it.id }.toSet()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Surface1,
        modifier         = Modifier.heightIn(min = screenHeight * 0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .navigationBarsPadding()
        ) {
            // ── Fixed header ───────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(twx.template.name.uppercase(), style = MaterialTheme.typography.headlineLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary) }
                }

                // Existing exercises (compact chips)
                if (twx.exercises.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("IN WORKOUT", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(twx.exercises) { ex ->
                            Row(
                                modifier = Modifier
                                    .background(Surface2, RoundedCornerShape(4.dp))
                                    .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(ex.name.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                IconButton(onClick = { onRemoveExercise(ex.id) }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Warning, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("ADD EXERCISES", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                    if (stagedIds.isNotEmpty()) {
                        Text("${stagedIds.size} selected", style = MaterialTheme.typography.labelSmall, color = Performance)
                    }
                }
                Spacer(Modifier.height(8.dp))
                ExercisePickerHeader(searchQuery, searchFilter, onSearchChange, onFilterChange)
                Spacer(Modifier.height(4.dp))
            }

            // ── Scrollable exercise list ───────────────────────────────────
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
                items(searchResults) { ex ->
                    val isAdded  = ex.id in addedIds
                    val isStaged = ex.id in stagedIds
                    ExercisePickerRow(ex, isAdded, isStaged, onToggle = { if (!isAdded) onToggleStage(ex) })
                }
            }

            // ── Pinned confirm button (always visible) ─────────────────────
            HorizontalDivider(color = Border, thickness = 0.5.dp)
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                if (stagedIds.isEmpty()) {
                    Text(
                        "Select exercises above, then confirm",
                        style     = MaterialTheme.typography.labelSmall,
                        color     = TextDisabled,
                        modifier  = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick  = onConfirmStaged,
                    enabled  = stagedIds.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(4.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Performance,
                        disabledContainerColor = Surface2
                    )
                ) {
                    if (stagedIds.isNotEmpty()) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Black)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (stagedIds.isEmpty()) "SELECT EXERCISES TO ADD"
                        else "ADD ${stagedIds.size} EXERCISE${if (stagedIds.size != 1) "S" else ""} TO WORKOUT",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (stagedIds.isNotEmpty()) Black else TextDisabled
                    )
                }
            }
        }
    }
}

// ── Shared picker helpers ─────────────────────────────────────────────────────

@Composable
private fun ExercisePickerHeader(
    searchQuery: String,
    searchFilter: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = searchQuery,
        onValueChange = onSearchChange,
        placeholder   = { Text("Search exercises…", color = TextDisabled) },
        modifier      = Modifier.fillMaxWidth(),
        singleLine    = true,
        shape         = RoundedCornerShape(4.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor  = Active, unfocusedBorderColor = Border,
            focusedTextColor    = TextPrimary, unfocusedTextColor = TextPrimary,
            cursorColor         = Active
        )
    )
    Spacer(Modifier.height(8.dp))
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding        = PaddingValues(horizontal = 2.dp)
    ) {
        items(CATEGORIES) { cat ->
            val selected = cat == searchFilter
            Box(
                modifier = Modifier
                    .background(if (selected) Active.copy(alpha = 0.2f) else Surface2, RoundedCornerShape(20.dp))
                    .border(1.dp, if (selected) Active else Border, RoundedCornerShape(20.dp))
                    .clickable { onFilterChange(cat) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(cat, style = MaterialTheme.typography.labelMedium, color = if (selected) Active else TextSecondary)
            }
        }
    }
}

@Composable
private fun ExercisePickerRow(
    ex: com.myfit.core.data.db.entity.ExerciseEntity,
    isAdded: Boolean,
    isStaged: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isStaged) Active.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable(enabled = !isAdded, onClick = onToggle)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(ex.name, style = MaterialTheme.typography.bodyMedium, color = if (isAdded) TextDisabled else TextPrimary)
            Text(ex.primaryMuscle.uppercase(), style = MaterialTheme.typography.labelSmall, color = if (isAdded) TextDisabled else TextSecondary)
        }
        when {
            isAdded  -> Text("IN WORKOUT", style = MaterialTheme.typography.labelSmall, color = Performance)
            isStaged -> Box(Modifier.size(24.dp).background(Active, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Black, modifier = Modifier.size(14.dp))
            }
            else     -> Icon(Icons.Default.Add, contentDescription = null, tint = Active, modifier = Modifier.size(20.dp))
        }
    }
    HorizontalDivider(color = Border, thickness = 0.5.dp)
}
