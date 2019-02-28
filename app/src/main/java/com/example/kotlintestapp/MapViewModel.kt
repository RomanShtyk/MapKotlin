package com.example.kotlintestapp

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng

class MapViewModel : ViewModel() {
    val mCheckPoints = MutableLiveData<HashMap<Int, BugMarker>>()
    val mReachedPoints = MutableLiveData<HashMap<Int, BugMarker>>()
    private val mCheckPointsMap = HashMap<Int, BugMarker>()
    private val mReachedPointsMap = HashMap<Int, BugMarker>()
    private lateinit var sharedPref: SharedPreferences
    fun initListOfCheckPoints(context: Context) {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        //sharedPref.edit().clear().apply()
        val i = 0..9
        for (a in i) {
            val bugMarker = BugMarker(
                //LatLng((49.765746 - a * 0.0001), (23.965262 - a * 0.0001))
                LatLng((49.813909 - a * 0.0001), (24.019452 - a * 0.0001)),
                a,
                sharedPref.getBoolean(a.toString(), false)
            )
            if (!sharedPref.getBoolean(a.toString(), false)) {
                mCheckPointsMap[a] = bugMarker
            } else {
                mReachedPointsMap[a] = bugMarker
            }
        }
        mCheckPoints.postValue(mCheckPointsMap)
        mReachedPoints.postValue(mReachedPointsMap)
    }

    fun reachPoint(a: String) {
        if (mCheckPointsMap[a.toInt()] != null) {
            mReachedPointsMap[a.toInt()] = mCheckPointsMap[a.toInt()]!!
            mCheckPointsMap.remove(a.toInt())
            mReachedPoints.postValue(mReachedPointsMap)
            mCheckPoints.postValue(mCheckPointsMap)
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putBoolean(a, true)
            editor.apply()
        }
    }
}