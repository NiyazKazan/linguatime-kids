package com.linguatime.kids.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ChildProfile(
    val id: String,
    val name: String,
    val ageGroup: String,
    val linkCode: String,
    val deviceLinked: Boolean,
    val pointsBalance: Long
)

class ChildRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun createChild(parentId: String, name: String, ageGroup: String): ChildProfile {
        val code = (100000..999999).random().toString()
        val ref = firestore.collection("children").document()
        val data = mapOf(
            "parentId" to parentId,
            "name" to name,
            "ageGroup" to ageGroup,
            "linkCode" to code,
            "deviceLinked" to false,
            "pointsBalance" to 0L,
            "currentLevel" to "PRE_A1",
            "createdAt" to System.currentTimeMillis()
        )
        ref.set(data).await()
        return ChildProfile(ref.id, name, ageGroup, code, false, 0L)
    }

    suspend fun getChildren(parentId: String): List<ChildProfile> {
        val snap = firestore.collection("children")
            .whereEqualTo("parentId", parentId)
            .get().await()
        return snap.documents.mapNotNull { it.toChild() }
    }

    suspend fun findByCode(code: String): ChildProfile? {
        val snap = firestore.collection("children")
            .whereEqualTo("linkCode", code)
            .limit(1)
            .get().await()
        return snap.documents.firstOrNull()?.toChild()
    }

    suspend fun getChild(id: String): ChildProfile? {
        val doc = firestore.collection("children").document(id).get().await()
        return if (doc.exists()) doc.toChild() else null
    }

    suspend fun linkDevice(childId: String, deviceId: String) {
        firestore.collection("children").document(childId)
            .update(mapOf("deviceLinked" to true, "deviceId" to deviceId))
            .await()
    }

    private fun DocumentSnapshot.toChild(): ChildProfile? {
        val name = getString("name") ?: return null
        return ChildProfile(
            id = id,
            name = name,
            ageGroup = getString("ageGroup") ?: "8-10",
            linkCode = getString("linkCode") ?: "",
            deviceLinked = getBoolean("deviceLinked") ?: false,
            pointsBalance = getLong("pointsBalance") ?: 0L
        )
    }
    suspend fun getChildPoints(childId: String): Long {
        val doc = firestore.collection("children").document(childId).get().await()
        return doc.getLong("pointsBalance") ?: 0L
    }

    suspend fun addPointsTransaction(childId: String, amount: Long, reason: String) {
        val transactionId = "${childId}_${System.currentTimeMillis()}"
        val currentPoints = getChildPoints(childId)
        val newBalance = currentPoints + amount
        
        val data = mapOf(
            "childId" to childId,
            "amount" to amount,
            "reason" to reason,
            "createdAt" to System.currentTimeMillis(),
            "balanceAfter" to newBalance
        )
        firestore.collection("points_transactions").document(transactionId).set(data).await()
        firestore.collection("children").document(childId).update("pointsBalance", newBalance).await()
    }
}
