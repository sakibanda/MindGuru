// NotificationUtils.kt
package app.mindguru.android.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.mindguru.android.R
import app.mindguru.android.components.Remote
import app.mindguru.android.data.model.Mood
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Random

const val INTENT_COMMAND = "COMMAND"
const val NOTIFICATION_CHANNEL_MOOD_TRACKER = "Mood Tracker"
object NotificationUtils {
    private const val CHANNEL_ID = "mood_tracker_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mood Tracker"
            val descriptionText = "Mood Tracker Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMoodNotification(context: Context) {
        val notificationId: Int = Random().nextInt()
        val uid = Firebase.auth.currentUser?.uid ?: ""
        val intentData = Uri.parse("mindguru://mood-tracker?uid=$uid&notificationId=$notificationId")
        val happyIntent = Intent(context, MoodLogReceiver::class.java).apply {
            action = Mood.HAPPY.name
            data = intentData
        }
        val happyPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, happyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE  )

        val straightIntent = Intent(context, MoodLogReceiver::class.java).apply {
            action = Mood.NEUTRAL.toString()
            data = intentData
        }
        val straightPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 1, straightIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE )

        val sadIntent = Intent(context, MoodLogReceiver::class.java).apply {
            action = Mood.SAD.toString()
            data = intentData
        }
        val sadPendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 2, sadIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_IMMUTABLE )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_MOOD_TRACKER,
                        "Mood Tracker",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                ) {
                    enableLights(false)
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    manager.createNotificationChannel(this)
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                Remote.captureException(e)
                Log.d("Error", "showNotification: ${e.localizedMessage}")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setChannelId(NOTIFICATION_CHANNEL_MOOD_TRACKER)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle("How are you feeling right now?")
            .setContentText("Select your mood")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_happy, Mood.HAPPY.name, happyPendingIntent)
            .addAction(R.drawable.ic_straight, Mood.NEUTRAL.toString(), straightPendingIntent)
            .addAction(R.drawable.ic_sad, Mood.SAD.toString(), sadPendingIntent)
            .setAutoCancel(true) // Ensure the notification is dismissed when clicked
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Ensure visibility is set

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(notificationId, builder.build())
        }
    }
}