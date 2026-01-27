package com.tadevolta.gym.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class GeocodeResult(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

data class AddressResult(
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val neighborhood: String,
    val complement: String = ""
)

class LocationHelper(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Geocodifica um endereço em coordenadas (latitude/longitude)
     * @param address Endereço completo para geocodificar
     * @return GeocodeResult com coordenadas ou null se não conseguir geocodificar
     */
    suspend fun geocodeAddress(address: String): GeocodeResult? {
        if (!Geocoder.isPresent()) {
            return null
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(address, 1)
                
                if (addresses.isNullOrEmpty()) {
                    null
                } else {
                    val addressResult = addresses[0]
                    GeocodeResult(
                        latitude = addressResult.latitude,
                        longitude = addressResult.longitude,
                        address = addressResult.getAddressLine(0)
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Faz reverse geocoding: converte coordenadas (latitude/longitude) em endereço
     * @param latitude Latitude da localização
     * @param longitude Longitude da localização
     * @return AddressResult com informações do endereço ou null se não conseguir fazer reverse geocoding
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): AddressResult? {
        if (!Geocoder.isPresent()) {
            return null
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                
                if (addresses.isNullOrEmpty()) {
                    null
                } else {
                    val address = addresses[0]
                    
                    // Extrair informações do endereço
                    val streetAddress = address.getAddressLine(0) ?: ""
                    val city = address.locality ?: address.subAdminArea ?: ""
                    val state = address.adminArea ?: ""
                    val zipCode = address.postalCode ?: ""
                    val neighborhood = address.subLocality ?: address.featureName ?: ""
                    val complement = address.featureName ?: ""
                    
                    AddressResult(
                        address = streetAddress,
                        city = city,
                        state = state,
                        zipCode = zipCode,
                        neighborhood = neighborhood,
                        complement = complement
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun requestLocationUpdates(
        onLocationUpdate: (Location) -> Unit
    ) {
        if (!hasLocationPermission()) {
            return
        }
        
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // 10 segundos
            fastestInterval = 5000 // 5 segundos
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let(onLocationUpdate)
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            // Tratar erro
        }
    }
}
