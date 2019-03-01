package com.example.kotlintestapp

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.preference.PreferenceManager

class MyViewModel : ViewModel() {
    var mCheckPoints = HashMap<Int, BugMarker>()
    var mReachedPoints = HashMap<Int, BugMarker>()
    private lateinit var sharedPref: SharedPreferences
    lateinit var mLocation: Location

    fun initLocation(context: Context){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        mLocation = Location("me")
        mLocation.longitude = sharedPref.getLong("long", 0).toDouble()
        mLocation.latitude = sharedPref.getLong("lat", 0).toDouble()
    }

    fun setLocation(long: Long, lat: Long){
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putLong("long", long)
        editor.putLong("lat", lat)
        editor.apply()
    }

}