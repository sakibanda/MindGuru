package app.mindguru.android.data.repository

import app.mindguru.android.components.Logger
import app.mindguru.android.components.Remote
import app.mindguru.android.data.model.User
import app.mindguru.android.data.sources.local.AppDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume

const val TAG = "FirebaseRepository"
class FirebaseRepository @Inject constructor(
    private val appDao: AppDao,
    private val preferenceRepository: PreferenceRepository
) {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    suspend fun sendMessage(prompt: String) {
        if (!isLogged()) {
            return
        }
        val uid = auth.uid ?: return
        val message = hashMapOf(
            "prompt" to prompt,
            "status" to "PROCESSING",
            "startTime" to System.currentTimeMillis()
        )
        firestore.collection("Users").document(uid).collection("Messages").add(message).await()
    }

    fun getMessages() = firestore.collection("Users")
        .document(auth.uid ?: "")
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
            val uid = auth.uid ?: return
            Logger.d(TAG, "setUpAccount uid: $uid")
            val docRef = firestore.collection("Users").document(uid)
            val snapshot = docRef.get().await()

            if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(User::class.java)?.let { User.currentUser = it }
                User.fetched = true
                return
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
        User.currentUser = user
        val uid = auth.uid ?: return
        val docRef = firestore.collection("Users").document(uid)
        docRef.set(user)
    }

    private fun isLogged(): Boolean {
        return Firebase.auth.currentUser != null
    }
}