package app.mindguru.android.data.repository

import android.app.Application
import app.mindguru.android.BuildConfig
import app.mindguru.android.components.Logger
import app.mindguru.android.components.Remote
import app.mindguru.android.data.model.MoodLog
import app.mindguru.android.data.model.User
import app.mindguru.android.data.sources.local.AppDao
import app.mindguru.android.utils.ScheduleNotifications
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

const val TAG = "FirebaseRepository"
class FirebaseRepository @Inject constructor(
    private val app: Application,
    private val preferenceRepository: PreferenceRepository
) {
    private val firestore: FirebaseFirestore = Firebase.firestore

    suspend fun getMoodLogs(): List<MoodLog> {
        val uid = Firebase.auth.currentUser?.uid ?: ""
        return try {
            Logger.e(TAG, "getMoodLogs")
            val snapshot = firestore.collection("Users").document(uid).collection("MoodLogs").get().await()
            snapshot.toObjects(MoodLog::class.java)
        } catch (e: Exception) {
            Logger.e(TAG, "getMoodLogs: ${e.message}")
            Remote.captureException(e)
            emptyList()
        }
    }

    fun logMood(uid: String, moodLog: MoodLog) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection("Users").document(uid).collection("MoodLogs").document()
            docRef.set(moodLog)
        }catch (e: Exception) {
            Logger.e(TAG, "logMood: ${e.message}")
            Remote.captureException(e)
        }
    }

    suspend fun updateFirstMessagePrompt(newPrompt: String) {
        try {
            Logger.e(TAG, "updateFirstMessagePrompt: $newPrompt")
            val uid = Firebase.auth.currentUser?.uid ?: ""
            val firestore = FirebaseFirestore.getInstance()

            val messagesCollection =
                firestore.collection("Users").document(uid).collection("Messages")
            val firstMessageSnapshot =
                messagesCollection.orderBy("startTime").limit(1).get().await()

            if (!firstMessageSnapshot.isEmpty) {
                Logger.e(
                    TAG,
                    "updateFirstMessagePrompt: firstMessageSnapshot.size = ${firstMessageSnapshot.size()}"
                )
                val firstMessageDocument = firstMessageSnapshot.documents[0]
                Logger.e(
                    TAG,
                    "updateFirstMessagePrompt: firstMessageDocument: ${
                        firstMessageDocument.data?.get("prompt")
                    }"
                )
                firstMessageDocument.reference.update("prompt", newPrompt).await()
            }
        }catch (e: Exception) {
            Logger.e(TAG, "updateFirstMessagePrompt: ${e.message}")
            Remote.captureException(e)
        }
    }

    fun resetChat() {
        if (!isLogged()) {
            return
        }
        val uid = Firebase.auth.currentUser?.uid ?: ""
        firestore.collection("Users").document(uid).collection("Messages").get().addOnSuccessListener { documents ->
            for (document in documents) {
                document.reference.delete()
            }
        }
    }

    suspend fun sendMessage(prompt: String) {
        if (!isLogged()) {
            return
        }
        val uid = Firebase.auth.currentUser?.uid ?: ""
        val message = hashMapOf(
            "prompt" to prompt,
            "status" to "PROCESSING",
            "startTime" to System.currentTimeMillis()
        )
        firestore.collection("Users").document(uid).collection("Messages").add(message).await()
    }

    fun getMessages() = firestore.collection("Users")
        .document(Firebase.auth.currentUser?.uid ?: "")
        .collection("Messages")
        .orderBy("startTime")

    suspend fun loginToFirebase(email: String, password: String): Boolean = suspendCancellableCoroutine { continuation ->
        if(isLogged()) {
            continuation.resume(true)
        }
        Logger.d(TAG, "loginToFirebase email: $email")
        Firebase.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                continuation.resume(true)
            } else {
                Firebase.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        continuation.resume(true)
                    } else {
                        continuation.resume(false)
                    }
                }
            }
        }
    }

    suspend fun loginToFirebase(token: String): Boolean = suspendCancellableCoroutine { continuation ->
        if(isLogged()) {
            continuation.resume(true)
        }
        Logger.d(TAG, "loginToFirebase token: $token")
        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
        Firebase.auth.signInWithCredential(firebaseCredential).addOnCompleteListener{ task ->
            if(task.isSuccessful) {
                continuation.resume(true)
            } else {
                continuation.resume(false)
            }
        }
    }

    suspend fun setUpAccount() {
        if(!isLogged()) {
            return
        }
        try {
            val uid = Firebase.auth.currentUser?.uid ?: ""
            Logger.e(TAG, "setUpAccount uid: $uid")
            val docRef = firestore.collection("Users").document(uid)
            val snapshot = docRef.get().await()

            if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(User::class.java)?.let { User.currentUser = it }
                User.fetched = true
                User.signup = false
            } else {
                val user = User(
                    email = Firebase.auth.currentUser?.email ?: "",
                    name = Firebase.auth.currentUser?.displayName ?: "",
                    picture = Firebase.auth.currentUser?.photoUrl.toString(),
                    gender = "",
                    dob = "",
                    relationship = "",
                    country = "",
                    employment = "",
                    symptoms = "",
                    healthSeverity = preferenceRepository.getHealthSeverity()
                )
                docRef.set(user)
                User.signup = true
                User.uid = uid
                User.currentUser = user
                User.fetched = true
                //appDao.insertUser(user)
            }
            if(BuildConfig.DEBUG) {
                //get current date in hours minutes seconds format add 5 seconds
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.SECOND, 1)
                val newTime = sdf.format(calendar.time)
                Logger.e(TAG, "notification time: $newTime")
                ScheduleNotifications.schedule(app, listOf(newTime))
            } else {
                val times = preferenceRepository.getString("notification_times", "08:00:00,13:00:00,20:00:00").split(",")
                Logger.e(TAG, "notification time: $times")
                ScheduleNotifications.schedule(app, times)
                preferenceRepository.setBoolean("notification_scheduled", true)
            }
        }catch (e: Exception) {
            Logger.e(TAG, "setUpAccount: ${e.message}")
            Remote.captureException(e)
        }
    }

    fun updateUser(user: User) {
        if(!isLogged()) {
            return
        }
        Logger.d(TAG, "updateUser")
        val uid = Firebase.auth.currentUser?.uid ?: ""
        User.currentUser = user
        val docRef = firestore.collection("Users").document(uid)
        docRef.set(user)
    }

    private fun isLogged(): Boolean {
        return Firebase.auth.currentUser != null
    }
}