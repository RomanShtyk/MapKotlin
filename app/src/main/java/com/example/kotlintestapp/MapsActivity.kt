package com.example.kotlintestapp

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import org.jetbrains.anko.toast
import java.lang.Math.abs


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLatLng: LatLng
    private lateinit var mClusterManager: ClusterManager<BugMarker>
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private var isChecking: Boolean = false
    private val myLocationHashMap = HashMap<String, Marker>()
    private val mCheckPointsMap = HashMap<Int, BugMarker>()
    private lateinit var viewModel: MapViewModel


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.my_menu, menu)
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_switch -> {
                val listFragment = ListFragment()
                supportFragmentManager.beginTransaction()
                    .add(R.id.container, listFragment)
                    .addToBackStack(null)
                    .commit()
                return true
            }
            R.id.action_mcentre -> {
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(lastLocation.latitude, lastLocation.longitude),
                        19f
                    )
                )
            }
            R.id.action_start ->{
                if(isChecking){
                    val icon: Drawable = resources.getDrawable(R.mipmap.start)
                    item.icon = icon
                    isChecking = false
                } else{
                    val icon: Drawable = resources.getDrawable(R.mipmap.stop)
                    item.icon = icon
                    isChecking = true
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        viewModel.initListOfCheckPoints(this)
        viewModel.mCheckPoints.observe(this, Observer<HashMap<Int, BugMarker>> { hash ->
            if (hash != null) {
                mCheckPointsMap.clear()
                mCheckPointsMap.putAll(hash)
            }
        })
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
                //var index: Int = -1
                if (isChecking) {
                    for (bug in mCheckPointsMap) {
                        if (isNearMyLocation(
                                LatLng(lastLocation.latitude, lastLocation.longitude),
                                bug.value.position
                            )
                        ) {
                            mClusterManager.removeItem(bug.value)
                            viewModel.reachPoint(bug.key.toString())
                            toast("Checkpoint #${bug.key} reached!")
                            break
                        }
                    }
                }
            }
        }
        createLocationRequest()
    }

    private fun isNearMyLocation(myLoc: LatLng, location: LatLng): Boolean {
        return abs(myLoc.latitude - location.latitude) < 0.0002 && abs(myLoc.longitude - location.longitude) < 0.0002
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    override fun onStop() {
        super.onStop()
        locationUpdateState = false
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }


    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)

                placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpMap()
        mClusterManager = ClusterManager(this, mMap)
        val renderer = CustomClusterRenderer(this, mMap, mClusterManager)
        mMap.setOnCameraIdleListener(mClusterManager)
        for (bug in mCheckPointsMap) {
            mClusterManager.addItem(bug.value)
        }
        mClusterManager.renderer = renderer
        mClusterManager.cluster()
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val mMarker = myLocationHashMap["CURRENT"]
        if (mMarker != null) {
            mMarker.remove()
            myLocationHashMap.remove("CURRENT")
        }
        val markerOptions = MarkerOptions().position(location)
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)
            )
        )

        // 2
        val mMarker1: Marker = mMap.addMarker(markerOptions)
        myLocationHashMap["CURRENT"] = mMarker1
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 5000
        // 3
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
}
