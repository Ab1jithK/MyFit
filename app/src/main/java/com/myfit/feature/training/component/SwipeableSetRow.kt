package com.myfit.feature.training.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.myfit.core.data.db.entity.SetEntity
import com.myfit.core.ui.theme.*
import com.myfit.feature.training.LoggedSet
import com.myfit.feature.training.SetType

private const val SWIPE_THRESHOLD = 120f

@Composable
fun SwipeableSetRow(
    loggedSet: LoggedSet,
    onComplete: () -> Unit,
    onSetTypeChange: (SetType) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var showOptions by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (showOptions) 0f else offsetX,
        animationSpec = tween(durationMillis = 80),
        label = "swipe_offset"
    )

    val rowBackground by animateColorAsState(
        targetValue = when {
            loggedSet.isNew -> Performance.copy(alpha = 0.15f)
            offsetX > 60f   -> Performance.copy(alpha = 0.1f)
            offsetX < -60f  -> Active.copy(alpha = 0.1f)
            else            -> Surface1
        },
        animationSpec = tween(200),
        label = "row_bg"
    )

    if (showOptions) {
        SetTypeOptionsRow(
            currentType = SetType.entries.firstOrNull {
                it.name.lowercase() == loggedSet.entity.setType
            } ?: SetType.WORKING,
            onSelect = { type ->
                onSetTypeChange(type)
                showOptions = false
                offsetX = 0f
            },
            onDismiss = {
                showOptions = false
                offsetX = 0f
            }
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(rowBackground)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > SWIPE_THRESHOLD  -> { onComplete(); offsetX = 0f }
                                offsetX < -SWIPE_THRESHOLD -> { showOptions = true }
                                else                       -> { offsetX = 0f }
                            }
                        },
                        onDragCancel = { offsetX = 0f }
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount)
                            .coerceIn(-SWIPE_THRESHOLD * 1.5f, SWIPE_THRESHOLD * 1.5f)
                    }
                }
                .graphicsLayer { translationX = animatedOffset }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            SetRowContent(loggedSet.entity)
        }
    }
}

@Composable
private fun SetRowContent(set: SetEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = setTypeTag(set.setType),
            style = MaterialTheme.typography.labelMedium,
            color = setTypeColor(set.setType)
        )
        Text(
            text = formatWeight(set.weightKg),
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary
        )
        Text(text = "×", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Text(
            text = "${set.reps}",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary
        )
        Text(
            text = "#${set.setNumber}",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun SetTypeOptionsRow(
    currentType: SetType,
    onSelect: (SetType) -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Surface2)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetType.entries.forEach { type ->
            val isSelected = type == currentType
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        if (isSelected) Active.copy(alpha = 0.2f) else Surface1,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        if (isSelected) Active else Border,
                        RoundedCornerShape(4.dp)
                    )
                    .clickable(role = Role.Button) { onSelect(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Active else TextSecondary
                )
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "✕",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            modifier = Modifier.clickable { onDismiss() }
        )
    }
}

private fun setTypeTag(type: String) = when (type) {
    "warmup"  -> "W"
    "dropset" -> "D"
    "myorep"  -> "M"
    else      -> "✓"
}

private fun setTypeColor(type: String): Color = when (type) {
    "warmup"  -> Caution
    "dropset" -> Active
    "myorep"  -> Active
    else      -> Performance
}

private fun formatWeight(kg: Float): String =
    if (kg == kg.toLong().toFloat()) "${kg.toLong()} kg" else "${"%.1f".format(kg)} kg"
