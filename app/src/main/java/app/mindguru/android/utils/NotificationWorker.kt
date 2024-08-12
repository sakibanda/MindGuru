package app.mindguru.android.utils

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.mindguru.android.BuildConfig
import app.mindguru.android.components.Logger
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val TAG = "NotificationWorker"
const val WORK_TAG = "NotificationWork"
class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Logger.e(TAG, "doWork")
        NotificationUtils.showMoodNotification(applicationContext)
        return Result.success()
    }
}

object ScheduleNotifications {
    fun schedule(context: Context, times: List<String>) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()

        times.forEach { time ->
            val (hour, minute, seconds) = time.split(":").map { it.toInt() }
            val delay = calculateDelay(hour, minute)
            //val delay = calculateInitialDelay(hour, minute, seconds)
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build()
            workManager.enqueue(workRequest)
            Logger.e(TAG, "schedule: $time")
            Logger.e(TAG, "delay: $delay")
        }
    }

    private fun calculateInitialDelay(hour: Int, minute: Int, seconds: Int): Long {
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, seconds)
            set(Calendar.MILLISECOND, 0)
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return targetTime.timeInMillis - currentTime.timeInMillis
    }

    private fun calculateDelay(hour: Int, minute: Int): Long{
        if(BuildConfig.DEBUG) return 5000 //RELEASE REMOVE THIS LINE
        val currentTime = System.currentTimeMillis()
        val targetTime = currentTime + (hour * 60 + minute) * 60 * 1000
        return targetTime - currentTime
    }
}