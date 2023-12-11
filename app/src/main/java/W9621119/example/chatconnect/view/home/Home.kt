package W9621119.example.chatconnect.view.home
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.viewmodel.compose.viewModel
import W9621119.example.chatconnect.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

/**
 * The home view which will contain all the code related to the view for HOME.
 *
 * Here we will show the list of chat messages sent by user.
 * And also give an option to send a message and logout.
 */


@Composable
fun SingleMessage(
    username: String,
    message: String,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp,
        backgroundColor = if (isCurrentUser) Color.Magenta else Color.White
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Text(text = username, style = TextStyle(fontWeight = FontWeight.Bold))
            Text(text = message)
        }
    }
}
@SuppressLint("MissingPermission")
@Composable
fun HomeView(
    authenticationOption: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val message: String by homeViewModel.message.observeAsState(initial = "")
    val messages: List<Map<String, Any>> by homeViewModel.messages.observeAsState(
        initial = emptyList<Map<String, Any>>().toMutableList()
    )
    val context = LocalContext.current

    // Check for location permission and request if needed
    val locationPermissionGranted = checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!locationPermissionGranted) {
        requestLocationPermissions(context)
    }

    // Retrieve location if permission is granted
    var location: Location? = null
    if (locationPermissionGranted) {
        location = getLocation(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 0.85f, fill = true),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            reverseLayout = true
        ) {
            items(messages) { message ->
                val isCurrentUser = message[Constants.IS_CURRENT_USER] as

                        Boolean

                SingleMessage(
                    message = message[Constants.MESSAGE].toString(),
                    isCurrentUser = isCurrentUser,
                    username = message[Constants.SENT_BY].toString()
                )
            }
        }

        OutlinedTextField(
            value = message,
            onValueChange = {
                homeViewModel.updateMessage(it)
            },
            label = {
                Text(
                    "Type Your Message"
                )
            },
            maxLines = 1,
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 1.dp)
                .fillMaxWidth()
                .weight(weight = 0.09f, fill = true),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            singleLine = true,
            trailingIcon = {

                Row {

                    // Add a new icon button here
                    IconButton(
                        onClick = {

                            if (locationPermissionGranted) {
                                if (location != null) {
                                    val latitude = location.latitude
                                    val longitude = location.longitude
                                    //homeViewModel.updateMessage("$message | Latitude: ${latitude.toString()}, Longitude: ${longitude.toString()}")
                                    hitApi(latitude, longitude,homeViewModel)
                                } else {
                                    hitApi(54.569698, -1.2358553,homeViewModel)
                                    //homeViewModel.updateMessage("Unable to retrieve location. Please try again later.")
                                }
                            } else {
                                homeViewModel.updateMessage("Location permission not granted. Please enable location services and try again.")
                            }

                            // Use latitude and longitude to display the user's location
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Button"
                        )
                    }

                    // Icon button for sending
                    IconButton(
                        onClick = {
                            homeViewModel.addMessage()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Button"
                        )
                    }

                }

            }
        )

        // Sign out Button
        Button(
            onClick = {
                Firebase.auth.signOut()
                homeViewModel.signout(authenticationOption = authenticationOption)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Sign Out")
        }
    }
}
fun getLocation(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Check for location permission
    if (checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Request permissions if not granted
        // This is just a placeholder, you should handle the permission request appropriately
        // See the documentation for ActivityCompat#requestPermissions for more details.
        requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            123
        )
        return null
    }

    return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
}


fun requestLocationPermissions(context: Context) {
    val permissionsArray = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    ActivityCompat.requestPermissions(context as Activity, permissionsArray, 100)
}
fun hitApi(latitude: Double, longitude: Double,homeViewModel: HomeViewModel) {
    GlobalScope.launch(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://address-from-to-latitude-longitude.p.rapidapi.com/geolocationapi?lat=$latitude&lng=$longitude")
            .get()
            .addHeader("X-RapidAPI-Key", "f45da053aemsh1f1c9676e61694cp1c68aajsn9ba30da96344")
            .addHeader("X-RapidAPI-Host", "address-from-to-latitude-longitude.p.rapidapi.com")
            .build()

        try {

            val response = client.newCall(request).execute()

// Check if the request was successful (status code 200)
            if (response.isSuccessful) {
                // Read the response body only once and store it in a variable
                val responseBodyString = response.body()?.string()
                val jsonResponse = JSONObject(responseBodyString)
                val resultsArray = jsonResponse.getJSONArray("Results")
                if (resultsArray.length() > 0) {
                    val resultObject = resultsArray.getJSONObject(0)

                    val address = resultObject.getString("address")
                    withContext(Dispatchers.Main) {
                        homeViewModel.updateMessage(address)
                    }
                    Log.d("Address", address)
                }
                // Log the JSON response
                if (responseBodyString != null) {
                    Log.d("Response", responseBodyString)
                }
            } else {
                // Handle unsuccessful response (non-200 status code)
                Log.e("Response", "Unsuccessful response: ${response.code()}")
            }
        } catch (e: IOException) {
            // Handle network error
            withContext(Dispatchers.Main) {
                //onError("Network error: ${e.message}")
            }
        }
    }
}