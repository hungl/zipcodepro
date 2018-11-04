package com.zipcodepro.zipcodepro

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {

    private var zipCodeMainLL: LinearLayout? = null
    private var zipCodeNetworkErrorLL: LinearLayout? = null
    private var reloadBtn: Button? = null

    private var errorTextView: TextView? = null
    private var zipCodeListLabelTextView: TextView? = null
    private var zipCodeListRecyclerView: RecyclerView? = null
    private val apiKey = "gXTMe761UEknkYBpIgAKOX00rBZVwMLWF4YtS1sCoLjOlv85TmL1YBLKzSRqtuYj"
    private val format = "radius.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        zipCodeMainLL = findViewById(R.id.main_zipcode_linearlayout)
        zipCodeNetworkErrorLL = findViewById(R.id.main_zipcode_network_error_linearlayout)

        reloadBtn = findViewById(R.id.main_zipcode_reload_button)
        reloadBtn?.setOnClickListener {
            checkNetworkConnection()
        }

        val zipCode = findViewById<EditText>(R.id.main_zipcode_et)
        val distance = findViewById<EditText?>(R.id.main_distance_et)
        val searchButton = findViewById<Button?>(R.id.main_search_btn)
        errorTextView = findViewById(R.id.main_error_tv)
        zipCodeListLabelTextView = findViewById(R.id.main_zipcode_list_label_tv)
        val viewManager = LinearLayoutManager(this)
        val viewAdapter = ZIPCodeAdapter()

        zipCodeListRecyclerView = findViewById<RecyclerView?>(R.id.main_zipcode_list_recyclerview)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }

        searchButton?.setOnClickListener {
            ZIPCodeApiService.create().searchZIPCodeByRadius(apiKey, format, zipCode?.text.toString(), distance?.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                hideError()
                                viewAdapter.setData(result.zipCodes)
                                Log.d(TAG, "ZIPCode API request successful! ZIP Codes : ${result.zipCodes}")
                            },
                            { error ->
                                showError((error as? HttpException)?.code())
                                Log.d(TAG, "ZIPCode API request error: ${error.message}")
                            },
                            {
                                Log.d(TAG, "ZIPCode API request completed")
                            }
                    )
        }

        checkNetworkConnection()
    }

    override fun onResume() {
        super.onResume()
        checkNetworkConnection()
    }

    private fun checkNetworkConnection() {
        if (NetworkUtils.isConnected(this)) {
            zipCodeMainLL?.visibility = VISIBLE
            zipCodeNetworkErrorLL?.visibility = GONE
        } else {
            zipCodeMainLL?.visibility = GONE
            zipCodeNetworkErrorLL?.visibility = VISIBLE
        }
    }

    private fun showError(errorCode: Int?) {
        errorTextView?.text = if (404 == errorCode) "The ZIP code you provided was not found." else "Something went wrong. Please try again later."
        errorTextView?.visibility = VISIBLE
        zipCodeListLabelTextView?.visibility = GONE
        zipCodeListRecyclerView?.visibility = GONE
    }

    private fun hideError() {
        zipCodeListLabelTextView?.visibility = VISIBLE
        zipCodeListRecyclerView?.visibility = VISIBLE
        errorTextView?.visibility = GONE
    }

    companion object {
        const val TAG = "ZIPCodeProMainActivity"

    }
}
