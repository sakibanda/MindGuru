package app.mindguru.android.data.repository
import app.mindguru.android.data.model.User
import app.mindguru.android.data.sources.local.AppDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val appDao: AppDao) {

    suspend fun insert(user: User) {
        withContext(Dispatchers.IO) {
            appDao.insertUser(user)
        }
    }

    suspend fun update(user: User) {
        withContext(Dispatchers.IO) {
            appDao.updateUser(user)
        }
    }

    suspend fun delete(user: User) {
        withContext(Dispatchers.IO) {
            appDao.deleteUser(user)
        }
    }

    suspend fun getUserById(id: Int): User? {
        return withContext(Dispatchers.IO) {
            appDao.getUserById(id)
        }
    }

    suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            appDao.getAllUsers()
        }
    }
}