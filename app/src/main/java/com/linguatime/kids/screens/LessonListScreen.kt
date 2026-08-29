package com.linguatime.kids.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import com.linguatime.kids.data.Lesson
import com.linguatime.kids.data.LessonRepository
import kotlinx.coroutines.launch

@Composable
fun LessonsListScreen(
    childId: String,
    childLevel: String,
    lessonRepository: LessonRepository,
    onLessonClick: (Lesson) -> Unit,
    onBack: () -> Unit
) {
    var lessons by remember { mutableStateOf<List<Lesson>>(emptyList()) }
    var completedLessons by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var generating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(childId) {
        lessons = lessonRepository.getLessons()
        completedLessons = lessonRepository.getCompletedLessons(childId)
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Уроки", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка генерации нового урока через ИИ
        Button(
            onClick = {
                generating = true
                error = null
                scope.launch {
                    try {
                        val newLesson = lessonRepository.generateLessonWithAI(childId, childLevel)
                        lessons = lessonRepository.getLessons() // перезагружаем список
                    } catch (e: Exception) {
                        error = "Не удалось сгенерировать урок: ${e.message}"
                    } finally {
                        generating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !generating && !loading
        ) {
            Text(if (generating) "Генерируем урок..." else "✨ Сгенерировать новый урок с ИИ")
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(lessons) { lesson ->
                    val isCompleted = lesson.id in completedLessons
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLessonClick(lesson) }
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                            Text(lesson.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${lesson.exercises.size} заданий • ${lesson.generatedBy}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                if (isCompleted) "✅ Пройден" else "Начать",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack) {
            Text("Назад")
        }
    }
}
