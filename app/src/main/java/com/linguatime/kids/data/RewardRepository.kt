package com.linguatime.kids.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class RewardPolicy(
    val childId: String,
    val pointsPerMinute: Int,
    val dailyMaxMinutes: Int,
    val allowedCategories: List<String>,
    val autoApprove: Boolean
)

data class TimeRequest(
    val requestId: String,
    val childId: String,
    val childName: String,
    val category: String,
    val minutesRequested: Int,
    val pointsCost: Int,
    val status: String,
    val createdAt: Long,
    val approvedAt: Long?
)

data class TimeGrant(
    val grantId: String,
    val childId: String,
    val category: String,
    val minutesGranted: Int,
    val startedAt: Long?,
    val expiresAt: Long?,
    val status: String
)

class RewardRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getOrCreatePolicy(childId: String): RewardPolicy {
        val doc = firestore.collection("reward_policies").document(childId).get().await()
        return if (doc.exists()) {
            doc.toPolicy()
        } else {
            val defaultPolicy = RewardPolicy(
                childId = childId,
                pointsPerMinute = 10,
                dailyMaxMinutes = 60,
                allowedCategories = listOf("games", "youtube", "messengers", "general"),
                autoApprove = false
            )
            firestore.collection("reward_policies").document(childId).set(
                mapOf(
                    "pointsPerMinute" to defaultPolicy.pointsPerMinute,
                    "dailyMaxMinutes" to defaultPolicy.dailyMaxMinutes,
                    "allowedCategories" to defaultPolicy.allowedCategories,
                    "autoApprove" to defaultPolicy.autoApprove,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
            defaultPolicy
        }
    }

    suspend fun updatePolicy(policy: RewardPolicy) {
        firestore.collection("reward_policies").document(policy.childId).set(
            mapOf(
                "pointsPerMinute" to policy.pointsPerMinute,
                "dailyMaxMinutes" to policy.dailyMaxMinutes,
                "allowedCategories" to policy.allowedCategories,
                "autoApprove" to policy.autoApprove
            )
        ).await()
    }

    suspend fun createTimeRequest(
        childId: String,
        childName: String,
        category: String,
        minutes: Int,
        pointsCost: Int
    ): String {
        val requestId = "req_${childId}_${System.currentTimeMillis()}"
        val data = mapOf(
            "childId" to childId,
            "childName" to childName,
            "category" to category,
            "minutesRequested" to minutes,
            "pointsCost" to pointsCost,
            "status" to "pending",
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("time_requests").document(requestId).set(data).await()
        return requestId
    }

    suspend fun getPendingRequests(parentId: String): List<TimeRequest> {
        val childrenSnap = firestore.collection("children")
            .whereEqualTo("parentId", parentId)
            .get().await()
        val childIds = childrenSnap.documents.map { it.id }
        
        if (childIds.isEmpty()) return emptyList()
        
        val requestsSnap = firestore.collection("time_requests")
            .whereIn("childId", childIds)
            .whereEqualTo("status", "pending")
            .get().await()
        
        return requestsSnap.documents.mapNotNull { it.toTimeRequest() }
    }

    suspend fun approveRequest(requestId: String, childId: String, pointsCost: Int) {
        val requestDoc = firestore.collection("time_requests").document(requestId).get().await()
        val request = requestDoc.toTimeRequest() ?: throw Exception("Запрос не найден")
        
        // Создаём grant
        val grantId = "grant_${childId}_${System.currentTimeMillis()}"
        val grantData = mapOf(
            "childId" to childId,
            "category" to request.category,
            "minutesGranted" to request.minutesRequested,
            "startedAt" to null,
            "expiresAt" to null,
            "status" to "active"
        )
        firestore.collection("time_grants").document(grantId).set(grantData).await()
        
        // Обновляем запрос
        firestore.collection("time_requests").document(requestId).update(
            mapOf("status" to "approved", "approvedAt" to System.currentTimeMillis())
        ).await()
        
        // Списываем баллы
        val childRepo = ChildRepository()
        childRepo.addPointsTransaction(childId, -pointsCost.toLong(), "time_request_$requestId")
    }

    suspend fun rejectRequest(requestId: String) {
        firestore.collection("time_requests").document(requestId).update(
            mapOf("status" to "rejected")
        ).await()
    }

    suspend fun getActiveGrants(childId: String): List<TimeGrant> {
        val snap = firestore.collection("time_grants")
            .whereEqualTo("childId", childId)
            .whereEqualTo("status", "active")
            .get().await()
        return snap.documents.mapNotNull { it.toTimeGrant() }
    }

    private fun DocumentSnapshot.toPolicy(): RewardPolicy {
        return RewardPolicy(
            childId = id,
            pointsPerMinute = getLong("pointsPerMinute")?.toInt() ?: 10,
            dailyMaxMinutes = getLong("dailyMaxMinutes")?.toInt() ?: 60,
            allowedCategories = get("allowedCategories") as? List<String> ?: listOf("games", "youtube", "messengers", "general"),
            autoApprove = getBoolean("autoApprove") ?: false
        )
    }

    private fun DocumentSnapshot.toTimeRequest(): TimeRequest? {
        val childId = getString("childId") ?: return null
        return TimeRequest(
            requestId = id,
            childId = childId,
            childName = getString("childName") ?: "",
            category = getString("category") ?: "general",
            minutesRequested = getLong("minutesRequested")?.toInt() ?: 0,
            pointsCost = getLong("pointsCost")?.toInt() ?: 0,
            status = getString("status") ?: "pending",
            createdAt = getLong("createdAt") ?: 0L,
            approvedAt = getLong("approvedAt")
        )
    }

    private fun DocumentSnapshot.toTimeGrant(): TimeGrant? {
        val childId = getString("childId") ?: return null
        return TimeGrant(
            grantId = id,
            childId = childId,
            category = getString("category") ?: "general",
            minutesGranted = getLong("minutesGranted")?.toInt() ?: 0,
            startedAt = getLong("startedAt"),
            expiresAt = getLong("expiresAt"),
            status = getString("status") ?: "active"
        )
    }
}
