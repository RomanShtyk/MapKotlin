package com.example.kotlintestapp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.kotlintestapp.R
import com.example.kotlintestapp.models.BugMarker


import kotlinx.android.synthetic.main.fragment_bugmarker.view.*


class MyRecyclerViewAdapter(
    private var mValues: HashMap<Int, BugMarker>
) : RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_bugmarker, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        if(item != null) {
            holder.mIdView.text = item.title
            holder.mContentView.text = item.snippet
        }
        with(holder.mView) {
            tag = item
        }
    }

    fun refreshList(list: HashMap<Int, BugMarker>) {
        mValues = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
