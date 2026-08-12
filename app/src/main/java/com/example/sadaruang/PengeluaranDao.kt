    package com.example.sadaruang

    import androidx.lifecycle.LiveData
    import androidx.room.Dao
    import androidx.room.Delete
    import androidx.room.Insert
    import androidx.room.Query
    import androidx.room.Update

    @Dao
    interface PengeluaranDao {
        // Fungsi untuk menyimpan data baru
        @Insert
        suspend fun insert(pengeluaran: Pengeluaran)

        // Fungsi untuk mengambil data berdasarkan bulan dan tahun saat ini
        @Query(""" SELECT * FROM pengeluaran WHERE bulan = :bulan AND tahun = :tahun AND userEmail = :email ORDER BY id DESC""")
        fun getByMonthYear(bulan: Int, tahun: Int, email: String): LiveData<List<Pengeluaran>>


        // Fungsi untuk menghitung total nominal pengeluaran dalam satu bulan
        @Query("SELECT SUM(nominal) FROM pengeluaran WHERE bulan = :m AND tahun = :y")
        fun getTotalBulan(m: Int, y: Int): LiveData<Long?>

        // Fungsi untuk menghitung total nominal pengeluaran dalam satu tahun
        @Query("SELECT SUM(nominal) FROM pengeluaran WHERE tahun = :y")
        fun getTotalTahun(y: Int): LiveData<Long?>

        // ---> TARUH DI SINI <---
        // Fungsi untuk mengambil semua list pengeluaran berdasarkan tahun

        @Query("SELECT * FROM pengeluaran WHERE tahun = :tahun AND userEmail = :email")
        fun getByYear(tahun: Int, email: String): LiveData<List<Pengeluaran>>

        @Query("DELETE FROM pengeluaran WHERE id = :id")
        suspend fun deleteById(id: Int)

        @Query("SELECT * FROM pengeluaran WHERE userEmail = :email")
        suspend fun getAllOnce(email: String): List<Pengeluaran>

        @Query("SELECT * FROM pengeluaran WHERE firestoreId = :id LIMIT 1")
        suspend fun getByFirestoreId(id: String): Pengeluaran?

        @Update
        suspend fun update(data: Pengeluaran)

        @Query("""SELECT * FROM pengeluaran WHERE keterangan = :ket AND nominal = :nominal AND tanggal = :tanggal AND userEmail = :email LIMIT 1 """)
        fun findDuplicate(
            ket: String,
            nominal: Long,
            tanggal: String,
            email: String
        ): Pengeluaran?
    }