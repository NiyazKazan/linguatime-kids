package com.linguatime.kids.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signUp(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Не удалось создать аккаунт")
    }

    suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Не удалось войти")
    }

    suspend fun saveParentProfile(uid: String, pinHash: String) {
        firestore.collection("parents").document(uid).set(
            mapOf(
                "pinHash" to pinHash,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun getParentPinHash(uid: String): String? {
        val doc = firestore.collection("parents").document(uid).get().await()
        return doc.getString("pinHash")
    }

    fun signOut() {
        auth.signOut()
    }

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}