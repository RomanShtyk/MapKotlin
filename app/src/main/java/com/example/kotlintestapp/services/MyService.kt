package com.example.kotlintestapp.services

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
import android.content.Intent
import android.provider.Settings
import com.example.kotlintestapp.R
import com.example.kotlintestapp.models.BugMarker
import com.example.kotlintestapp.view.MapsActivity
import org.jetbrains.anko.longToast


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
    private var isInit: Boolean? = true
    private var isChecking: Boolean? = false

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notificationInit()
        sharedPrefInit()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallBackInit()
        createLocationRequest()
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(MapsActivity()) { location ->
            if (location != null) {
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
            }
        }

    }

    private fun locationCallBackInit() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                var removeIt: Int = -1
                if (isChecking != null && isChecking as Boolean) {
                    for (bug in mCheckPointsMap) {
                        if (isNearMyLocation(
                                LatLng(lastLocation.latitude, lastLocation.longitude),
                                bug.value.position
                            )
                        ) {
                            mReachedPointsMap[bug.key] = mCheckPointsMap[bug.key]!!
                            removeIt = bug.key
                            val editor: SharedPreferences.Editor = sharedPref.edit()
                            editor.putBoolean(bug.key.toString(), true)
                            editor.apply()
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
                            break
                        }

                    }
                }
                if (removeIt != -1) {
                    mCheckPointsMap.remove(removeIt)
                    val intentFragment = Intent()
                    intentFragment.action = "list"
                    intentFragment.putExtra("list", mReachedPointsMap)
                    sendBroadcast(intentFragment)

                    val intentListActivity = Intent()
                    intentListActivity.action = "listOfPoints"
                    intentListActivity.putExtra("listOfPoints", mCheckPointsMap)
                    sendBroadcast(intentListActivity)

                    toast("You reached the #$removeIt point")
                }

                if (isInit as Boolean) {
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
    }

    private fun isNearMyLocation(myLoc: LatLng, location: LatLng): Boolean {
        return Math.abs(myLoc.latitude - location.latitude) < 0.0002 && Math.abs(myLoc.longitude - location.longitude) < 0.0002
    }

    private fun sharedPrefInit() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        //sharedPref.edit().clear().apply()
        val i = 0..9
        for (a in i) {
            val bugMarker = BugMarker(
                //LatLng((49.765746 - a * 0.0001),(23.965262 - a * 0.0001) )
                (49.813909 - a * 0.0001), (24.019452 - a * 0.0001),
                a
            )
            if (!sharedPref.getBoolean(a.toString(), false)) {
                mCheckPointsMap[a] = bugMarker
            } else {
                mReachedPointsMap[a] = bugMarker
            }
        }
    }

    private fun notificationInit() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        isChecking = intent?.extras?.getBoolean("isChecking")
        if (isChecking == false)
            stopForeground(true)
        if (isChecking == true) {
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
            } else {
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
        }
        isInit = intent?.extras?.getBoolean("isInit")
        return START_NOT_STICKY
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 3000
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    longToast("You should enable Location service, and press Back")
                    val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)
                } catch (sendEx: IntentSender.SendIntentException) {
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
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }
}
