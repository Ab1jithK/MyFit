package com.myfit.feature.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myfit.core.data.db.entity.FoodItemEntity
import com.myfit.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(vm: NutritionViewModel = hiltViewModel()) {
    val state       by vm.state.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    var showCustomSheet by remember { mutableStateOf(false) }

    state.pendingItem?.let { item ->
        ConfirmSheet(
            item      = item,
            onConfirm = { grams, slot -> vm.confirmLog(item, grams, slot) },
            onDismiss = vm::dismissConfirm
        )
    }

    if (showCustomSheet) {
        AddCustomFoodSheet(
            onSave = { name, cal, protein, carbs, fat ->
                vm.addCustomFood(name, cal, protein, carbs, fat)
                showCustomSheet = false
            },
            onDismiss = { showCustomSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("NUTRITION", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            MacroSummaryRow(state.dailyMacros)
            Spacer(Modifier.height(8.dp))
            // Search bar
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = vm::setSearchQuery,
                placeholder   = { Text("Search foods…", color = TextDisabled) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(4.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Active,
                    unfocusedBorderColor = Border,
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary,
                    cursorColor          = Active
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        TextButton(onClick = { vm.setSearchQuery("") }) {
                            Text("✕", color = TextSecondary)
                        }
                    }
                }
            )
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Active, modifier = Modifier.size(32.dp))
            }
            return@Column
        }

        if (searchQuery.isNotEmpty()) {
            // Search results view
            Row(
                modifier = Modifier.fillMaxWidth().background(Surface1)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RESULTS (${state.searchResults.size})", style = MaterialTheme.typography.labelLarge, color = Active)
                TextButton(onClick = { showCustomSheet = true }) {
                    Text("+ CUSTOM FOOD", style = MaterialTheme.typography.labelMedium, color = Performance)
                }
            }
            if (state.searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No foods found", style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showCustomSheet = true },
                            shape  = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Performance)
                        ) {
                            Text("ADD CUSTOM FOOD", color = Black, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.searchResults, key = { it.id }) { food ->
                        QuickItemCard(
                            item    = QuickItem(food),
                            onClick = { vm.selectQuickItem(QuickItem(food)) }
                        )
                    }
                }
            }
        } else {
            // Normal Quick Stack view
            Row(
                modifier = Modifier.fillMaxWidth().background(Surface1)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("QUICK STACK", style = MaterialTheme.typography.labelLarge, color = Active)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showCustomSheet = true }) {
                        Text("+ CUSTOM", style = MaterialTheme.typography.labelSmall, color = Performance)
                    }
                    Text("TAP TO LOG", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(0.45f),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.quickItems, key = { it.food.id }) { item ->
                    QuickItemCard(item = item, onClick = { vm.selectQuickItem(item) })
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Surface1)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("TODAY'S LOG", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            }

            if (state.todayEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(0.55f).padding(32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("No entries yet today", style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(0.55f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(state.todayEntries, key = { it.log.id }) { entry ->
                        LogEntryRow(entry)
                        HorizontalDivider(color = Border, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroSummaryRow(macros: DailyMacros) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Surface1, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MacroChip("${macros.calories.toInt()}", "KCAL", Performance)
        MacroChip("${macros.proteinG.toInt()}g", "PROTEIN", Active)
        MacroChip("${macros.carbsG.toInt()}g", "CARBS", Caution)
        MacroChip("${macros.fatG.toInt()}g", "FAT", TextSecondary)
    }
}

@Composable
private fun MacroChip(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun QuickItemCard(item: QuickItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(72.dp)
            .background(Surface1, RoundedCornerShape(4.dp))
            .border(1.dp, Border, RoundedCornerShape(4.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Text(
                text     = item.food.name.uppercase(),
                style    = MaterialTheme.typography.bodyMedium,
                color    = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text  = "${item.food.proteinPer100.toInt()}g P · ${item.food.caloriesPer100.toInt()} kcal",
                style = MaterialTheme.typography.labelSmall,
                color = Active
            )
        }
    }
}

@Composable
private fun LogEntryRow(entry: NutritionLogEntry) {
    val fmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(fmt.format(Date(entry.log.loggedAt)), style = MaterialTheme.typography.labelLarge, color = TextSecondary, modifier = Modifier.width(44.dp))
        Text(entry.food.name.uppercase(), style = MaterialTheme.typography.bodyMedium, color = TextPrimary,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${entry.log.quantityG.toInt()}g", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Spacer(Modifier.width(8.dp))
        Text("${entry.calories.toInt()} kcal", style = MaterialTheme.typography.labelLarge, color = Performance)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmSheet(item: QuickItem, onConfirm: (grams: Float, mealSlot: String?) -> Unit, onDismiss: () -> Unit) {
    val servingOptions = listOf(50f, 100f, 150f, 200f, 250f, 300f)
    var selectedGrams by remember { mutableFloatStateOf(item.defaultGrams) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface1) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(item.food.name.uppercase(), style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            val factor = selectedGrams / 100f
            Text(
                "${(item.food.caloriesPer100 * factor).toInt()} kcal · P ${(item.food.proteinPer100 * factor).toInt()}g · C ${(item.food.carbsPer100 * factor).toInt()}g · F ${(item.food.fatPer100 * factor).toInt()}g",
                style = MaterialTheme.typography.labelLarge, color = TextSecondary
            )
            Spacer(Modifier.height(16.dp))
            Text("SERVING SIZE", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                servingOptions.forEach { grams ->
                    val selected = selectedGrams == grams
                    Box(
                        modifier = Modifier.size(52.dp)
                            .background(if (selected) Active.copy(alpha = 0.2f) else Surface2, RoundedCornerShape(4.dp))
                            .border(1.dp, if (selected) Active else Border, RoundedCornerShape(4.dp))
                            .clickable { selectedGrams = grams },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${grams.toInt()}g", style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Active else TextSecondary, textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick  = { onConfirm(selectedGrams, null) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape  = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Performance)
            ) {
                Text("LOG NOW", style = MaterialTheme.typography.headlineMedium, color = Black)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomFoodSheet(
    onSave: (name: String, cal: Float, protein: Float, carbs: Float, fat: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var name    by remember { mutableStateOf("") }
    var cal     by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs   by remember { mutableStateOf("") }
    var fat     by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && cal.toFloatOrNull() != null

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface1) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("CUSTOM FOOD", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
            Text("Per 100g values", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
            Spacer(Modifier.height(16.dp))

            MacroField("Food name", name, KeyboardType.Text) { name = it }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { MacroField("Calories", cal, KeyboardType.Decimal) { cal = it } }
                Box(Modifier.weight(1f)) { MacroField("Protein (g)", protein, KeyboardType.Decimal) { protein = it } }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { MacroField("Carbs (g)", carbs, KeyboardType.Decimal) { carbs = it } }
                Box(Modifier.weight(1f)) { MacroField("Fat (g)", fat, KeyboardType.Decimal) { fat = it } }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick  = {
                    onSave(name, cal.toFloatOrNull() ?: 0f, protein.toFloatOrNull() ?: 0f,
                           carbs.toFloatOrNull() ?: 0f, fat.toFloatOrNull() ?: 0f)
                },
                enabled  = isValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape  = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Performance)
            ) {
                Text("SAVE FOOD", style = MaterialTheme.typography.headlineSmall, color = Black)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MacroField(label: String, value: String, keyboardType: KeyboardType, onChange: (String) -> Unit) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label, color = TextDisabled, style = MaterialTheme.typography.labelSmall) },
        modifier      = Modifier.fillMaxWidth(),
        singleLine    = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape  = RoundedCornerShape(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Active,
            unfocusedBorderColor = Border,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = Active
        )
    )
}
