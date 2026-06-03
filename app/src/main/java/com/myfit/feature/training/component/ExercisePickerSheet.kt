package com.myfit.feature.training.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myfit.core.data.db.entity.ExerciseEntity
import com.myfit.core.ui.theme.*

private val FILTER_CATEGORIES = listOf("ALL","CHEST","BACK","SHOULDERS","LEGS","ARMS","CORE")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(
    query: String,
    results: List<ExerciseEntity>,
    selectedFilter: String = "ALL",
    onQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit = {},
    onSelect: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface1,
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                HorizontalDivider(Modifier.width(36.dp), color = Border, thickness = 4.dp)
            }
        }
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Text(
                "SELECT EXERCISE",
                style    = MaterialTheme.typography.headlineMedium,
                color    = TextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            OutlinedTextField(
                value         = query,
                onValueChange = onQueryChange,
                placeholder   = { Text("Search…", style = MaterialTheme.typography.bodyMedium) },
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape         = RoundedCornerShape(4.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Active, unfocusedBorderColor = Border,
                    cursorColor          = Active, focusedTextColor = TextPrimary,
                    unfocusedTextColor   = TextPrimary
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            // Muscle group filter chips
            LazyRow(
                contentPadding      = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(FILTER_CATEGORIES) { cat ->
                    val selected = cat == selectedFilter
                    Box(
                        modifier = Modifier
                            .background(
                                if (selected) Active.copy(alpha = 0.2f) else Surface2,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onFilterChange(cat) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            cat,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Active else TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "${results.size} exercises",
                style    = MaterialTheme.typography.labelSmall,
                color    = TextDisabled,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(results, key = { it.id }) { exercise ->
                    ExercisePickerRow(exercise = exercise, onClick = { onSelect(exercise) })
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerRow(exercise: ExerciseEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth().height(64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(exercise.name.uppercase(), style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Text(exercise.primaryMuscle.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Text(exercise.movementPattern?.uppercase() ?: "", style = MaterialTheme.typography.labelSmall, color = Active.copy(alpha = 0.7f))
    }
}
