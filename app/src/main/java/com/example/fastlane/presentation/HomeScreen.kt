package com.example.lostfound.presentation

import android.Manifest
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import com.example.lostfound.model.LocationDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreenWithMap() {
    val context = LocalContext.current
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()
    val locationDetails = remember { mutableStateOf<LocationDetails?>(null) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    coroutineScope.launch(Dispatchers.IO) {
                        val locDetails = getAddressFromLocations(location.latitude, location.longitude, context)
                        locationDetails.value = locDetails
                        val latLng = LatLng(location.latitude, location.longitude)
                        currentLatLng = latLng

                        // Push to Firestore
                        auth.currentUser?.uid?.let { uid ->
                            val updates = mapOf(
                                "address" to getAddressFromLocation(location.latitude, location.longitude, context),
                                "locationDetails" to locDetails
                            )
                            db.collection("users").document(uid)
                                .update(updates)
                                .addOnFailureListener {
                                    Log.e("MapScreen", "Failed to update location: ${it.message}")
                                }
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

    Box(modifier = Modifier.fillMaxSize()) {
        currentLatLng?.let { latLng ->
            LaunchedEffect(latLng) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = latLng),
                    title = "You",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Fetching your live location...", color = Color.Gray)
        }
    }
}