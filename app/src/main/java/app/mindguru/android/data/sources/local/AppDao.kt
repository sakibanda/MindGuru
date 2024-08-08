package app.mindguru.android.data.sources.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import app.mindguru.android.data.model.User

@Dao
interface AppDao {
    @Insert
    fun insertUser(user: User)

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(user: User)

    @Query("SELECT * FROM user WHERE id = :id")
    fun getUserById(id: Int): User?

    @Query("SELECT * FROM user")
    fun getAllUsers(): List<User>
}