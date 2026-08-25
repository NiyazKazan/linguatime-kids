package com.linguatime.kids.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.linguatime.kids.data.AuthRepository
import com.linguatime.kids.data.RewardRepository
import com.linguatime.kids.data.TimeRequest
import kotlinx.coroutines.launch

@Composable
fun ParentTimeRequestsScreen(
    authRepository: AuthRepository,
    rewardRepository: RewardRepository,
    onBack: () -> Unit
) {
    var requests by remember { mutableStateOf<List<TimeRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        requests = rewardRepository.getPendingRequests(authRepository.currentUser?.uid ?: "")
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Запросы на экранное время", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (requests.isEmpty()) {
            Text("Нет активных запросов")
        } else {
            LazyColumn {
                items(requests) { request ->
                    Card(modifier = Modifier.padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(request.childName, style = MaterialTheme.typography.titleMedium)
                            Text("Категория: ${request.category}")
                            Text("${request.minutesRequested} минут")
                            Text("Стоимость: ${request.pointsCost} баллов")
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            rewardRepository.approveRequest(request.requestId, request.childId, request.pointsCost)
                                            requests = rewardRepository.getPendingRequests(authRepository.currentUser?.uid ?: "")
                                        }
                                    }
                                ) {
                                    Text("Одобрить")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            rewardRepository.rejectRequest(request.requestId)
                                            requests = rewardRepository.getPendingRequests(authRepository.currentUser?.uid ?: "")
                                        }
                                    }
                                ) {
                                    Text("Отклонить")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack) {
            Text("Назад")
        }
    }
}
