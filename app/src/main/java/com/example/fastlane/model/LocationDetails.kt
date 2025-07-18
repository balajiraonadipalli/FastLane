package com.example.lostfound.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationDetails(
    val latitude: Double,
    val longitude: Double,
) : Parcelable {
    constructor() : this(0.0, 0.0)
}
