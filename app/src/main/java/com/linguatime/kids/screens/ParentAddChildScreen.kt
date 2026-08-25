package com.linguatime.kids.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linguatime.kids.data.AuthRepository
import com.linguatime.kids.data.ChildRepository
import kotlinx.coroutines.launch

@Composable
fun ParentAddChildScreen(
    repository: AuthRepository,
    childRepository: ChildRepository,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ageGroup by remember { mutableStateOf("8-10") }
    var createdCode by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (createdCode == null) {
            Text("Новый ребёнок", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя ребёнка") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = ageGroup == "8-10", onClick = { ageGroup = "8-10" })
                Text("8–10 лет")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = ageGroup == "11-14", onClick = { ageGroup = "11-14" })
                Text("11–14 лет")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    loading = true
                    error = null
                    scope.launch {
                        try {
                            val parent = repository.currentUser
                                ?: throw Exception("Нет родителя")
                            val child = childRepository.createChild(parent.uid, name.trim(), ageGroup)
                            createdCode = child.linkCode
                        } catch (e: Exception) {
                            error = "Не удалось создать профиль. Попробуй ещё раз."
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && name.isNotBlank()
            ) {
                Text(if (loading) "Создаём..." else "Создать профиль")
            }
            TextButton(onClick = onBack) {
                Text("Назад")
            }
        } else {
            Text("Ребёнок создан!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Код привязки:")
            Text(text = createdCode!!, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Открой приложение на телефоне ребёнка, выбери «Я ребёнок» и введи этот код.")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Готово")
            }
        }
    }
}