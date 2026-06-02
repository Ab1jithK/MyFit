package com.myfit.feature.training.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myfit.core.ui.theme.*
import com.myfit.feature.training.RestState

@Composable
fun RestTimerBar(
    restState: RestState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = restState != RestState.Idle,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it },
        modifier = modifier
    ) {
        when (restState) {
            RestState.Idle -> Unit

            is RestState.Counting -> {
                val progress by animateFloatAsState(
                    targetValue = restState.progress,
                    animationSpec = tween(durationMillis = 900),
                    label = "rest_progress"
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface2)
                        .clickable { onDismiss() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("REST", style = MaterialTheme.typography.labelLarge, color = Active)
                        Text(
                            text = formatSeconds(restState.remainingSeconds),
                            style = MaterialTheme.typography.displaySmall,
                            color = timerColor(restState.remainingSeconds)
                        )
                        Text(
                            "TAP TO SKIP",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDisabled
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = timerColor(restState.remainingSeconds),
                        trackColor = Border
                    )
                }
            }

            RestState.Elapsed -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Performance.copy(alpha = 0.12f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REST COMPLETE — GO",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Performance
                    )
                    Text(
                        "TAP TO DISMISS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDisabled
                    )
                }
            }
        }
    }
}

private fun formatSeconds(s: Int) = "%d:%02d".format(s / 60, s % 60)

private fun timerColor(remaining: Int) = when {
    remaining > 60 -> Active
    remaining > 20 -> Caution
    else           -> Warning
}
