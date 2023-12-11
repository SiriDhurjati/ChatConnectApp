package w9621119.example.chatconnect.nav

import androidx.navigation.NavHostController
import w9621119.example.chatconnect.nav.Destination.AuthenticationOption
import w9621119.example.chatconnect.nav.Destination.Home
import w9621119.example.chatconnect.nav.Destination.Login
import w9621119.example.chatconnect.nav.Destination.Register

/**
 * A set of destination used in the whole application
 */
object Destination {
    const val AuthenticationOption = "authenticationOption"
    const val Register = "register"
    const val Login = "login"
    const val Home = "home"
}

/**
 * Set of routes which will be passed to different composable so that
 * the routes which are required can be taken.
 */
class Action(navController: NavHostController) {
    val home: () -> Unit = {
        navController.navigate(Home) {
            popUpTo(Login) {
                inclusive = true
            }
            popUpTo(Register) {
                inclusive = true
            }
        }
    }
    val login: () -> Unit = { navController.navigate(Login) }
    val register: () -> Unit = { navController.navigate(Register) }
    val authenticationOption: () -> Unit = { navController.navigate(AuthenticationOption) }

    val navigateBack: () -> Unit = { navController.popBackStack() }
}