package com.example.sadaruang

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class WeeklyWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {

        val prefs = applicationContext.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val aktif = prefs.getBoolean("notif", false)

        if (aktif) {
            NotificationHelper(applicationContext).showNotification(
                "Reminder Keuangan",
                "Jangan lupa cek pengeluaran minggu ini ya!"
            )
        }

        return Result.success()
    }
}