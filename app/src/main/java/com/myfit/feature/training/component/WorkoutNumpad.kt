package com.myfit.feature.training.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myfit.core.ui.theme.*
import com.myfit.feature.training.NumpadTarget

private val NUMPAD_KEYS = listOf("7","8","9","4","5","6","1","2","3",".","0","⌫")

@Composable
fun WorkoutNumpad(
    weightInput: String,
    repsInput: String,
    activeInput: NumpadTarget,
    onKeyTap: (String) -> Unit,
    onBackspace: () -> Unit,
    onWeightFocus: () -> Unit,
    onRepsFocus: () -> Unit,
    onLogSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface1)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Weight / Reps display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InputDisplay(
                value = weightInput,
                label = "KG",
                isActive = activeInput == NumpadTarget.WEIGHT,
                onClick = onWeightFocus,
                modifier = Modifier.weight(1f)
            )
            InputDisplay(
                value = repsInput,
                label = "REPS",
                isActive = activeInput == NumpadTarget.REPS,
                onClick = onRepsFocus,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        // 4×3 numpad grid
        NUMPAD_KEYS.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    NumpadKey(
                        label = key,
                        onClick = { if (key == "⌫") onBackspace() else onKeyTap(key) },
                        modifier = Modifier.weight(1f).height(56.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        // LOG SET — 64dp per spec
        Button(
            onClick = onLogSet,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Performance)
        ) {
            Text(
                text = "LOG SET",
                style = MaterialTheme.typography.headlineMedium,
                color = Black
            )
        }
    }
}

@Composable
private fun InputDisplay(
    value: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .border(1.dp, if (isActive) Performance else Border, RoundedCornerShape(4.dp))
            .background(if (isActive) Surface2 else Surface1, RoundedCornerShape(4.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = if (isActive) Performance else TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun NumpadKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Surface2, RoundedCornerShape(4.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}
