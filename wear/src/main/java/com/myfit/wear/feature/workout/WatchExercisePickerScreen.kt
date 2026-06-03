package com.myfit.wear.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*

private val Black       = Color(0xFF000000)
private val Surface1    = Color(0xFF111111)
private val Surface2    = Color(0xFF1A1A1A)
private val Performance = Color(0xFF00FF87)
private val Active      = Color(0xFF00B4FF)
private val TextPrimary = Color(0xFFEEEEEE)
private val TextMuted   = Color(0xFF888888)

@Composable
fun WatchExercisePickerScreen(
    onSelect: (exerciseId: String) -> Unit,
    vm: WatchWorkoutViewModel = hiltViewModel()
) {
    val state     by vm.state.collectAsStateWithLifecycle()
    val listState  = rememberScalingLazyListState()

    Scaffold(
        modifier          = Modifier.background(Black),
        timeText          = { TimeText() },
        vignette          = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    "PICK EXERCISE",
                    color         = Performance,
                    fontSize      = 13.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            // Muscle-group filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding        = PaddingValues(horizontal = 4.dp)
                ) {
                    rowItems(WATCH_FILTERS) { cat ->
                        val selected = cat == state.exerciseFilter
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selected) Active.copy(alpha = 0.25f) else Surface2,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { vm.setExerciseFilter(cat) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                cat,
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (selected) Active else TextMuted
                            )
                        }
                    }
                }
            }

            items(state.exercises) { exercise ->
                Chip(
                    onClick = {
                        vm.selectExercise(exercise)
                        onSelect(exercise.id)
                    },
                    label  = {
                        Text(exercise.name, fontSize = 12.sp, color = TextPrimary)
                    },
                    secondaryLabel = {
                        Text(exercise.primaryMuscle.uppercase(), fontSize = 9.sp, color = TextMuted)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ChipDefaults.chipColors(
                        backgroundColor       = Surface1,
                        secondaryContentColor = TextMuted
                    )
                )
            }
        }
    }
}
