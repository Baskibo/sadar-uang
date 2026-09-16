package com.example.sadaruang

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PengeluaranRepository(
    private val dao: PengeluaranDao,
    private val firestore: FirebaseFirestore
) {

    suspend fun insert(data: Pengeluaran) {
       
        val docRef = firestore.collection("pengeluaran").document()

        val newData = data.copy(
            firestoreId = docRef.id
        )

        docRef.set(newData).await()

        dao.insert(newData)
    }

    suspend fun delete(data: Pengeluaran) {
        data.firestoreId?.let {
            firestore.collection("pengeluaran").document(it).delete()
        }
        dao.deleteById(data.id)
    }
}
