package com.example.kotlintestapp.view


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.kotlintestapp.adapters.MyRecyclerViewAdapter
import com.example.kotlintestapp.R
import com.example.kotlintestapp.adapters.RecyclerViewEmptyViewSupport
import com.example.kotlintestapp.models.BugMarker

class ListFragment : Fragment() {

    private lateinit var mAdapter: MyRecyclerViewAdapter
    private val mReachedPointsMap = HashMap<Int, BugMarker>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bugmarker_list, container, false)
        mAdapter = MyRecyclerViewAdapter(hashMapOf(1 to BugMarker(1.1, 1.1, 1)))
        val rv: RecyclerViewEmptyViewSupport = view.findViewById(R.id.r_view)
        rv.adapter = mAdapter
        val emptyView: TextView = view.findViewById(R.id.empty)
        rv.setEmptyView(emptyView)
        rv.layoutManager = LinearLayoutManager(context)
        if(arguments != null){
            mReachedPointsMap.putAll(arguments!!["list"] as HashMap<Int, BugMarker>)
            mAdapter.refreshList(mReachedPointsMap)
        }

        return view
    }

}

