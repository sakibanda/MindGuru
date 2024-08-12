package app.mindguru.android.data.model

import androidx.room.Entity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

@Entity(tableName = "mood_logs")
data class MoodLog(
    val date: Date = Date(),
    val mood: Mood = Mood.NEUTRAL,
    val notes: String = ""
)

object MoodLogAPI {
    private val db = FirebaseFirestore.getInstance()

    fun logMood(uid: String, moodLog: MoodLog) {
        val docRef = db.collection("Users").document(uid).collection("MoodLogs").document()
        docRef.set(moodLog)
    }
}