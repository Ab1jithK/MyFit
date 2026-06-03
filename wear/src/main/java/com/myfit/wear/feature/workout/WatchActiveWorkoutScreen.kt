package com.myfit.wear.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*

private val Black = Color(0xFF000000)

@Composable
fun WatchActiveWorkoutScreen(
    sessionId: String,
    onPickExercise: () -> Unit,
    onFinish: () -> Unit,
    vm: WatchWorkoutViewModel = hiltViewModel()
) {
    val inputState by vm.inputState.collectAsStateWithLifecycle()
    val logState   by vm.logState.collectAsStateWithLifecycle()
    var showLog    by remember { mutableStateOf(false) }

    val focusReq = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusReq.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .onRotaryScrollEvent { event ->
                vm.rotateBezel(event.verticalScrollPixels)
                true
            }
            .focusRequester(focusReq)
            .focusable()
    ) {
        Scaffold(
            modifier = Modifier.background(Black),
            timeText = { TimeText() },
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
        ) {
            if (showLog) {
                WatchWorkoutLogScreen(
                    logState             = logState,
                    currentExerciseId    = inputState.currentExercise?.id,
                    onSelectSetForEdit   = vm::selectSetForEdit,
                    onTapEditNumpad      = vm::tapEditNumpad,
                    onBackspaceEdit      = vm::backspaceEdit,
                    onSetEditActiveInput = vm::setEditActiveInput,
                    onConfirmEdit        = vm::confirmEdit,
                    onDeleteEditingSet   = vm::deleteEditingSet,
                    onDismissEdit        = vm::dismissEdit,
                    onPickExercise       = onPickExercise,
                    onFinish             = { vm.finishWorkout(onFinish) },
                    onBack               = { showLog = false }
                )
            } else {
                WatchWorkoutInputScreen(
                    inputState       = inputState,
                    onTapNumpad      = vm::tapNumpad,
                    onBackspace      = vm::backspace,
                    onSetActiveInput = vm::setActiveInput,
                    onLogSet         = vm::logSet,
                    onDismissRest    = vm::dismissRestTimer,
                    onPickExercise   = onPickExercise,
                    onViewLog        = { showLog = true }
                )
            }
        }
    }
}
