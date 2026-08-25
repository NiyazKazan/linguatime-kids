package com.linguatime.kids.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linguatime.kids.data.RewardPolicy
import com.linguatime.kids.data.RewardRepository
import kotlinx.coroutines.launch

@Composable
fun ParentScreenTimeSettings(
    childId: String,
    rewardRepository: RewardRepository,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    var policy by remember { mutableStateOf<RewardPolicy?>(null) }
    var pointsPerMinute by remember { mutableStateOf("10") }
    var dailyMaxMinutes by remember { mutableStateOf("60") }
    var autoApprove by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(childId) {
        val p = rewardRepository.getOrCreatePolicy(childId)
        policy = p
        pointsPerMinute = p.pointsPerMinute.toString()
        dailyMaxMinutes = p.dailyMaxMinutes.toString()
        autoApprove = p.autoApprove
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Настройки экранного времени", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Text("Загрузка...")
        } else {
            OutlinedTextField(
                value = pointsPerMinute,
                onValueChange = { pointsPerMinute = it.filter { c -> c.isDigit() } },
                label = { Text("Баллов за 1 минуту") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = dailyMaxMinutes,
                onValueChange = { dailyMaxMinutes = it.filter { c -> c.isDigit() } },
                label = { Text("Максимум минут в день") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = autoApprove, onCheckedChange = { autoApprove = it })
                Text("Автоматически одобрять запросы (в пределах лимита)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    saving = true
                    scope.launch {
                        val updatedPolicy = RewardPolicy(
                            childId = childId,
                            pointsPerMinute = pointsPerMinute.toIntOrNull() ?: 10,
                            dailyMaxMinutes = dailyMaxMinutes.toIntOrNull() ?: 60,
                            allowedCategories = listOf("games", "youtube", "messengers", "general"),
                            autoApprove = autoApprove
                        )
                        rewardRepository.updatePolicy(updatedPolicy)
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving
            ) {
                Text(if (saving) "Сохраняем..." else "Сохранить")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack) {
            Text("Назад")
        }
    }
}
