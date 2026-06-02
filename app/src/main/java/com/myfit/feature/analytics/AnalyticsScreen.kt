package com.myfit.feature.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myfit.core.ui.component.chart.BarChart
import com.myfit.core.ui.component.chart.LineChart
import com.myfit.core.ui.theme.*

private enum class AnalyticsTab(val label: String) {
    VOLUME("VOLUME"), STRENGTH("STRENGTH"), CNS("CNS")
}

@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf(AnalyticsTab.VOLUME) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        Text(
            text = "ANALYTICS",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Tab strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface1)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalyticsTab.entries.forEach { tab ->
                val selected = tab == activeTab
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) Active.copy(alpha = 0.2f) else Surface2,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { activeTab = tab }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) Active else TextSecondary
                    )
                }
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Active, modifier = Modifier.size(32.dp))
            }
        } else {
            when (activeTab) {
                AnalyticsTab.VOLUME   -> VolumeTab(state)
                AnalyticsTab.STRENGTH -> StrengthTab(state, vm::selectExercise)
                AnalyticsTab.CNS      -> CnsTab(state)
            }
        }
    }
}

@Composable
private fun VolumeTab(state: AnalyticsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionHeader("VOLUME BY MUSCLE", "Last 28 days · kg × reps")
        Spacer(Modifier.height(12.dp))
        BarChart(bars = state.volumeBars, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun StrengthTab(state: AnalyticsUiState, onSelectExercise: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionHeader("ESTIMATED 1RM", "Last 90 days · Epley formula")
        Spacer(Modifier.height(8.dp))

        if (state.availableExercises.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            val selectedName = state.availableExercises
                .firstOrNull { it.id == state.selectedExerciseId }?.name ?: "Select exercise"

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Text(
                        text = selectedName.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Active
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Surface2)
                ) {
                    state.availableExercises.forEach { ex ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    ex.name.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextPrimary
                                )
                            },
                            onClick = { onSelectExercise(ex.id); expanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        LineChart(
            points = state.e1rmPoints,
            lineColor = Performance,
            modifier = Modifier.fillMaxWidth(),
            yLabelFormat = { "${it.toInt()} kg" }
        )
    }
}

@Composable
private fun CnsTab(state: AnalyticsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionHeader("CNS RECOVERY TREND", "Last 14 days · fatigue score 0–1")
        Spacer(Modifier.height(12.dp))
        LineChart(
            points = state.cnsPoints,
            lineColor = Active,
            modifier = Modifier.fillMaxWidth(),
            yLabelFormat = { "${"%.0f".format(it * 100)}%" }
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(Performance, "Low (recovery)")
            LegendItem(Caution,    "Moderate")
            LegendItem(Warning,    "High (fatigue)")
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title,    style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Text(subtitle, style = MaterialTheme.typography.labelMedium,    color = TextSecondary)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
