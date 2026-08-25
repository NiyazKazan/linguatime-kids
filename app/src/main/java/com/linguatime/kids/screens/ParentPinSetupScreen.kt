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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.linguatime.kids.data.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun ParentPinSetupScreen(
    repository: AuthRepository,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Придумай PIN", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("PIN защищает родительскую зону от ребёнка (4–8 цифр).")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = pin1,
            onValueChange = { pin1 = it.filter { c -> c.isDigit() }.take(8) },
            label = { Text("PIN") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = pin2,
            onValueChange = { pin2 = it.filter { c -> c.isDigit() }.take(8) },
            label = { Text("Повтори PIN") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                when {
                    pin1.length < 4 -> error = "PIN должен быть не короче 4 цифр."
                    pin1 != pin2 -> error = "PIN-коды не совпадают."
                    else -> {
                        loading = true
                        error = null
                        scope.launch {
                            try {
                                val user = repository.currentUser
                                    ?: throw Exception("Нет пользователя")
                                repository.saveParentProfile(user.uid, repository.hashPin(pin1))
                                onDone()
                            } catch (e: Exception) {
                                error = "Не удалось сохранить PIN. Попробуй ещё раз."
                            } finally {
                                loading = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && pin1.isNotBlank() && pin2.isNotBlank()
        ) {
            Text(if (loading) "Сохраняем..." else "Сохранить PIN")
        }
        TextButton(onClick = onBack) {
            Text("Назад")
        }
    }
}