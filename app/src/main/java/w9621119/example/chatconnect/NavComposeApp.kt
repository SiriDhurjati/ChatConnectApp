package w9621119.example.chatconnect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import w9621119.example.chatconnect.nav.Action
import w9621119.example.chatconnect.nav.Destination.AuthenticationOption
import w9621119.example.chatconnect.nav.Destination.Home
import w9621119.example.chatconnect.nav.Destination.Login
import w9621119.example.chatconnect.nav.Destination.Register
import w9621119.example.chatconnect.ui.theme.FlashChatTheme
import w9621119.example.chatconnect.view.AuthenticationView
import w9621119.example.chatconnect.view.home.HomeView
import w9621119.example.chatconnect.view.login.LoginView
import w9621119.example.chatconnect.view.register.RegisterView

/**
 * The main Navigation composable which will handle all the navigation stack.
 */
/**
 * The main Navigation composable which will handle all the navigation stack.
 */

@Composable
fun NavComposeApp() {
    val navController = rememberNavController()
    val actions = remember(navController) { Action(navController) }
    FlashChatTheme {
        NavHost(
            navController = navController,
            startDestination =
            if (FirebaseAuth.getInstance().currentUser != null)
                Home
            else
                AuthenticationOption
        ) {
            composable(AuthenticationOption) {
                AuthenticationView(
                    register = actions.register,
                    login = actions.login
                )
            }
            composable(Register) {
                RegisterView(
                    home = actions.home,
                    back = actions.navigateBack
                )
            }
            composable(Login) {
                LoginView(
                    home = actions.home,
                    back = actions.navigateBack
                )
            }
            composable(Home) {
                HomeView(
                    authenticationOption = actions.authenticationOption
                )
            }
        }
    }
}