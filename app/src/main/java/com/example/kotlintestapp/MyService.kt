package com.example.kotlintestapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.PendingIntent
import android.support.v4.app.NotificationCompat


class MyService : Service() {


    override fun onCreate() {
        super.onCreate()

    }


    //    private val NOTIF_ID = 1
//    private val NOTIF_CHANNEL_ID = "com.example.kotlintestapp"
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val notificationIntent = Intent(this, MapsActivity::class.java)
//
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0,
//            notificationIntent, 0
//        )
//
//        startForeground(
//            NOTIF_ID, NotificationCompat.Builder(
//                this,
//                NOTIF_CHANNEL_ID
//            ) // don't forget create a notification channel first
//                .setOngoing(true)
//                .setSmallIcon(R.mipmap.icons8_bug_48)
//                .setContentTitle("Foreground service")
//                .setContentText("Service is running background")
//                .setContentIntent(pendingIntent)
//                .build()
//        )
//
//        return START_NOT_STICKY
//    }

}
