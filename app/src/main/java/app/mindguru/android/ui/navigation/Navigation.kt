package app.mindguru.android.ui.navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.mindguru.android.ui.User.CallLoginScreen
import app.mindguru.android.ui.User.CallMentalHealthScreen
import app.mindguru.android.ui.User.CallUserDetailsScreen
import app.mindguru.android.ui.Userimport.CallSymptomsScreen
import app.mindguru.android.ui.chat.CallChatScreen

@Composable
fun Navigation(navController: NavHostController, startDestination: String, isLoggedIn: Boolean) {
    NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.fillMaxSize()) {
        composable("MentalHealthScreen") {
            CallMentalHealthScreen(navigateNext = { navController.navigate(if(isLoggedIn) "ChatScreen" else "LoginScreen") })
        }

        composable("LoginScreen") {
            CallLoginScreen(navigateNext = { navController.navigate("UserDetailsScreen") {
                popUpTo("MentalHealthScreen") {
                    inclusive = true
                }
            } })
        }

        composable("ChatScreen") {
            CallChatScreen()
        }

        composable("SymptomsScreen") {
            CallSymptomsScreen(navigateNext = { navController.navigate("ChatScreen") {
                popUpTo("UserDetailsScreen") {
                    inclusive = true
                }
            } })
        }

        composable("UserDetailsScreen") {
            CallUserDetailsScreen(navigateNext = { navController.navigate("SymptomsScreen") {
                popUpTo("LoginScreen") {
                    inclusive = true
                }
            } })
        }
    }
}