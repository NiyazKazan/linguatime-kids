package com.linguatime.kids.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linguatime.kids.data.ChildRepository
import com.linguatime.kids.data.RewardPolicy
import com.linguatime.kids.data.RewardRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildRequestTimeScreen(
    childId: String,
    childName: String,
    childRepository: ChildRepository,
    rewardRepository: RewardRepository,
    onRequested: () -> Unit,
    onBack: () -> Unit
) {
    var pointsBalance by remember { mutableStateOf(0L) }
    var policy by remember { mutableStateOf<RewardPolicy?>(null) }
    var loading by remember { mutableStateOf(true) }
    
    var selectedCategory by remember { mutableStateOf("Игры") }
    var minutesInput by remember { mutableStateOf("") }
    var isRequesting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val categories = listOf("Игры", "YouTube", "Мессенджеры", "Общее")
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(childId) {
        pointsBalance = childRepository.getChildPoints(childId)
        policy = rewardRepository.getOrCreatePolicy(childId)
        loading = false
    }

    val pointsPerMin = policy?.pointsPerMinute ?: 10
    val minutes = minutesInput.toIntOrNull() ?: 0
    val cost = minutes * pointsPerMin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Запросить время", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Твои баллы: $pointsBalance", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = minutesInput,
                onValueChange = { minutesInput = it.filter { c -> c.isDigit() } },
                label = { Text("Сколько минут?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Стоимость: $cost баллов",
                style = MaterialTheme.typography.bodyMedium
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        minutes <= 0 -> error = "Введи корре количество минут."
                        cost > pointsBalance -> error = "Недостаточно баллов!"
                        else -> {
                            isRequesting = true
                            error = null
                            scope.launch {
                                try {
                                    rewardRepository.createTimeRequest(
                                        childId = childId,
                                        childName = childName,
                                        category = selectedCategory,
                                        minutes = minutes,
                                        pointsCost = cost
                                    )
                                    onRequested()
                                } catch (e: Exception) {
                                    error = "Ошибка сети. Попробуй ещё раз."
                                    isRequesting = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRequesting && minutes > 0
            ) {
                Text(if (isRequesting) "Отправляем..." else "Отправить запрос родителю")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack) {
            Text("Назад")
        }
    }
}
