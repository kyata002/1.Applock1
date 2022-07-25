package com.mtg.applock.service.worker

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mtg.applock.service.AppLockerService

class ServiceCheckerWorker(val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        ContextCompat.startForegroundService(context, Intent(context, AppLockerService::class.java))
        return Result.success()
    }
}