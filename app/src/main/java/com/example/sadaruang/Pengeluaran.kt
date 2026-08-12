package com.example.sadaruang

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pengeluaran")
data class Pengeluaran(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val keterangan: String,
    val kategori: String,
    val nominal: Long,
    val tanggal: String,
    val bulan: Int,
    val tahun: Int,
    val bukti: String? = null,
    val userEmail: String,
    val firestoreId: String? = null
)