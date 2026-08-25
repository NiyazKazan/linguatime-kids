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
import com.linguatime.kids.data.ChildProfile
import com.linguatime.kids.data.ChildRepository

@Composable
fun ChildHomeScreen(
    childId: String,
    childRepository: ChildRepository,
    onLoggedOut: () -> Unit
) {
    var child by remember { mutableStateOf<ChildProfile?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(childId) {
        child = childRepository.getChild(childId)
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else {
            val current = child
            if (current == null) {
                Text("Профиль не найден. Попроси родителя создать его заново.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onLoggedOut, modifier = Modifier.fillMaxWidth()) {
                    Text("Назад")
                }
            } else {
                Text("Привет, ${current.name}!", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Твои баллы: ${current.pointsBalance}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Уроки английского скоро появятся здесь.")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onLoggedOut, modifier = Modifier.fillMaxWidth()) {
                    Text("Выйти")
                }
            }
        }
    }
}