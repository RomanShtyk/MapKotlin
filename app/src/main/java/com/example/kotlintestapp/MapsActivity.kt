package com.example.kotlintestapp

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var mClusterManager: ClusterManager<BugMarker>

    private val myLocationHashMap = HashMap<String, Marker>()
    private val mCheckPointsMap = HashMap<Int, BugMarker>()
    private val listFragment = ListFragment()
    private lateinit var mIntentFilter: IntentFilter
    private var lastLocation: Location? = null
    private var mReachedPointsMap = HashMap<Int, BugMarker>()
    private var isChecking: Boolean = false
    private lateinit var viewModel: MyViewModel

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.my_menu, menu)
        return true
    }


    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_switch -> {
                val bundle: Bundle = Bundle()
                bundle.putSerializable("list", mReachedPointsMap)
                listFragment.arguments = bundle
                if (!listFragment.isAdded) {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.container, listFragment)
                        .addToBackStack(null)
                        .commit()
                }
                return true
            }
            R.id.action_mcentre -> {
                if (lastLocation != null) {
                    if (!listFragment.isAdded) {
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastLocation!!.latitude, lastLocation!!.longitude),
                                19f
                            )
                        )
                    } else {
                        toast("Wait...")
                    }
                }
            }
            R.id.action_start -> {

                if (isChecking) {
                    val icon: Drawable = resources.getDrawable(R.mipmap.start)
                    item.icon = icon
                    isChecking = false

                    val serviceIntent = Intent(this, MyService::class.java)
                    stopService(serviceIntent)

                } else {
                    val icon: Drawable = resources.getDrawable(R.mipmap.stop)
                    item.icon = icon
                    isChecking = true
                    //foreground service

                    val serviceIntent = Intent(this, MyService::class.java)
                    ContextCompat.startForegroundService(this, serviceIntent)

                }
            }
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //передавати інтент в сервіс
        viewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
        viewModel.initLocation(this)
        mReachedPointsMap = viewModel.mReachedPoints
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction("location")
        mIntentFilter.addAction("listOfPoints")
        mIntentFilter.addAction("list")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    public override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, mIntentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, MyService::class.java)
        stopService(serviceIntent)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mClusterManager = ClusterManager(this, mMap)
        val renderer = CustomClusterRenderer(this, mMap, mClusterManager)
        mMap.setOnCameraIdleListener(mClusterManager)
        mClusterManager.renderer = renderer
        mCheckPointsMap.putAll(viewModel.mCheckPoints)
        for (bug in mCheckPointsMap) {
            mClusterManager.addItem(bug.value)
            mClusterManager.cluster()
        }
        lastLocation = viewModel.mLocation
        placeMarkerOnMap(LatLng(lastLocation!!.latitude, lastLocation!!.longitude))
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


    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            if (intent!!.action == "location") {
                lastLocation = intent.extras["location"] as Location
                viewModel.mLocation = lastLocation as Location
                viewModel.setLocation(lastLocation!!.longitude.toLong(), lastLocation!!.latitude.toLong())
                placeMarkerOnMap(LatLng(lastLocation!!.latitude, lastLocation!!.longitude))
            }
            if (intent.action == "listOfPoints") {
                mCheckPointsMap.clear()
                mCheckPointsMap.putAll(intent.extras["listOfPoints"] as HashMap<Int, BugMarker>)
                viewModel.mCheckPoints = mCheckPointsMap
                mClusterManager.clearItems()
                for (bug in mCheckPointsMap) {
                    mClusterManager.addItem(bug.value)
                    mClusterManager.cluster()
                }
            }
            if (intent.action == "list") {
                mReachedPointsMap = intent.extras["list"] as HashMap<Int, BugMarker>
                viewModel.mReachedPoints = mReachedPointsMap
            }
        }
    }

}
