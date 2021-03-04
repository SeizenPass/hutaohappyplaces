package com.example.hutaohappyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hutaohappyplaces.R
import com.example.hutaohappyplaces.models.HappyPlaceModel

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_map)
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    val mapFragment = supportFragmentManager
        .findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)
}

/**
 * Manipulates the map once available.
 * This callback is triggered when the map is ready to be used.
 * This is where we can add markers or lines, add listeners or move the camera. In this case,
 * we just add a marker near Sydney, Australia.
 * If Google Play services is not installed on the device, the user will be prompted to install
 * it inside the SupportMapFragment. This method will only be triggered once the user has
 * installed Google Play services and returned to the app.
*/
override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    var details: HappyPlaceModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
    val sydney = LatLng(details.latitude, details.longitude)
    mMap.addMarker(MarkerOptions().position(sydney).title(details.title))
    //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14f))
}
}
