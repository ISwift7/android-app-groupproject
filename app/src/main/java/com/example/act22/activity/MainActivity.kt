package com.example.act22.activity

import AssetManagerViewModel
import com.example.act22.viewmodel.AIViewModel
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.act22.viewmodel.AuthenticationViewModel
import com.example.act22.ui.pages.main.CreateMainPage
import com.example.act22.ui.pages.authentication.LandingPage
import com.example.act22.ui.pages.authentication.PassportRecovery
import com.example.act22.ui.pages.authentication.RegistrationSuccess
import com.example.act22.ui.pages.authentication.SignInPage
import com.example.act22.ui.pages.authentication.SignUpPage
import com.example.act22.ui.pages.main.AssetDetails
import com.example.act22.ui.pages.main.FeedbackPage
import com.example.act22.ui.pages.main.Options
import com.example.act22.ui.pages.main.PortfolioBuildingPage

import com.example.act22.ui.pages.main.SupportEmail
import com.example.act22.ui.pages.main.SupportPage
import com.example.act22.ui.pages.main.UserPortfolio
import com.example.act22.ui.pages.main.UserProfile
import com.example.act22.ui.theme.ACT22Theme
import com.example.act22.viewmodel.AssetPriceViewModel
import com.example.act22.viewmodel.PortfolioViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.GoogleApiAvailability
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.act22.data.repository.AssetRepositoryFirebaseImpl
import com.example.act22.payment.PaymentSheetManager
import android.util.Log
import androidx.navigation.NavController
import com.example.act22.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

sealed class Screen(val route: String) {
    object StartPage : Screen("Start_Page")
    object SignInPage : Screen("SignIn_Page")
    object SignUpPage : Screen("SignUp_Page")
    object MainPage : Screen("Main_Page")
    object Portfolio : Screen("Portfolio")
    object PortfolioBuilder : Screen("PortfolioBuilder")
    object PasswordRecovery : Screen("Recovery")
    object RegistrationSuccess : Screen("RegistrationSuccess")
    object Profile : Screen("Profile")
    object Support : Screen("Support")
    object SupportEmail : Screen("Support_Email")
    object Feedback : Screen("Feedback")
    object Options : Screen("Options")
    object AssetDetails : Screen("details/{ID}") {
        fun createRoute(ID: String): String {
            return "details/$ID"
        }
    }
}

val LocalPaymentSheetManager = staticCompositionLocalOf<PaymentSheetManager?> { null }

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthenticationViewModel by viewModels()
    private val portfolioViewModel: PortfolioViewModel by viewModels()
    private val aiViewModel: AIViewModel by viewModels()
    private val assetManagerViewModel by viewModels<AssetManagerViewModel> {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when (modelClass) {
                    AssetManagerViewModel::class.java -> AssetManagerViewModel()
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                } as T
            }
        }
    }
    private val assetPriceViewModel by viewModels<AssetPriceViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AssetPriceViewModel(AssetRepositoryFirebaseImpl()) as T
            }
        }
    }
    private var navController: NavController? = null
    private lateinit var paymentSheetManager: PaymentSheetManager
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        try {

            com.stripe.android.PaymentConfiguration.init(
                applicationContext,
                "pk_test_51OPwQhHuhy8PxLxbxmOKHZGnpUNgFXMJBKxGyHXgKhEbwwKxEPtUAh8Ry8PoQDqyXHtxNvHI8iZQEtWLYhLvlHSq00yPrEPwbf"
            )
            Log.d("MainActivity", "Stripe PaymentConfiguration initialized successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize Stripe PaymentConfiguration: ${e.message}")
        }

        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(this)
        if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                availability.getErrorDialog(this, resultCode, 9000)?.show()
            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(this.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val startScreen = if (authViewModel.checkIfUserIsLoggedIn()){ Screen.MainPage.route } else { Screen.StartPage.route }

        enableEdgeToEdge()
        try {
            paymentSheetManager = PaymentSheetManager(this)
            Log.d("MainActivity", "PaymentSheetManager initialized successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize PaymentSheetManager: ${e.message}")
        }

        setContent {
            ACT22Theme (){
                CompositionLocalProvider(LocalPaymentSheetManager provides paymentSheetManager) {
                    NavigationSetUp(
                        startScreen,
                        onNavControllerAvailable = { controller ->
                            navController = controller
                        },
                        authViewModel,
                        portfolioViewModel,
                        assetManagerViewModel,
                        assetPriceViewModel,
                        aiViewModel,
                        this
                    )
                }
            }
        }
    }

    fun launchGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9000) {
            recreate()
            return
        }

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken

                if (idToken != null) {
                    authViewModel.handleGoogleAuthentication(idToken) { success, message ->
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            navController?.navigate(Screen.MainPage.route)
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to retrieve ID token.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    }
}

@Composable
fun NavigationSetUp(
    startScreen : String,
    onNavControllerAvailable: (NavController) -> Unit,
    authenticationViewModel: AuthenticationViewModel,
    portfolioViewModel: PortfolioViewModel,
    assetManagerViewModel: AssetManagerViewModel,
    assetPriceViewModel: AssetPriceViewModel,
    aiViewModel: AIViewModel,
    mainActivity: MainActivity
){
    val navController = rememberNavController()
    onNavControllerAvailable(navController)
    NavHost(navController = navController, startDestination = startScreen, builder = {
        composable(Screen.StartPage.route){
            LandingPage(
                navController,
                authenticationViewModel
            )
        }
        composable(Screen.SignInPage.route) {
            SignInPage(
                navController,
                authenticationViewModel,
                mainActivity
            )
        }
        composable(Screen.SignUpPage.route) {
            SignUpPage(
                navController,
                authenticationViewModel,
                mainActivity
            )
        }
        composable(Screen.PasswordRecovery.route) {
            PassportRecovery(
                navController,
                authenticationViewModel
            )
        }
        composable(Screen.RegistrationSuccess.route){
            RegistrationSuccess(
                navController,
                authenticationViewModel
            )
        }
        composable(Screen.PortfolioBuilder.route){
            PortfolioBuildingPage(
                navController,
                portfolioViewModel
            )
        }
        composable(Screen.MainPage.route) {
            CreateMainPage(
                navController,
                assetManagerViewModel
            )
        }
        composable(Screen.Portfolio.route) {
            UserPortfolio(
                navController,
                portfolioViewModel
            )
        }
        composable(Screen.Profile.route) {
            UserProfile(navController)
        }
        composable(Screen.Options.route) {
            Options(navController)
        }
        composable(Screen.Support.route) {
            SupportPage(navController)
        }
        composable(Screen.SupportEmail.route) {
            SupportEmail(navController)
        }
        composable(Screen.Feedback.route) {
            FeedbackPage(navController)
        }
        composable(
            Screen.AssetDetails.route,
            arguments = listOf(navArgument("ID") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("ID")
            if (id != null) {
                AssetDetails(
                    navController,
                    assetPriceViewModel,
                    portfolioViewModel,
                    aiViewModel,
                    id
                )
            }
        }


    })
}



