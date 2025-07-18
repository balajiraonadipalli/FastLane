package com.example.lostfound.viewmodel
import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfound.model.LocationDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.util.*

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val _locationDetails = MutableStateFlow<LocationDetails?>(null)
    val locationDetails: StateFlow<LocationDetails?> = _locationDetails

    fun updateLocation(lat: Double, long: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val location = getAddressFromLocations(lat, long, getApplication())
            _locationDetails.value = location
        }
    }

    private fun getAddressFromLocations(lat: Double, long: Double, context: Context): LocationDetails {
        val url =
            "https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$long&key=YOUR_API_KEY"
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

                for (i in 0 until addressComponents.length()) {
                    val component = addressComponents.getJSONObject(i)
                    val types = component.getJSONArray("types")
                    when {
                        types.toString().contains("locality") -> village = component.getString("long_name")
                        types.toString().contains("administrative_area_level_3") -> mandal = component.getString("long_name")
                        types.toString().contains("administrative_area_level_2") -> district = component.getString("long_name")
                        types.toString().contains("administrative_area_level_1") -> state = component.getString("long_name")
                        types.toString().contains("country") -> country = component.getString("long_name")
                    }
                }

                return LocationDetails(lat, long)
            }
        }

        return LocationDetails(lat, long)
    }
}