package com.example.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSyncHelper {
    private const val TAG = "FirebaseSyncHelper"
    
    var isFirebaseAvailable: Boolean = false
        private set

    fun initialize(context: Context) {
        try {
            // Attempt to initialize Firebase. If google-services.json is missing, this will throw.
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                isFirebaseAvailable = true
                Log.d(TAG, "Firebase is already initialized.")
            } else {
                val app = FirebaseApp.initializeApp(context)
                if (app != null) {
                    isFirebaseAvailable = true
                    Log.d(TAG, "Firebase initialized successfully.")
                } else {
                    isFirebaseAvailable = false
                    Log.w(TAG, "FirebaseApp.initializeApp returned null.")
                }
            }
        } catch (e: Exception) {
            isFirebaseAvailable = false
            Log.e(TAG, "Firebase initialization failed: ${e.message}. Operating in offline/Room cache mode.", e)
        }
    }

    fun getAuth(): FirebaseAuth? {
        return if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun getFirestore(): FirebaseFirestore? {
        return if (isFirebaseAvailable) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                null
            }
        } else null
    }
}
