package com.example.kotlintestapp

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.io.Serializable

class BugMarker(
    private val mPositionLatitude: Double,
    private val mPositionLongitude: Double,
    private val counter: Int
) : ClusterItem, Serializable {

    override fun getSnippet(): String {
        return "Snippet"
    }

    override fun getTitle(): String {
        return "Checkpoint #$counter"
    }

    override fun getPosition(): LatLng {
        return LatLng(mPositionLatitude, mPositionLongitude)
    }
}