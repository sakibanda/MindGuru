package app.mindguru.android.ui.User

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import app.mindguru.android.R
import app.mindguru.android.components.Logger
import app.mindguru.android.components.Remote
import app.mindguru.android.data.model.SignInType
import app.mindguru.android.ui.chat.ChatBubble
import app.mindguru.android.ui.components.GoogleLoginButton
import app.mindguru.android.ui.components.NormalLoginButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

private const val TAG = "LoginScreen"

@Composable
fun ShowLoginScreen(
    navigateNext: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()) {
    var loginError = viewModel.error
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val isProcessing by viewModel.processing.collectAsState()
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            Log.e(TAG, "signin trigger with coroutine")
            navigateNext()
        }
    }

    LoginScreen(onGoogleButtonClick = { token, rawNonce ->
        viewModel.updateProcessingState(true)
        viewModel.loginWithToken(token, rawNonce)//, rawNonce = rawNonce
    }, onNormalButtonClick = {
        if (!isProcessing) {
            loginError = false
            viewModel.updateProcessingState(true)
            viewModel.loginWithEmailAndPass()
            //viewModel.continueWithGuest()
        }
    }, isProcessing, loginError)
}

@Composable
fun LoginScreen(
    onGoogleButtonClick: (String, String) -> Unit,
    onNormalButtonClick: () -> Unit,
    isProcessing: Boolean,
    isLoginError: Boolean
) {
    Box(modifier = Modifier
        .pointerInput(Unit) {}
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 5.dp, bottom = 50.dp, start = 10.dp, end = 10.dp)
                .background(color = MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .size(180.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))

            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    imageVector = Icons.Outlined.Bolt,
                    contentDescription = "powered by icon",
                    colorFilter = ColorFilter.tint(
                        MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = stringResource(R.string.powered_by),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )

            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                ChatBubble(message = "To continue, you can login or use guest mode ...", isUserMessage = false)

                if (!isProcessing){
                    Spacer(modifier = Modifier.height(25.dp))
                    GoogleSignInButton(onSuccessCallback = onGoogleButtonClick)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(id = R.string.or).uppercase(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    NormalLoginButton(
                        text = stringResource(if (Remote.getInt("SIGN_IN_MODE") == SignInType.Guest.ordinal) R.string.user_continue else R.string.continue_guest),
                        onClick = onNormalButtonClick
                    )
                }

                if ( isProcessing ) {
                    var text by remember {
                        mutableLongStateOf(0)
                    }
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(500)
                            text++
                            if (text > 3)
                                text = 0
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(20.dp))
                        //please_wait
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(R.string.please_wait))
                                withStyle(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                ) {
                                    append(".".repeat(text.toInt()))
                                }
                                withStyle(
                                    SpanStyle(
                                        color = Color.Transparent
                                    )
                                ) {
                                    append(".".repeat(3-text.toInt()))
                                }
                            },
                            color = MaterialTheme.colorScheme.onBackground,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W600,
                            ),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }

                if (isLoginError) {
                    Spacer(modifier = Modifier.height(20.dp))
                    TextFieldError(textError = stringResource(id = R.string.google_signin_error))
                }
            }

        }

        Column(Modifier.align(Alignment.BottomEnd)) {
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .padding(end = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.tertiary
            )
            PolicyText()
        }
    }
}

@Composable
fun PolicyText() {
    val uriHandler = LocalUriHandler.current
    val terms = stringResource(id = R.string.terms_service)
    val privacy = stringResource(id = R.string.privacy_policy)

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(MaterialTheme.colorScheme.onBackground)) {
            append("${stringResource(id = R.string.policy_text)} ")
        }
        withStyle(
            style = SpanStyle(
                MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            pushStringAnnotation(tag = terms, annotation = terms)
            append(terms)
        }
        append(" & ")
        withStyle(
            style = SpanStyle(
                MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            pushStringAnnotation(tag = privacy, annotation = privacy)
            append(privacy)
        }
    }
    ClickableText(text = annotatedString, style = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    ), modifier = Modifier.padding(5.dp), onClick = { offset ->
        annotatedString.getStringAnnotations(offset, offset)
            .firstOrNull()?.let { span ->
                if (span.item.contentEquals(terms)) {
                    runCatching {
                        uriHandler.openUri(Remote.getString("TERMS_OF_USE"))
                    }.onFailure {
                        //it.printStackTrace()
                        Remote.captureException(it)
                    }
                } else {
                    runCatching {
                        uriHandler.openUri(Remote.getString("PRIVACY_POLICY"))
                    }.onFailure {
                        //it.printStackTrace()
                        Remote.captureException(it)
                    }
                }
            }
    })

    // Text(text = annotatedString,style = MaterialTheme.typography.body1, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp, bottom = 16.dp))
}

