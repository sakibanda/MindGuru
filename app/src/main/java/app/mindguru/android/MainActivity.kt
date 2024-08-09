package app.mindguru.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import app.mindguru.android.components.Logger
import app.mindguru.android.components.Remote
import app.mindguru.android.components.Utils
import app.mindguru.android.data.model.User
import app.mindguru.android.ui.components.Loading
import app.mindguru.android.ui.navigation.Navigation
import app.mindguru.android.ui.theme.MindGuruTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    //ComponentActivity
    val DEBUG = BuildConfig.DEBUG //BuildConfig.DEBUG //BuildConfig.DEBUG //BuildConfig.DEBUG //false //BuildConfig.DEBUG
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup()

        var startDestination = if(viewModel.getSeverity() == "") "MentalHealthScreen" else if(Firebase.auth.uid != null) "ChatScreen" else "LoginScreen"
        if(Firebase.auth.uid != null){
            viewModel.setupUser()
            Logger.d("MainActivity", "User: ${User.currentUser.toString()}")
        }

        //enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val isLoggedIn = Firebase.auth.uid != null
            var loading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                if(Firebase.auth.uid != null){
                    while (!User.fetched) {
                        delay(1000)
                    }
                    if(User.currentUser?.symptoms == ""){
                        startDestination = "SymptomsScreen"
                    }
                    loading = false
                }else
                    loading = false
            }

            MindGuruTheme {
                if(loading){
                    Loading()
                    return@MindGuruTheme
                }

                //ModalNavigationDrawer(drawerContent = { /*TODO*/ }) {
                    Navigation(navController = navController, startDestination=startDestination, isLoggedIn=isLoggedIn)
                //}
            }
        }
    }

    private fun setup(){
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds =
                if (DEBUG || Remote.checkGate("DEBUG")) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        //remote config setDefaultsAsync
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        CoroutineScope(Dispatchers.IO).launch {
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            remoteConfig.fetchAndActivate().addOnCompleteListener {
                if(it.isSuccessful){
                    Logger.d("MainActivity", "Remote Config fetched and activated")
                }else{
                    Logger.e("MainActivity", "Remote Config fetch failed")
                }
            }.await()
        }
    }
}