package com.linguatime.kids.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import com.linguatime.kids.BuildConfig


data class Exercise(
    val type: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val points: Int,
    val explanation: String = ""
)

data class Lesson(
    val id: String,
    val title: String,
    val level: String,
    val description: String,
    val exercises: List<Exercise>,
    val generatedBy: String = "manual"
)

class LessonRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()

    suspend fun getLessons(): List<Lesson> {
        val snap = firestore.collection("lessons").get().await()
        return snap.documents.mapNotNull { it.toLesson() }
    }

    suspend fun getLesson(id: String): Lesson? {
        val doc = firestore.collection("lessons").document(id).get().await()
        return if (doc.exists()) doc.toLesson() else null
    }

    suspend fun getCompletedLessons(childId: String): List<String> {
        val snap = firestore.collection("child_lessons")
            .whereEqualTo("childId", childId)
            .get().await()
        return snap.documents.map { it.getString("lessonId") ?: "" }
    }

    suspend fun completeLesson(childId: String, lessonId: String, score: Int, pointsEarned: Int) {
        val progressId = "${childId}_$lessonId"
        val data = mapOf(
            "childId" to childId,
            "lessonId" to lessonId,
            "completedAt" to System.currentTimeMillis(),
            "score" to score,
            "pointsEarned" to pointsEarned
        )
        firestore.collection("child_lessons").document(progressId).set(data).await()
        
        val childRepo = ChildRepository()
        childRepo.addPointsTransaction(childId, pointsEarned.toLong(), "lesson_completed_$lessonId")
    }

// НОВЫЙ МЕТОД: прямой вызов Hugging Face API из Android
    suspend fun generateLessonWithAI(childId: String, level: String): Lesson {
        // Токен Hugging Face (ВРЕМЕННО в коде!)
        val hfToken = com.linguatime.kids.BuildConfig.HF_TOKEN
        
        val systemPrompt = """Ты — дружелюбный учитель английского для детей 8-14 лет.
        Сгенерируй 3 задания multiple_choice для уровня $level.
        Верни ТОЛЬКО JSON:
        {
          "exercises": [
            {
              "type": "multiple_choice",
              "question": "Вопрос",
              "options": ["a", "b", "c", "d"],
              "correctAnswer": "правильный ответ",
              "points": 5,
              "explanation": "Объяснение на русском"
            }
          ]
        }""".trimIndent()

        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val request = okhttp3.Request.Builder()
            .url("https://api-inference.huggingface.co/models/Qwen/Qwen3.8-2.4T-A95B/v1/chat/completions")
            .addHeader("Authorization", "Bearer $hfToken")
            .addHeader("Content-Type", "application/json")
            .post(
                okhttp3.MediaType.parse("application/json; charset=utf-8")!!.parseString(
                    """{
                      "model": "Qwen/Qwen3.8-2.4T-A95B",
                      "messages": [
                        {"role": "system", "content": "$systemPrompt"},
                        {"role": "user", "content": "Сгенерируй урок для уровня $level"}
                      ],
                      "temperature": 0.7,
                      "max_tokens": 1000
                    }"""
                )
            )
            .build()

        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("Hugging Face API error: ${response.code} ${response.body?.string()}")
        }

        val responseBody = response.body?.string()
            ?: throw Exception("Empty response from API")

        // Парсим JSON ответ
        val json = org.json.JSONObject(responseBody)
        val content = json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        // Извлекаем JSON из ответа
        val jsonMatch = Regex("""\{[\s\S]*\}""").find(content)
        val jsonStr = jsonMatch?.value ?: content
        
        val lessonData = org.json.JSONObject(jsonStr)
        val exercisesArray = lessonData.getJSONArray("exercises")
        
        val exercises = (0 until exercisesArray.length()).map { i ->
            val ex = exercisesArray.getJSONObject(i)
            Exercise(
                type = ex.optString("type", "multiple_choice"),
                question = ex.getString("question"),
                options = (0 until ex.getJSONArray("options").length()).map { j ->
                    ex.getJSONArray("options").getString(j)
                },
                correctAnswer = ex.getString("correctAnswer"),
                points = ex.optInt("points", 5),
                explanation = ex.optString("explanation", "")
            )
        }

        // Сохраняем урок в Firestore
        val lessonRef = firestore.collection("lessons").document()
        lessonRef.set(
            mapOf(
                "title" to "Урок уровня $level",
                "level" to level,
                "description" to "Сгенерировано ИИ (Qwen 3.8)",
                "exercises" to exercises.map { ex ->
                    mapOf(
                        "type" to ex.type,
                        "question" to ex.question,
                        "options" to ex.options,
                        "correctAnswer" to ex.correctAnswer,
                        "points" to ex.points,
                        "explanation" to ex.explanation
                    )
                },
                "generatedBy" to "Qwen3.8",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "childId" to childId
            )
        ).await()

        return Lesson(
            id = lessonRef.id,
            title = "Урок уровня $level",
            level = level,
            description = "Сгенерировано ИИ",
            exercises = exercises,
            generatedBy = "Qwen3.8"
        )
    }

    private fun DocumentSnapshot.toLesson(): Lesson? {
        val title = getString("title") ?: return null
        val exercisesList = get("exercises") as? List<*> ?: emptyList<Any>()
        val exercises = exercisesList.mapNotNull { exercise ->
            val map = exercise as? Map<*, *> ?: return@mapNotNull null
            Exercise(
                type = map["type"] as? String ?: "multiple_choice",
                question = map["question"] as? String ?: "",
                options = (map["options"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                correctAnswer = map["correctAnswer"] as? String ?: "",
                points = (map["points"] as? Long)?.toInt() ?: 5,
                explanation = map["explanation"] as? String ?: ""
            )
        }
        return Lesson(
            id = id,
            title = title,
            level = getString("level") ?: "A1",
            description = getString("description") ?: "",
            exercises = exercises,
            generatedBy = getString("generatedBy") ?: "manual"
        )
    }
}
