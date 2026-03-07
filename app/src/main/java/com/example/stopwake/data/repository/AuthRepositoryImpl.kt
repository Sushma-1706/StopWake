package com.example.stopwake.data.repository

import com.example.stopwake.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun signUpWithEmail(email: String, password: String): FirebaseUser? {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await().user
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun signInAnonymously(): FirebaseUser? {
        return try {
            firebaseAuth.signInAnonymously().await().user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}

