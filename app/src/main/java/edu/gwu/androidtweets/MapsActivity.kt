package edu.gwu.androidtweets

import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.doAsync

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        // Triggers the loading of the map
        mapFragment.getMapAsync(this)
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

                        val toast = Toast.makeText(
                            this@MapsActivity,
                            "You clicked: $postalAddress",
                            Toast.LENGTH_LONG
                        )
                        toast.show()

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
    }
}