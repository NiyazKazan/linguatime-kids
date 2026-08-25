package com.linguatime.kids.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linguatime.kids.data.AuthRepository

@Composable
fun ParentHomeScreen(
    repository: AuthRepository,
    onLoggedOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Родительская зона", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(repository.currentUser?.email ?: "email неизвестен")
        Spacer(modifier = Modifier.height(24.dp))
        Text("Здесь скоро появятся: прогресс ребёнка, баллы, экранное время и запросы.")
        Spacer(modifier = Modifier.height(24.dp))
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