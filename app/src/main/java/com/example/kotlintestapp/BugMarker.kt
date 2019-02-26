package com.example.kotlintestapp

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class BugMarker(private val mPosition: LatLng, private val counter: Int, private var isReached: Boolean) : ClusterItem {

    override fun getSnippet(): String {
        return "Snippet"
    }

    override fun getTitle(): String {
        return "Checkpoint #$counter"
    }

    override fun getPosition(): LatLng {
        return mPosition
    }
}