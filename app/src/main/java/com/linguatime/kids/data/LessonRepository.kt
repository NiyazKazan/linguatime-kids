package com.linguatime.kids.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

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

    // НОВЫЙ МЕТОД: генерация урока через ИИ
    suspend fun generateLessonWithAI(childId: String, level: String): Lesson {
        val data = hashMapOf(
            "childId" to childId,
            "level" to level
        )
        
        val result = functions
            .getHttpsCallable("generateLesson")
            .call(data)
            .await()
        
        val resultData = result.data as? Map<*, *>
            ?: throw Exception("Invalid response from server")
        
        val lessonId = resultData["lessonId"] as? String
            ?: throw Exception("No lessonId in response")
        
        val exercisesList = resultData["exercises"] as? List<*>
            ?: throw Exception("No exercises in response")
        
        val exercises = exercisesList.mapNotNull { ex ->
            val map = ex as? Map<*, *> ?: return@mapNotNull null
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
            id = lessonId,
            title = "Урок уровня $level",
            level = level,
            description = "Сгенерировано ИИ",
            exercises = exercises,
            generatedBy = "Qwen"
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
