package com.cremcashcamfin.collateralappraiser.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import android.Manifest

/**
 * Utility object for handling location-related operations using FusedLocationProviderClient.
 */
object MapHelper {

    /**
     * Asynchronously retrieves the device's current location.
     *
     * Note:
     * - Requires the app to have ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission.
     * - Uses the last known location if available; otherwise, requests a fresh location update.
     *
     * @param context The context used to obtain the FusedLocationProviderClient.
     * @return The current [Location] if available, or null if failed or not yet determined.
     */
    @SuppressLint("MissingPermission") // Make sure permissions are checked before calling this
    suspend fun getCurrentLocation(context: Context): Location? {
        // Get the location client
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        // Suspend the coroutine until the location is available
        return suspendCancellableCoroutine { cont ->
            // Try to get the last known location
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Resume with the cached location
                        cont.resume(location)
                    } else {
                        // If last known location is null, request a current one
                        fusedClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                            null // No CancellationToken
                        ).addOnSuccessListener { updatedLocation ->
                            cont.resume(updatedLocation)
                        }.addOnFailureListener {
                            cont.resume(null)
                        }
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
    }

    fun fetchPropertyLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onLocationReceived: (Location?) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_SHORT).show()
            onLocationReceived(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            onLocationReceived(location)
        }
    }

}