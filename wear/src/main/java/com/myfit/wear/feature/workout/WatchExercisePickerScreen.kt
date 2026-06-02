package com.myfit.wear.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*

private val Black       = Color(0xFF000000)
private val Surface1    = Color(0xFF111111)
private val Performance = Color(0xFF00FF87)
private val TextPrimary = Color(0xFFEEEEEE)

@Composable
fun WatchExercisePickerScreen(
    onSelect: (exerciseId: String) -> Unit,
    vm: WatchWorkoutViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Black), contentAlignment = Alignment.TopCenter) {
        Column {
            Text(
                "PICK EXERCISE",
                color         = Performance,
                fontSize      = 12.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier      = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                textAlign     = TextAlign.Center
            )

            ScalingLazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(state.exercises) { exercise ->
                    Chip(
                        onClick   = {
                            vm.selectExercise(exercise)
                            onSelect(exercise.id)
                        },
                        label     = { Text(exercise.name, fontSize = 12.sp, color = TextPrimary) },
                        modifier  = Modifier.fillMaxWidth(),
                        colors    = ChipDefaults.chipColors(backgroundColor = Surface1)
                    )
                }
            }
        }
    }
}
