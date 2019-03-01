package com.example.kotlintestapp

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.toast


class MyService : Service() {

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLatLng: LatLng
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "com.example.kotlintestapp"
    private val description = "Point reached notification"
    private val mCheckPointsMap = HashMap<Int, BugMarker>()
    private val mReachedPointsMap = HashMap<Int, BugMarker>()
    private lateinit var sharedPref: SharedPreferences
    private var isInit: Boolean = true

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun isNearMyLocation(myLoc: LatLng, location: LatLng): Boolean {
        return Math.abs(myLoc.latitude - location.latitude) < 0.0002 && Math.abs(myLoc.longitude - location.longitude) < 0.0002
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val int = Intent(applicationContext, MapsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            int,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = Notification.Builder(applicationContext, channelId)
                .setContentTitle("Service is working")
                .setSmallIcon(R.mipmap.icons8_bug_48)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        applicationContext.resources,
                        R.mipmap.icons8_bug_48
                    )
                )
                .setContentIntent(pendingIntent)
        } else{
            builder = Notification.Builder(applicationContext)
                .setContentTitle("Service is working")
                .setSmallIcon(R.mipmap.icons8_bug_48)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        applicationContext.resources,
                        R.mipmap.icons8_bug_48
                    )
                )
                .setContentIntent(pendingIntent)
        }
        startForeground(1, builder.build())

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        //sharedPref.edit().clear().apply()
        val i = 0..9
        for (a in i) {
            val bugMarker = BugMarker(
                //LatLng((49.765746 - a * 0.0001), (23.965262 - a * 0.0001))
                (49.813909 - a * 0.0001), (24.019452 - a * 0.0001),
                a,
                sharedPref.getBoolean(a.toString(), false)
            )
            if (!sharedPref.getBoolean(a.toString(), false)) {
                mCheckPointsMap[a] = bugMarker
            } else {
                mReachedPointsMap[a] = bugMarker
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                var removeIt: Int = -1
                for(bug in mCheckPointsMap){
                if(isNearMyLocation(LatLng(lastLocation.latitude, lastLocation.longitude),bug.value.position)){

                    mReachedPointsMap[bug.key] = mCheckPointsMap[bug.key]!!

                    removeIt = bug.key

                    val editor: SharedPreferences.Editor = sharedPref.edit()
                    editor.putBoolean(bug.key.toString(), true)
                    editor.apply()
                    //notification
                    toast("You reached the #${bug.key} point")
                    val intent = Intent(applicationContext, MapsActivity::class.java)
                    val pending = PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        builder = Notification.Builder(applicationContext, channelId)
                            .setContentTitle("Congratulations!!!")
                            .setContentText("You reached the #${bug.key} point")
                            .setSmallIcon(R.mipmap.icons8_bug_48)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(
                                    applicationContext.resources,
                                    R.mipmap.icons8_bug_48
                                )
                            )
                            .setContentIntent(pending)
                    } else {

                        builder = Notification.Builder(applicationContext)
                            .setContentTitle("Congratulations!!!")
                            .setContentText("You reached the #${bug.key} point")
                            .setSmallIcon(R.mipmap.icons8_bug_48)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(
                                    applicationContext.resources,
                                    R.mipmap.icons8_bug_48
                                )
                            )
                            .setContentIntent(pending)
                    }
                    notificationManager.notify(1234, builder.build())
                    //end notification

                    val intentFragment = Intent()
                    intentFragment.action = "list"
                    intentFragment.putExtra("list", mReachedPointsMap)
                    sendBroadcast(intentFragment)

                    val intentListActivity = Intent()
                    intentListActivity.action = "listOfPoints"
                    intentListActivity.putExtra("listOfPoints", mCheckPointsMap)
                    sendBroadcast(intentListActivity)
                    break
                    }

                }

                if(removeIt != -1)
                mCheckPointsMap.remove(removeIt)

                if(isInit){
                    val intentFragment = Intent()
                    intentFragment.action = "list"
                    intentFragment.putExtra("list", mReachedPointsMap)
                    sendBroadcast(intentFragment)

                    val intentListActivity = Intent()
                    intentListActivity.action = "listOfPoints"
                    intentListActivity.putExtra("listOfPoints", mCheckPointsMap)
                    sendBroadcast(intentListActivity)
                    isInit = false
                }

                val intentLocationActivity = Intent()
                intentLocationActivity.action = "location"
                intentLocationActivity.putExtra("location", p0.lastLocation)
                sendBroadcast(intentLocationActivity)
            }
        }
        createLocationRequest()

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                MapsActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(MapsActivity()) { location ->
            if (location != null) {
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)


            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_NOT_STICKY
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 3000
        // 3
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            //startLocationUpdates()
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
                        MapsActivity(),
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                //redo
                MapsActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }




}
