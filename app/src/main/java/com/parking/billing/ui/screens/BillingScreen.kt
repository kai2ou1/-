package com.parking.billing.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parking.billing.FeeBreakdown
import com.parking.billing.ParkingCalculator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(modifier: Modifier = Modifier) {
    var entryDate by remember { mutableStateOf(LocalDate.now()) }
    var entryTime by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var exitDate by remember { mutableStateOf(LocalDate.now()) }
    var exitTime by remember { mutableStateOf(LocalTime.of(18, 0)) }
    var showEntryDatePicker by remember { mutableStateOf(false) }
    var showExitDatePicker by remember { mutableStateOf(false) }
    var showEntryTimePicker by remember { mutableStateOf(false) }
    var showExitTimePicker by remember { mutableStateOf(false) }
    var feeBreakdown by remember { mutableStateOf<FeeBreakdown?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    if (showEntryDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = entryDate.atStartOfDay()
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEntryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        entryDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showEntryDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showEntryDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showExitDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = exitDate.atStartOfDay()
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showExitDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        exitDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showExitDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEntryTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = entryTime.hour,
            initialMinute = entryTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showEntryTimePicker = false },
            title = { Text("选择入场时间") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    entryTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showEntryTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showEntryTimePicker = false }) { Text("取消") }
            }
        )
    }

    if (showExitTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = exitTime.hour,
            initialMinute = exitTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showExitTimePicker = false },
            title = { Text("选择出场时间") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    exitTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showExitTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showExitTimePicker = false }) { Text("取消") }
            }
        )
    }

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocalParking,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "停车计费系统",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Parking Fee Calculator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Time input section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "停车时间",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Entry time
                DateTimeRow(
                    label = "入场时间",
                    dateText = entryDate.format(dateFormatter),
                    timeText = entryTime.format(timeFormatter),
                    onDateClick = { showEntryDatePicker = true },
                    onTimeClick = { showEntryTimePicker = true }
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Exit time
                DateTimeRow(
                    label = "出场时间",
                    dateText = exitDate.format(dateFormatter),
                    timeText = exitTime.format(timeFormatter),
                    onDateClick = { showExitDatePicker = true },
                    onTimeClick = { showExitTimePicker = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculate button
        Button(
            onClick = {
                errorMessage = null
                val entry = LocalDateTime.of(entryDate, entryTime)
                val exit = LocalDateTime.of(exitDate, exitTime)
                when {
                    exit.isBefore(entry) -> {
                        errorMessage = "出场时间不能早于入场时间"
                        feeBreakdown = null
                    }
                    else -> {
                        try {
                            feeBreakdown = ParkingCalculator.calculate(entry, exit)
                        } catch (e: IllegalArgumentException) {
                            errorMessage = e.message
                            feeBreakdown = null
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "计算费用",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Error message
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Fee result
        AnimatedVisibility(
            visible = feeBreakdown != null,
            enter = fadeIn() + slideInVertically()
        ) {
            feeBreakdown?.let { breakdown ->
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    FeeResultCard(breakdown = breakdown)
                    Spacer(modifier = Modifier.height(16.dp))
                    FeeRulesCard()
                }
            }
        }
    }
}

@Composable
private fun DateTimeRow(
    label: String,
    dateText: String,
    timeText: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = onDateClick,
                label = { Text(dateText) },
                leadingIcon = {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            AssistChip(
                onClick = onTimeClick,
                label = { Text(timeText) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun FeeResultCard(breakdown: FeeBreakdown) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                breakdown.isFree -> MaterialTheme.colorScheme.errorContainer
                        .let { MaterialTheme.colorScheme.primaryContainer }
                breakdown.isCapped -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "计算结果",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "停放时长",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = formatDuration(breakdown),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
            )

            // Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "应收费用",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = formatFeeLarge(breakdown.totalFee),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        breakdown.isFree -> MaterialTheme.colorScheme.onPrimaryContainer
                        breakdown.isCapped -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fee description
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
            ) {
                Text(
                    text = ParkingCalculator.formatFeeDescription(breakdown),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            // Breakdown detail
            if (!breakdown.isFree && breakdown.totalFee > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                BreakdownDetail(breakdown)
            }
        }
    }
}

@Composable
private fun BreakdownDetail(breakdown: FeeBreakdown) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "费用明细",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (breakdown.extraHours > 0) {
                DetailRow(
                    label = "计费时长（含超时）",
                    value = "${breakdown.displayHours}小时",
                    mono = true
                )
                DetailRow(
                    label = "基础费用（前4小时）",
                    value = formatFee(breakdown.baseFee),
                    mono = true
                )
                DetailRow(
                    label = "超时 ${breakdown.extraHours}小时 × 2元",
                    value = formatFee(breakdown.extraFee),
                    mono = true
                )
                if (breakdown.isCapped) {
                    DetailRow(
                        label = "封顶价（24小时）",
                        value = formatFee(breakdown.totalFee),
                        mono = true
                    )
                }
            } else {
                DetailRow(
                    label = "计费时长",
                    value = "${breakdown.displayHours}小时",
                    mono = true
                )
                DetailRow(
                    label = "4小时内一口价",
                    value = formatFee(breakdown.baseFee),
                    mono = true
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, mono: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FeeRulesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "收费标准",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            val rules = listOf(
                "1小时内免费",
                "4小时内收费5元",
                "超过4小时后，每增加1小时加收2元",
                "不足1小时按1小时计算",
                "24小时内封顶20元"
            )
            rules.forEachIndexed { index, rule ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = rule,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDuration(breakdown: FeeBreakdown): String {
    val hours = breakdown.totalHours.toInt()
    val minutes = ((breakdown.totalHours - hours) * 60).toInt()
    return when {
        hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
        hours > 0 -> "${hours}小时"
        else -> "${minutes}分钟"
    }
}

private fun formatFee(fee: Double): String {
    return if (fee == fee.toLong().toDouble()) {
        "${fee.toInt()}元"
    } else {
        String.format("%.1f元", fee)
    }
}

private fun formatFeeLarge(fee: Double): String {
    return if (fee == fee.toLong().toDouble()) {
        "${fee.toInt()}元"
    } else {
        String.format("%.1f元", fee)
    }
}
