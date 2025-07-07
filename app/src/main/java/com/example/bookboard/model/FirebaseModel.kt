package com.example.bookboard.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FirebaseModel {

    private val database = Firebase.firestore

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }

        database.firestoreSettings = setting

        val auth = Firebase.auth


}