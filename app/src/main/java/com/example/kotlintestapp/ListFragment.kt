package com.example.kotlintestapp

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.model.LatLng

class ListFragment : Fragment() {

    private lateinit var viewModel: MapViewModel
    private lateinit var mAdapter: MyRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(MapViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bugmarker_list, container, false)
        mAdapter = MyRecyclerViewAdapter(hashMapOf(1 to BugMarker(LatLng(1.1, 1.1), 1, true)))
        val rv: RecyclerView = view.findViewById(R.id.list)
        rv.adapter = mAdapter
        rv.layoutManager = LinearLayoutManager(context)
        viewModel.mReachedPoints.observe(this, Observer<HashMap<Int, BugMarker>> { hash ->
            mAdapter.refreshList(hash!!)
        })
        return view
    }
}

