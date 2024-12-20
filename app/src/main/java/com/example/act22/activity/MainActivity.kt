package com.example.act22.activity


import com.example.act22.viewmodel.AIViewModel
import AssetManagerViewModel
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
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
import com.example.act22.ui.pages.main.SettingsPage
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
    object Settings : Screen("Settings")
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


class MainActivity : ComponentActivity() {

    private val authViewModel: AuthenticationViewModel by viewModels()
    private val portfolioViewModel: PortfolioViewModel by viewModels()
    private val assetManagerViewModel: AssetManagerViewModel by viewModels {
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
    private val aiViewModel: AIViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Check Google Play Services
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

        val startScreen = if (authViewModel.checkIfUserIsLoggedIn()){ Screen.MainPage.route } else { Screen.StartPage.route }

        enableEdgeToEdge()
        setContent {
            ACT22Theme (){
                NavigationSetUp(
                    startScreen,
                    authViewModel,
                    portfolioViewModel,
                    assetManagerViewModel,
                    assetPriceViewModel,
                    aiViewModel
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9000) {
            // Retry initialization after user action
            recreate()
        }
    }
}

@Composable
fun NavigationSetUp(
    startScreen : String,
    authenticationViewModel: AuthenticationViewModel,
    portfolioViewModel: PortfolioViewModel,
    assetManagerViewModel: AssetManagerViewModel,
    assetPriceViewModel: AssetPriceViewModel,
    aiViewModel: AIViewModel
){
    val navController = rememberNavController()
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
                authenticationViewModel
            )
        }
        composable(Screen.SignUpPage.route) {
            SignUpPage(
                navController,
                authenticationViewModel
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
        composable(Screen.Settings.route) {
            SettingsPage(navController)
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



