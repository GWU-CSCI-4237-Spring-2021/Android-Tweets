package edu.gwu.androidtweets

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.doAsync

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var currentLocation: ImageButton

    private lateinit var confirm: Button

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var locationProvider: FusedLocationProviderClient

    private var currentAddress: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        locationProvider = LocationServices.getFusedLocationProviderClient(this)

        firebaseAuth = FirebaseAuth.getInstance()
        val email = firebaseAuth.currentUser!!.email
        title = getString(R.string.welcome, email)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        currentLocation = findViewById(R.id.current_location)
        confirm = findViewById(R.id.confirm)

        confirm.isEnabled = false
        confirm.setOnClickListener {
            if (currentAddress != null) {
                val intent = Intent(this, TweetsActivity::class.java)
                intent.putExtra("address", currentAddress)
                startActivity(intent)
            }
        }

        currentLocation.setOnClickListener {
            checkLocationPermission()
        }

        // Triggers the loading of the map
        mapFragment.getMapAsync(this)
    }

    // Determine whether we have the location permission, else ask for it
    private fun checkLocationPermission() {
        val locationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (locationPermissionGranted) {
            Log.d("MapsActivity", "Initial permission check - granted")
            useCurrentLocation()
        } else {
            Log.d("MapsActivity", "Initial permission check - not granted")

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                200
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("MapsActivity", "Permission prompt result - granted")
            useCurrentLocation()
        } else {
            Log.d("MapsActivity", "Permission prompt result - not granted")

        }
    }

    // The Location permission has been granted, so we can invoke the GPS to
    // get the user's location
    @SuppressLint("MissingPermission")
    private fun useCurrentLocation() {
        // Request a fresh location
        val locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationProvider.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // A new location has been sensed
            Log.d("MapsActivity", "New location sensed: $locationResult")

            // Stop sensing new locations
            locationProvider.removeLocationUpdates(this)

            val lat = locationResult.lastLocation.latitude
            val lon = locationResult.lastLocation.longitude
            doGeocoding(LatLng(lat, lon))
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener { coords: LatLng ->
            doGeocoding(coords)
        }
    }

    private fun doGeocoding(coords: LatLng) {
        mMap.clear()

        doAsync {
            // Geocoding should be done on a background thread - it involves networking
            // and has the potential to cause the app to freeze (Application Not Responding error)
            // if done on the UI Thread and it takes too long.
            val geocoder: Geocoder = Geocoder(this@MapsActivity)

            // In Kotlin, you can assign the result of a try-catch block. Both the "try" and
            // "catch" clauses need to yield a valid value to assign.
            val results: List<Address> = try {
                geocoder.getFromLocation(
                    coords.latitude,
                    coords.longitude,
                    10
                )
            } catch (e: Exception) {
                Log.e("MapsActivity", "Geocoder failed", e)
                listOf<Address>()
            }

            // Move back to the UI Thread now that we have some results to show.
            // The UI can only be updated from the UI Thread.
            runOnUiThread {
                if (results.isNotEmpty()) {
                    // Potentially, we could show all results to the user to choose from,
                    // but for our usage it's sufficient enough to just use the first result
                    val firstResult = results.first()
                    val postalAddress = firstResult.getAddressLine(0)

                    updateConfirmButton(firstResult)

                    // Add a map marker where the user tapped and pan the camera over
                    mMap.addMarker(MarkerOptions().position(coords).title(postalAddress))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(coords))
                } else {
                    Log.d("MapsActivity", "No results from geocoder!")
                    val toast = Toast.makeText(
                        this@MapsActivity,
                        "No results for location!",
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                }
            }
        }
    }

    /**
     * Flips the button color from red --> green and updates the icon
     */
    private fun updateConfirmButton(address: Address) {
        val greenColor = getColor(R.color.buttonGreen)
        val checkIcon = getDrawable(R.drawable.ic_check)

        confirm.isEnabled = true
        confirm.text = address.getAddressLine(0)
        confirm.setBackgroundColor(greenColor)

        // The four parameters here are the icon you want shown on each of the four sides of the button
        confirm.setCompoundDrawablesWithIntrinsicBounds(checkIcon, null, null, null)

        currentAddress = address
    }
}