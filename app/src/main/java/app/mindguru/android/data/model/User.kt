package app.mindguru.android.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "",
    val email: String = "",
    val picture: String = "",
    var gender: String = "",
    var dob: String = "",
    var relationship: String = "",
    var country: String = "",
    var employment: String = "",
    var symptoms: String = "",
    val healthSeverity: String = "",
) {
    companion object {
        var fetched: Boolean = false
        var signup: Boolean = false
        var uid: String? = null
        var currentUser: User? = null
    }
}