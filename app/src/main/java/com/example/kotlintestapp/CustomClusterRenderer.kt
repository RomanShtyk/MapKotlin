package com.example.kotlintestapp

import android.content.Context
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


class CustomClusterRenderer(
    mContext: Context, map: GoogleMap,
    clusterManager: ClusterManager<BugMarker>
) : DefaultClusterRenderer<BugMarker>(mContext, map, clusterManager) {

    override fun onBeforeClusterItemRendered(
        item: BugMarker,
        markerOptions: MarkerOptions?
    ) {
        val markerDescriptor: BitmapDescriptor =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
        markerOptions?.icon(markerDescriptor)
    }
}