@Composable
fun TextFieldError(textError: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = textError,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginPreview() {
    LoginScreen(
        onGoogleButtonClick = { _, _ -> },
        onNormalButtonClick = {},
        isProcessing = false,
        isLoginError = false
    )
}

@Composable
fun GoogleSignInButton(
    onSuccessCallback: (String, String) -> Unit,
) {
    val retryType by remember { mutableStateOf("") }
    var showButton by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = UUID.randomUUID().toString().toByteArray()
    val digest = md.digest(bytes)
    val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }
    val onClick: () -> Unit = {
        //solve getCredentialAsync no provider dependencies found - please ensure the desired provider dependencies are added with sdk 24
        coroutineScope.launch {
            //TODO: Exception None: getCredentialAsync no provider dependencies found - please ensure the desired provider dependencies are added
            try {
                //if SDK < Android 13  Use GoogleIDOption
                /*if(Build.VERSION.SDK_INT < 33) {
                    retryType = "AuthorizedAccountsFalse"
                }*/
                signIn(retryType, context, hashedNonce, onSuccessCallback)
            } catch (e: GoogleIdTokenParsingException) {
                Logger.e(TAG, "GoogleIdTokenParsingException: " + e.message)
                //Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                //no credential
                //if No credentials available show signup with google
                if (e.message?.contains("No credentials")!!) {
                    try {
                        Logger.e(TAG, "Exception AuthorizedAccountsFalse: " + e.message)
                        signIn("AuthorizedAccountsFalse", context, hashedNonce, onSuccessCallback)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Please, add Google account to login.", Toast.LENGTH_LONG).show()
                        Logger.e(TAG, "Exception AuthorizedAccountsFalse Exception: " + e.message)
                        //Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                if(e.message!!.contains("Cannot find a matching credential"))
                    Toast.makeText(context, "Please, add Google account to login.", Toast.LENGTH_LONG).show()

                Logger.e(TAG, "Exception Others: " + e.message)
                //Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*LaunchedEffect(showButton) {
    //Auto Show Login Screen
        if (!showButton) {
            onClick()
            showButton = true
        }
    }*/

    //if (showButton) {
    GoogleLoginButton(
        text = stringResource(id = R.string.sign_in_with_google),
        onClick = onClick
    )
    //}

}
suspend fun signIn(retryType:String, context: Context, hashedNonce: String, onSuccessCallback: (String, String) -> Unit) {
    val credentialManager = CredentialManager.create(context)
    val rawNonce = UUID.randomUUID().toString()
    val request : GetCredentialRequest
    if(retryType == "") {
        val googleIdOption: GetSignInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(Remote.getString("WEB_CLIENT_ID"))
                .setNonce(hashedNonce)
                .build()
        request = GetCredentialRequest.Builder()
            .setPreferImmediatelyAvailableCredentials(false)
            .addCredentialOption(googleIdOption)
            .build()
    }
    else  {
        val googleIdOption: GetGoogleIdOption =
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(retryType == "AuthorizedAccounts")
                .setServerClientId(Remote.getString("WEB_CLIENT_ID"))
                .setNonce(hashedNonce)
                .build()
        request = GetCredentialRequest.Builder()
            .setPreferImmediatelyAvailableCredentials(false)
            .addCredentialOption(googleIdOption)
            .build()
    }
    val result = credentialManager.getCredential(
        request = request,
        context = context,
    )
    val credential = result.credential

    val googleIdTokenCredential = GoogleIdTokenCredential
        .createFrom(credential.data)
    val googleIdToken = googleIdTokenCredential.idToken

    //AppLogger.logD(TAG, "googleIdToken: "+googleIdToken)
    //Toast.makeText(context, "You are signed in !", Toast.LENGTH_SHORT).show()
    onSuccessCallback(googleIdToken, rawNonce)
}
