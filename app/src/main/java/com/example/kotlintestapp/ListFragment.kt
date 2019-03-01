package com.example.kotlintestapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.model.LatLng

class ListFragment : Fragment() {

    private lateinit var mAdapter: MyRecyclerViewAdapter
    private val mReachedPointsMap = HashMap<Int, BugMarker>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bugmarker_list, container, false)
        mAdapter = MyRecyclerViewAdapter(hashMapOf(1 to BugMarker(1.1, 1.1, 1, true)))
        val rv: RecyclerView = view.findViewById(R.id.list)
        rv.adapter = mAdapter
        rv.layoutManager = LinearLayoutManager(context)
        if(arguments != null){
            mReachedPointsMap.putAll(arguments!!["list"] as HashMap<Int, BugMarker>)
            mAdapter.refreshList(mReachedPointsMap)
        }

        return view
    }

}

