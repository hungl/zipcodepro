package com.zipcodepro.zipcodepro

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by hunglac on 11/2/18.
 */
class ZIPCodeAdapter :
        RecyclerView.Adapter<ZIPCodeAdapter.ZIPCodeViewHolder>() {

    private var data = ArrayList<String>()

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ZIPCodeViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): ZIPCodeAdapter.ZIPCodeViewHolder {
            // create a new view
            val textView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.zipcode_textview, parent, false) as TextView
            // set the view's size, margins, paddings and layout parameters

            return ZIPCodeViewHolder(textView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: ZIPCodeViewHolder, position: Int) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            if(position % 2 != 0){
                holder.textView.setBackgroundColor(Color.LTGRAY)
            } else {
                holder.textView.setBackgroundColor(Color.TRANSPARENT)
            }
            holder.textView.text = data[position]
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = data.size

        fun setData(data: ArrayList<String>) {
            this.data = data
            notifyDataSetChanged()
        }
}
