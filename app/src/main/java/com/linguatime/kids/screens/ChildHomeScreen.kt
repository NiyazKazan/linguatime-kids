package com.linguatime.kids.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.linguatime.kids.data.ChildRepository
import com.linguatime.kids.data.DeviceStorage
import kotlinx.coroutines.launch

@Composable
fun ChildLoginScreen(
    childRepository: ChildRepository,
    deviceStorage: DeviceStorage,
    onLoggedIn: () -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Вход для ребёнка", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Попроси у родителя код привязки и введи его здесь.")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
            label = { Text("Код из 6 цифр") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
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
                        val child = childRepository.findByCode(code)
                        if (child == null) {
                            error = "Код не найден. Проверь и попробуй ещё раз."
                        } else {
                            childRepository.linkDevice(child.id, deviceStorage.deviceId())
                            deviceStorage.saveChildId(child.id)
                            onLoggedIn()
                        }
                    } catch (e: Exception) {
                        error = "Ошибка сети. Попробуй ещё раз."
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && code.length == 6
        ) {
            Text(if (loading) "Проверяем..." else "Войти")
        }
        TextButton(onClick = onBack) {
            Text("Назад")
        }
    }
}