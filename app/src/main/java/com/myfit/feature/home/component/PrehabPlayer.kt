package com.myfit.feature.home.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.myfit.core.domain.PrehabMove
import com.myfit.core.ui.theme.*

@Composable
fun PrehabPlayer(
    moves: List<PrehabMove>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Surface1)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "PREHAB / MOBILITY",
                style = MaterialTheme.typography.labelLarge,
                color = Caution
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${moves.size} moves",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled
            )
        }

        Spacer(Modifier.height(8.dp))

        if (moves.isEmpty()) {
            Text(
                text = "Add exercises to your template to unlock prehab recommendations",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDisabled,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                itemsIndexed(moves) { index, move ->
                    PrehabMoveCard(move = move, index = index)
                }
            }
        }
    }
}

@Composable
private fun PrehabMoveCard(move: PrehabMove, index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_pulse_$index")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800 + index * 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale_$index"
    )

    Box(
        modifier = Modifier
            .width(140.dp)
            .fillMaxHeight()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(4.dp))
            .background(Surface2)
            .border(1.dp, Caution.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "${move.sets} × ${move.repsOrSeconds}",
                style = MaterialTheme.typography.labelSmall,
                color = Caution
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = move.name.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                maxLines = 2
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = move.cue,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 2
            )
        }
    }
}
