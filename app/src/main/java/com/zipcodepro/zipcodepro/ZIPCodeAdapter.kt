package com.zipcodepro.zipcodepro

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by hunglac on 11/2/18.
 * ZIP Code Adapter for recycler view
 */
class ZIPCodeAdapter :
        RecyclerView.Adapter<ZIPCodeAdapter.ZIPCodeViewHolder>() {

    private var data = ArrayList<String>()

    class ZIPCodeViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): ZIPCodeAdapter.ZIPCodeViewHolder {
            val textView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.zipcode_textview, parent, false) as TextView
            return ZIPCodeViewHolder(textView)
        }

        override fun onBindViewHolder(holder: ZIPCodeViewHolder, position: Int) {
            if(position % 2 != 0){
                holder.textView.setBackgroundColor(Color.LTGRAY)
            } else {
                holder.textView.setBackgroundColor(Color.TRANSPARENT)
            }
            holder.textView.text = data[position]
        }

        override fun getItemCount() = data.size

        fun setData(data: ArrayList<String>) {
            this.data = data
            notifyDataSetChanged()
        }
}
