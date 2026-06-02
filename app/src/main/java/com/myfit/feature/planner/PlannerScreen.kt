package com.myfit.feature.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myfit.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(vm: PlannerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    // Add template sheet
    if (state.showAddTemplate) {
        AddTemplateSheet(onSave = vm::addTemplate, onDismiss = vm::hideAddTemplate)
    }

    // Template editor sheet
    state.editingTemplate?.let { twx ->
        TemplateEditorSheet(
            twx             = twx,
            searchQuery     = state.exercisePickerQuery,
            searchResults   = state.exerciseSearchResults,
            onSearchChange  = vm::searchExercises,
            onAddExercise   = vm::addExerciseToTemplate,
            onRemoveExercise = { exId -> vm.removeExerciseFromTemplate(twx.template.id, exId) },
            onDismiss       = vm::closeTemplateEditor
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Black).statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("WEEK PLAN", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
            IconButton(onClick = vm::showAddTemplate) {
                Icon(Icons.Default.Add, contentDescription = "Add template", tint = Performance)
            }
        }

        // Day picker
        LazyRow(
            modifier = Modifier.fillMaxWidth().background(Surface1).padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(7) { day ->
                val selected = state.selectedDay == day
                val hasWorkout = (state.byDay[day]?.isNotEmpty()) == true
                Column(
                    modifier = Modifier
                        .background(
                            if (selected) Active.copy(alpha = 0.2f) else Surface2,
                            RoundedCornerShape(4.dp)
                        )
                        .border(1.dp, if (selected) Active else if (hasWorkout) Performance.copy(alpha = 0.4f) else Border, RoundedCornerShape(4.dp))
                        .clickable { vm.selectDay(day) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(DAY_LABELS[day], style = MaterialTheme.typography.labelLarge, color = if (selected) Active else TextSecondary)
                    if (hasWorkout) {
                        Spacer(Modifier.height(3.dp))
                        Box(Modifier.size(4.dp).background(Performance, RoundedCornerShape(2.dp)))
                    }
                }
            }
        }

        // Selected day label + add button
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${DAY_LABELS[state.selectedDay]} — ${state.selectedTemplates.size} workout${if (state.selectedTemplates.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelLarge, color = TextSecondary
            )
            TextButton(onClick = vm::showAddTemplate) {
                Text("+ ADD WORKOUT", style = MaterialTheme.typography.labelMedium, color = Performance)
            }
        }

        HorizontalDivider(color = Border, thickness = 0.5.dp)

        if (state.selectedTemplates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No workouts on ${DAY_LABELS[state.selectedDay]}", style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = vm::showAddTemplate,
                        shape  = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Active)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Black)
                        Spacer(Modifier.width(8.dp))
                        Text("ADD WORKOUT", color = Black, style = MaterialTheme.typography.labelLarge)
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
                        twx       = twx,
                        onEdit    = { vm.openTemplateEditor(twx) },
                        onDelete  = { vm.deleteTemplate(twx.template.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(twx: TemplateWithExercises, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = Surface1,
            title = { Text("Delete ${twx.template.name}?", color = TextPrimary) },
            text  = { Text("This removes the template. Logged workouts are unaffected.", color = TextSecondary) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTemplateSheet(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface1) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("NEW WORKOUT", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("Workout name (e.g. PUSH A)", color = TextDisabled, style = MaterialTheme.typography.labelSmall) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(4.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Active, unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Active
                )
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick  = { onSave(name) },
                enabled  = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape  = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Performance)
            ) {
                Text("CREATE", style = MaterialTheme.typography.headlineSmall, color = Black)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateEditorSheet(
    twx: TemplateWithExercises,
    searchQuery: String,
    searchResults: List<com.myfit.core.data.db.entity.ExerciseEntity>,
    onSearchChange: (String) -> Unit,
    onAddExercise: (com.myfit.core.data.db.entity.ExerciseEntity) -> Unit,
    onRemoveExercise: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface1) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(twx.template.name.uppercase(), style = MaterialTheme.typography.headlineLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary) }
            }

            // Current exercises
            if (twx.exercises.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("EXERCISES", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                Spacer(Modifier.height(6.dp))
                twx.exercises.forEach { ex ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ex.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemoveExercise(ex.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Warning, modifier = Modifier.size(16.dp))
                        }
                    }
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("ADD EXERCISES", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
            Spacer(Modifier.height(6.dp))

            // Exercise search
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = onSearchChange,
                placeholder   = { Text("Search exercises…", color = TextDisabled) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(4.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Active, unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Active
                )
            )
            Spacer(Modifier.height(8.dp))

            // Search results (already-added ones shown greyed)
            val addedIds = twx.exercises.map { it.id }.toSet()
            LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                items(searchResults) { ex ->
                    val alreadyAdded = ex.id in addedIds
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !alreadyAdded) { onAddExercise(ex) }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ex.name, style = MaterialTheme.typography.bodyMedium, color = if (alreadyAdded) TextDisabled else TextPrimary)
                            Text(ex.primaryMuscle.uppercase(), style = MaterialTheme.typography.labelSmall, color = if (alreadyAdded) TextDisabled else TextSecondary)
                        }
                        if (alreadyAdded)
                            Text("ADDED", style = MaterialTheme.typography.labelSmall, color = Performance)
                        else
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Active, modifier = Modifier.size(20.dp))
                    }
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
