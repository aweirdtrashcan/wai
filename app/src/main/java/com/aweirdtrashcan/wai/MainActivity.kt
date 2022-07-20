package com.aweirdtrashcan.wai

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aweirdtrashcan.wai.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var multiplePermissionsRequest: ActivityResultLauncher<Array<String>>
    private lateinit var backgroundLocationPermissionRequest: ActivityResultLauncher<String>

    private var hasPermission: Boolean = false
    private val permissionsList: Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @SuppressLint("InlinedApi")
    private val backgroundLocationPermission: String =
        Manifest.permission.ACCESS_BACKGROUND_LOCATION

    private var hasCoarseLocation by Delegates.notNull<Boolean>()
    private var hasFineLocation by Delegates.notNull<Boolean>()
    private var hasAccessBackgroundLocation by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        multiplePermissionsRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            it.entries.forEach { result ->
                when (result.key) {
                    permissionsList[0] -> {
                        hasFineLocation = result.value
                    }
                    permissionsList[1] -> {
                        hasCoarseLocation = result.value
                    }
                }
            }
        }

        backgroundLocationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            hasAccessBackgroundLocation = it
        }

        askPermission()

        val cancellationToken = CancellationTokenSource()
        cancellationToken.token.onCanceledRequested {
            Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show()
        }

        @SuppressLint("MissingPermission")
        if (hasPermission) {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            location.addOnCompleteListener{
                println(
                    "***LOCATION***\nLatitude: ${it.result.latitude}\nLongitude: ${it.result.longitude}"
                )
            }
        }
    }

    private fun checkPermissions() {
        hasCoarseLocation =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        hasFineLocation =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        hasAccessBackgroundLocation =
            if (Build.VERSION.SDK_INT >= Q) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        if (hasFineLocation &&
            hasCoarseLocation &&
            hasAccessBackgroundLocation
        ) { hasPermission = true }

    }

    private fun askPermission() {
        if (!hasFineLocation && !hasCoarseLocation) {
            Toast.makeText(this, "called", Toast.LENGTH_LONG).show()
            multiplePermissionsRequest.launch(permissionsList)
        }
        if (!hasAccessBackgroundLocation) {
            backgroundLocationPermissionRequest.launch(backgroundLocationPermission)
        }
    }
}