package com.example.kotlintestapp.view

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.example.kotlintestapp.adapters.CustomClusterRenderer
import com.example.kotlintestapp.services.MyService
import com.example.kotlintestapp.R
import com.example.kotlintestapp.models.BugMarker
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
    private var backPressCounter: Int = 0

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.my_menu, menu)
        if (menu != null) {
            val actionStartButton: MenuItem = menu.findItem(R.id.action_start)
            isChecking = isServiceRunningInForeground(this, MyService::class.java)
            if (isChecking) {
                actionStartButton.icon = resources.getDrawable(R.mipmap.stop)
            } else {
                actionStartButton.icon = resources.getDrawable(R.mipmap.start)
            }
        }
        return true
    }


    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_switch -> {
                backPressCounter = 0
                val bundle = Bundle()
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
                        toast("Go back to map")
                    }
                }
            }
            R.id.action_start -> {
                if (isChecking) {
                    val icon: Drawable = resources.getDrawable(R.mipmap.start)
                    item.icon = icon
                    isChecking = false

                    val serviceIntent = Intent(this, MyService::class.java)
                    serviceIntent.putExtra("isChecking", false)
                    ContextCompat.startForegroundService(this, serviceIntent)
                    //stopService(serviceIntent)
                    toast("Not working...")

                } else {
                    val icon: Drawable = resources.getDrawable(R.mipmap.stop)
                    item.icon = icon
                    isChecking = true

                    val serviceIntent = Intent(this, MyService::class.java)
                    serviceIntent.putExtra("isChecking", true)
                    ContextCompat.startForegroundService(this, serviceIntent)
                    toast("Working...")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
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
        val serviceIntent = Intent(this, MyService::class.java)
        serviceIntent.putExtra("isInit", true)
        startService(serviceIntent)
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

        val mMarker1: Marker = mMap.addMarker(markerOptions)
        myLocationHashMap["CURRENT"] = mMarker1
    }


    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            if (intent!!.action == "location") {
                lastLocation = intent.extras!!["location"] as Location
                placeMarkerOnMap(LatLng(lastLocation!!.latitude, lastLocation!!.longitude))
            }
            if (intent.action == "listOfPoints") {
                mCheckPointsMap.clear()
                mCheckPointsMap.putAll(intent.extras!!["listOfPoints"] as HashMap<Int, BugMarker>)
                mClusterManager.clearItems()
                for (bug in mCheckPointsMap) {
                    mClusterManager.addItem(bug.value)
                    mClusterManager.cluster()
                }
            }
            if (intent.action == "list") {
                mReachedPointsMap = intent.extras!!["list"] as HashMap<Int, BugMarker>
            }
        }
    }

    private fun isServiceRunningInForeground(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    override fun onBackPressed() {
        when {
            listFragment.isAdded -> super.onBackPressed()
            backPressCounter == 0 -> {
                backPressCounter += 1
                toast("Press back one more time to close app")
            }
            else -> super.onBackPressed()
        }

    }
}
