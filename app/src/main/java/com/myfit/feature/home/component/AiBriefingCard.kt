package com.myfit.feature.home.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.myfit.core.domain.model.BriefingBullet
import com.myfit.core.domain.model.BulletColor
import com.myfit.core.ui.theme.*

@Composable
fun AiBriefingCard(
    bullets: List<BriefingBullet>,
    modifier: Modifier = Modifier
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Surface1)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "AI BRIEFING",
                style = MaterialTheme.typography.labelLarge,
                color = Active
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(pulseAlpha)
                    .background(Active, CircleShape)
            )
        }

        Spacer(Modifier.height(10.dp))

        bullets.forEach { bullet ->
            BulletRow(bullet)
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun BulletRow(bullet: BriefingBullet) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(7.dp)
                .background(bullet.color.toCompose(), CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = bullet.text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun BulletColor.toCompose(): Color = when (this) {
    BulletColor.GREEN -> Performance
    BulletColor.AMBER -> Caution
    BulletColor.RED   -> Warning
    BulletColor.BLUE  -> Active
}
