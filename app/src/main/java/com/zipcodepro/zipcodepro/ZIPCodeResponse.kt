package com.zipcodepro.zipcodepro

import com.google.gson.annotations.SerializedName

/**
 * Created by hunglac on 11/2/18.
 */
data class ZIPCodeResponse(@SerializedName("zip_codes") val zipCodes: ArrayList<String>)
