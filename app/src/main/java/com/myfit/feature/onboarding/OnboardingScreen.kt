package com.myfit.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myfit.core.ui.theme.*

private enum class OnboardingStep { INTENT, PERMISSIONS }
private enum class IntentPath { CREATE_MESOCYCLE, QUICK_START, IMPORT_DATA }

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableStateOf(OnboardingStep.INTENT) }

    AnimatedContent(
        targetState = step,
        transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } },
        label = "onboarding_step"
    ) { currentStep ->
        when (currentStep) {
            OnboardingStep.INTENT -> IntentScreen(
                onPathSelected = { step = OnboardingStep.PERMISSIONS }
            )
            OnboardingStep.PERMISSIONS -> PermissionsScreen(onContinue = onComplete)
        }
    }
}

@Composable
private fun IntentScreen(onPathSelected: (IntentPath) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "MYFIT", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "SELECT TRAINING MODE",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(48.dp))

        IntentCard(
            label = "CREATE MESOCYCLE",
            description = "Design a structured training program with weekly periodization",
            accentColor = Performance,
            onClick = { onPathSelected(IntentPath.CREATE_MESOCYCLE) }
        )
        Spacer(Modifier.height(16.dp))
        IntentCard(
            label = "QUICK START",
            description = "Begin a blank workout session immediately",
            accentColor = Active,
            onClick = { onPathSelected(IntentPath.QUICK_START) }
        )
        Spacer(Modifier.height(16.dp))
        IntentCard(
            label = "IMPORT DATA",
            description = "Restore from local CSV or JSON backup",
            accentColor = Caution,
            onClick = { onPathSelected(IntentPath.IMPORT_DATA) }
        )
    }
}

@Composable
private fun IntentCard(
    label: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor
            )
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PermissionsScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "SENSOR ACCESS", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "REQUIRED FOR LOCAL AI ENGINE",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(32.dp))

        PermissionRow("Heart Rate", "Real-time HR and recovery curve calculation")
        PermissionRow("Activity", "Workout detection and volume estimation")
        PermissionRow("Body Metrics", "Bodyweight normalization for relative strength")

        Spacer(Modifier.height(20.dp))

        Surface(
            color = Surface2,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "Raw biometrics are parsed strictly within local device memory. No data leaves this device. No accounts. No internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Start
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Performance)
        ) {
            Text(
                text = "GRANT ACCESS + CONTINUE",
                style = MaterialTheme.typography.headlineMedium,
                color = Black
            )
        }
    }
}

@Composable
private fun PermissionRow(name: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .size(8.dp)
                .border(1.dp, Performance, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(name, style = MaterialTheme.typography.headlineSmall)
            Text(detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
