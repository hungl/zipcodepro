package com.zipcodepro.zipcodepro

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by hunglac on 11/4/18.
 * Network utils to handle network check
 */
class NetworkUtils {

    companion object {
        fun isConnected(context: Context?): Boolean {
            val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return connectivityManager?.activeNetworkInfo != null
        }
    }
}
