package com.example.fastlane

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fastlane.authentication.login
import com.example.fastlane.authentication.signUp
import com.example.fastlane.ui.theme.FastLaneTheme
import com.example.lostfound.presentation.HomeScreenWithMap
import com.example.lostfound.presentation.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val db = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            var startDestination by remember { mutableStateOf<String?>(null) }

            // Determine the initial screen
            LaunchedEffect(userId) {
                if (userId != null) {
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            val userRole = document.getString("role")
                            startDestination = if (userRole == "admin") "adminHome" else "Home"
                        }
                        .addOnFailureListener {
                            startDestination = "login"
                        }
                } else {
                    startDestination = "login"
                }
            }

            // Wait for Firebase role check
            if (startDestination != null) {
                FastLaneTheme {
                    NavHost(navController = navController, startDestination = "HomeScreenWithMap") {
                        composable("login") {
                            login(navController)
                        }
                        composable("signUp") {
                            signUp(navController)
                        }
                        composable("ProfileScreen"){
                            ProfileScreen(navController)
                        }
                        composable("HomeScreenWithMap"){
                            HomeScreenWithMap()
                        }

//                        composable("Home") {
//                            Home(navController)
//                        }
//                        composable("adminHome") {
//                            adminHome(navController)
//                        }
                    }
                }
            }
        }
    }
}
