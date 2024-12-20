package com.example.act22.viewmodel

import com.google.firebase.firestore.FirebaseFirestore

//class that holds instance of firestore db
//performs basic queries that should be used in other classes
class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun addDocument(
        collection: String,
        data: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.collection(collection).add(data)
            .addOnSuccessListener { documentReference ->
                onComplete(true, documentReference.id)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun getDocuments(
        collection: String,
        onComplete: (Boolean, List<Map<String, Any>>?) -> Unit
    ) {
        db.collection(collection).get()
            .addOnSuccessListener { querySnapshot ->
                val documents = querySnapshot.documents.map { it.data ?: emptyMap() }
                onComplete(true, documents)
            }
            .addOnFailureListener { exception ->
                onComplete(false, null)
            }
    }

    fun updateDocument(
        collection: String,
        documentId: String,
        updates: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection(collection).document(documentId).update(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteDocument(
        collection: String,
        documentId: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection(collection).document(documentId).delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
