package com.example.fastlane.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.example.lostfound.model.LocationDetails
import com.example.lostfound.model.Profiles
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun profiles(
    navController: NavController,
    name: String,
    email: String,
    phone: String,
    address: String,
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    context: Context,
    locationDetails: LocationDetails,
    onComplete: () -> Unit
) {
    val user = auth.currentUser
    if (user == null) {
        Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show()
        return
    }

    val uid = user.uid
    val userRef = db.collection("users").document(uid)

    userRef.get().addOnSuccessListener { document ->
        val profile = Profiles(
            name = name,
            email = email,
            phone = phone,
            address = address,
            locationDetails = locationDetails
        )

        userRef.set(profile)
            .addOnSuccessListener {
                Toast.makeText(context, "Your profile updated successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("HomeScreenWithMap")
                onComplete()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Failed to fetch existing profile: ${e.message}", Toast.LENGTH_SHORT).show()
        onComplete()
    }
}
