package com.linguatime.kids.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linguatime.kids.data.Lesson
import com.linguatime.kids.data.LessonRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LessonScreen(
    childId: String,
    lesson: Lesson,
    lessonRepository: LessonRepository,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var currentExerciseIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var totalScore by remember { mutableStateOf(0) }
    var totalPoints by remember { mutableStateOf(0) }

    val exercises = lesson.exercises
    val currentExercise = exercises.getOrNull(currentExerciseIndex)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(lesson.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Задание ${currentExerciseIndex + 1} из ${exercises.size}")
        Spacer(modifier = Modifier.height(24.dp))

        if (currentExercise != null) {
            Text(currentExercise.question, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            currentExercise.options.forEach { option ->
                Button(
                    onClick = {
                        if (selectedAnswer == null) {
                            selectedAnswer = option
                            isCorrect = option == currentExercise.correctAnswer
                            if (isCorrect == true) {
                                totalScore += 1
                                totalPoints += currentExercise.points
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswer == null
                ) {
                    Text(option)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isCorrect != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (isCorrect == true) "✅ Правильно!" else "❌ Неправильно. Правильный ответ: ${currentExercise.correctAnswer}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (currentExerciseIndex < exercises.size - 1) {
                            currentExerciseIndex += 1
                            selectedAnswer = null
                            isCorrect = null
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                lessonRepository.completeLesson(childId, lesson.id, totalScore, totalPoints)
                                onComplete()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (currentExerciseIndex < exercises.size - 1) "Следующее задание" else "Завершить урок")
                }
            }
        }
    }
}
