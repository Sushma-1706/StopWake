package com.example.stopwake.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun signUpWithEmail(email: String, password: String): FirebaseUser?
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser?
    suspend fun signInAnonymously(): FirebaseUser?
    suspend fun signOut()
}
