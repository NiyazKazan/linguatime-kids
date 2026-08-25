package com.linguatime.kids.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linguatime.kids.data.AuthRepository
import com.linguatime.kids.data.ChildProfile
import com.linguatime.kids.data.ChildRepository

@Composable
fun ParentHomeScreen(
    repository: AuthRepository,
    childRepository: ChildRepository,
    onAddChild: () -> Unit,
    onScreenTimeSettings: (String) -> Unit,
    onTimeRequests: () -> Unit,
    onLoggedOut: () -> Unit
) {
    var children by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        children = childRepository.getChildren(repository.currentUser?.uid ?: "")
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Родительская зона", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(repository.currentUser?.email ?: "")
        Spacer(modifier = Modifier.height(24.dp))
        Text("Дети:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (loading) {
            CircularProgressIndicator()
        } else if (children.isEmpty()) {
            Text("Пока нет детей. Добавь первого ребёнка.")
        } else {
            children.forEach { child ->
                Column {
                    Text(child.name, style = MaterialTheme.typography.titleMedium)
                    Text("Код: ${child.linkCode}")
                    Text(if (child.deviceLinked) "Устройство привязано" else "Устройство не привязано")
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onScreenTimeSettings(child.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Настройки экранного времени")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onTimeRequests, modifier = Modifier.fillMaxWidth()) {
            Text("Запросы на время")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onAddChild, modifier = Modifier.fillMaxWidth()) {
            Text("Добавить ребёнка")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                repository.signOut()
                onLoggedOut()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти")
        }
    }
}
