package app.mindguru.android.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import app.mindguru.android.components.Logger
import app.mindguru.android.data.model.MoodLog
import app.mindguru.android.data.model.Mood
import app.mindguru.android.data.repository.FirebaseRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class MoodLogReceiver : BroadcastReceiver() {
    val TAG = "MoodLogReceiver"
    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    override fun onReceive(context: Context, intent: Intent) {
        // data = Uri.parse("uid:$uid")
        val notificationId = intent.data?.getQueryParameter("notificationId").toString()
        NotificationManagerCompat.from(context).cancel(notificationId.toInt())
        val uid = intent.data?.getQueryParameter("uid").toString()
        when (intent.action) {
            Mood.HAPPY.name -> {
                Logger.e(TAG, "onReceive: HAPPY $uid")
                firebaseRepository.logMood(uid, MoodLog(Date(), Mood.HAPPY))
            }
            Mood.NEUTRAL.toString() -> {
                Logger.e(TAG, "onReceive: STRAIGHT  $uid")
                firebaseRepository.logMood(uid, MoodLog(Date(), Mood.NEUTRAL))
            }
            Mood.SAD.toString() -> {
                Logger.e(TAG, "onReceive: SAD  $uid")
                firebaseRepository.logMood(uid, MoodLog(Date(), Mood.SAD))
                //launch app
                /*val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                Logger.e(TAG, "onReceive: launchIntent $launchIntent")
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }*/
            }
        }
    }
}