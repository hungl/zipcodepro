package com.zipcodepro.zipcodepro

import com.google.gson.annotations.SerializedName

/**
 * Created by hunglac on 11/2/18.
 * ZIP Code Response model
 */
data class ZIPCodeResponse(@SerializedName("zip_codes") val zipCodes: ArrayList<String>)
