package app.mindguru.android.ui.navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.mindguru.android.data.model.User
import app.mindguru.android.ui.MoodTrackerScreen
import app.mindguru.android.ui.User.ShowLoginScreen
import app.mindguru.android.ui.User.ShowMentalHealthScreen
import app.mindguru.android.ui.User.ShowUserDetailsScreen
import app.mindguru.android.ui.Userimport.ShowSymptomsScreen
import app.mindguru.android.ui.chat.ShowChatScreen
import app.mindguru.android.ui.settings.SettingsScreen

@Composable
fun Navigation(navController: NavHostController, startDestination: String, isLoggedIn: Boolean) {
    NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.fillMaxSize()) {

        composable("MoodTrackerScreen") {
            MoodTrackerScreen(navigateBack = { navController.popBackStack() })
        }
        composable("SettingsScreen") {
            SettingsScreen(navigateBack = { navController.popBackStack() })
        }

        composable("MentalHealthScreen") {
            ShowMentalHealthScreen(navigateNext = { navController.navigate(if(isLoggedIn) "ChatScreen" else "LoginScreen") })
        }

        composable("LoginScreen") {
            ShowLoginScreen(navigateNext = { navController.navigate(if(User.signup) "UserDetailsScreen" else "ChatScreen") {
                popUpTo("MentalHealthScreen") {
                    inclusive = true
                }
            } })
        }

        composable("ChatScreen") {
            ShowChatScreen(navController = navController)
        }

        composable("SymptomsScreen") {
            var navigateNext = { navController.navigate("ChatScreen") {
                popUpTo("UserDetailsScreen") {
                    inclusive = true
                }
            } }
            if(User.currentUser!!.symptoms.isNotEmpty()) {
                navigateNext = { navController.popBackStack() }
            }
            ShowSymptomsScreen(navigateNext = navigateNext)
        }

        composable("UserDetailsScreen") {
            var navigateNext = { navController.navigate("SymptomsScreen") {
                popUpTo("LoginScreen") {
                    inclusive = true
                }
            } }
            if(User.currentUser!!.symptoms.isNotEmpty()) {
                navigateNext = { navController.popBackStack() }
            }
            ShowUserDetailsScreen(navigateNext = navigateNext)
        }
    }
}