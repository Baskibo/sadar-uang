package com.example.sadaruang

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PengeluaranRepository(
    private val dao: PengeluaranDao,
    private val firestore: FirebaseFirestore
) {

    suspend fun insert(data: Pengeluaran) {

        // 1. simpan ke firestore dulu
        val docRef = firestore.collection("pengeluaran").document()

        val newData = data.copy(
            firestoreId = docRef.id
        )

        docRef.set(newData).await()

        // 2. simpan ke room (SUDAH ADA firestoreId)
        dao.insert(newData)
    }

    suspend fun delete(data: Pengeluaran) {
        data.firestoreId?.let {
            firestore.collection("pengeluaran").document(it).delete()
        }
        dao.deleteById(data.id)
    }
}