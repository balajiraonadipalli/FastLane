package com.example.lostfound.presentation

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fastlane.R
import com.example.fastlane.ui.theme.primary_dark
import com.example.fastlane.ui.theme.primary_light
import com.example.fastlane.ui.theme.white
import com.example.fastlane.viewmodel.profiles
import com.example.lostfound.model.LocationDetails
import com.example.lostfound.model.Profiles
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val isGettingLocation = remember { mutableStateOf(false) }
    val isUploading = remember { mutableStateOf(false) }
    val locationDetails = remember { mutableStateOf<LocationDetails?>(null) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    coroutineScope.launch(Dispatchers.IO) {
                        val locDetails = getAddressFromLocations(location.latitude, location.longitude, context)
                        val addr = getAddressFromLocation(location.latitude, location.longitude, context)
                        locationDetails.value = locDetails
                        address.value = addr

                        // 🔥 Update in Firestore every 10 sec
                        auth.currentUser?.uid?.let { uid ->
                            val updates = hashMapOf(
                                "address" to addr,
                                "locationDetails" to locDetails
                            )
                            db.collection("users").document(uid).update(updates as Map<String, Any>)
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L).build()
        if (locationPermission.status.isGranted) {
            fusedLocation.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } else {
            locationPermission.launchPermissionRequest()
        }
        onDispose {
            fusedLocation.removeLocationUpdates(locationCallback)
        }
    }

    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                document.toObject(Profiles::class.java)?.let { profile ->
                    name.value = profile.name
                    email.value = profile.email
                    phone.value = profile.phone
                    address.value = profile.address
                    profile.uri?.let { imageUri.value = Uri.parse(it) }
                    locationDetails.value = profile.locationDetails
                }
                isLoading.value = false
            }
        } ?: run { isLoading.value = false }
    }

    if (isLoading.value) {
        LoadingScreen()
    } else {
        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F7FA))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = R.drawable.avatar,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, primary_light, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = white),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.logout), contentDescription = null, modifier = Modifier.size(20.dp),tint = Color.White)
                        Text("Logout", fontSize = 14.sp)
                    }
                }
                Text("Profile Information", style = MaterialTheme.typography.headlineSmall, color = primary_dark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                Spacer(modifier = Modifier.height(24.dp))
                ProfileTextField(name.value, { name.value = it }, "Full Name", Icons.Default.Person, Modifier.fillMaxWidth())
                ProfileTextField(email.value, { email.value = it }, "Email Address", Icons.Default.Email, Modifier.fillMaxWidth())
                ProfileTextField(phone.value, { phone.value = it }, "Vehicle Number", Icons.Default.Warning, Modifier.fillMaxWidth())
                LocationField(address.value, { address.value = it }, isGettingLocation.value, {}, locationPermission, Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (address.value.isEmpty()) {
                            Toast.makeText(context, "Please enter your address", Toast.LENGTH_SHORT).show()
                        } else {
                            isUploading.value = true
                            coroutineScope.launch {
                                profiles(
                                    navController,
                                    name = name.value,
                                    email = email.value,
                                    phone = phone.value,
                                    address = address.value,
                                    db = db,
                                    auth = auth,
                                    context = context,
                                    locationDetails = locationDetails.value ?: LocationDetails()
                                ) { isUploading.value = false }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary_light, contentColor = white),
                    enabled = !isUploading.value && !isGettingLocation.value
                ) {
                    if (isUploading.value) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = white)
                    } else {
                        Text("SAVE PROFILE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// Keep helper functions like LoadingScreen, ProfileTextField, LocationField, getAddressFromLocation(), etc., unchanged.
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = primary_light,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading Profile...",
                color = primary_dark,
                fontSize = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = primary_light
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        modifier = modifier.padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = primary_light,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = primary_light,
            unfocusedLabelColor = Color.Gray,
            cursorColor = primary_light
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
private fun LocationField(
    address: String,
    onValueChange: (String) -> Unit,
    isGettingLocation: Boolean,
    onLocationClick: () -> Unit,
    locationPermission: PermissionState,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = address,
        onValueChange = onValueChange,
        label = { Text("Current Address") },
        trailingIcon = {
            if (isGettingLocation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = primary_light,
                    strokeWidth = 3.dp
                )
            } else {
                IconButton(
                    onClick = {
                        if (!locationPermission.status.isGranted) {
                            locationPermission.launchPermissionRequest()
                        } else {
                            onLocationClick()
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.marker),
                        contentDescription = "Get current location",
                        tint = Color.Unspecified
                    )
                }

            }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        modifier= Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = primary_light,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = primary_light,
            unfocusedLabelColor = Color.Gray,
            cursorColor = primary_light
        ),
    )
}

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private fun getCurrentLocation(
    fusedLocation: FusedLocationProviderClient,
    onSuccess: (Double, Double) -> Unit,
    onFailure: (Exception) -> Unit
) {
    fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            location?.let { onSuccess(it.latitude, it.longitude) }
                ?: run { onFailure(Exception("Location not available")) }
        }
        .addOnFailureListener { onFailure(it) }
}

fun getAddressFromLocations(lat: Double, long: Double,context: Context): LocationDetails {
    val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$long&key=AIzaSyARI55ShS61bvK81pmne8_3nPN1CMxN5pQ"
    val response = URL(url).readText()
    val jsonObject = JSONObject(response)
    if (jsonObject.getString("status") == "OK") {
        val results = jsonObject.getJSONArray("results")
        if (results.length() > 0) {
            val addressComponents = results.getJSONObject(0).getJSONArray("address_components")
            var village = ""
            var mandal = ""
            var district = ""
            var state = ""
            var country = ""
            var region=""

            for (i in 0 until addressComponents.length()) {
                val component = addressComponents.getJSONObject(i)
                val types = component.getJSONArray("types")
                when {
                    types.toString().contains("locality") -> village = component.getString("long_name")
                    types.toString().contains("administrative_area_level_4") -> region = component.getString("long_name")
                    types.toString().contains("administrative_area_level_3") -> mandal = component.getString("long_name")
                    types.toString().contains("administrative_area_level_2") -> district = component.getString("long_name")
                    types.toString().contains("administrative_area_level_1") -> state = component.getString("long_name")
                    types.toString().contains("country") -> country = component.getString("long_name")
                }
            }

            return LocationDetails(
                latitude = lat,
                longitude = long,
            )
        }
    }

    return LocationDetails(
        latitude = lat,
        longitude = long,
    )
}
fun extractDistrict(addressLine: String?): String? {
    if (addressLine.isNullOrEmpty()) return null

    val parts = addressLine.split(", ")
    return if (parts.size > 2) parts[parts.size - 3] else null
}
fun getAddressFromLocation(lat: Double, long: Double, context: Context): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocation(lat, long, 1)
    return if (addresses != null && addresses.isNotEmpty()) {
        "${addresses[0].getAddressLine(0)}, ${addresses[0].locality}, ${addresses[0].adminArea}, ${addresses[0].countryName}"
    } else {
        "Address not found"
    }
}