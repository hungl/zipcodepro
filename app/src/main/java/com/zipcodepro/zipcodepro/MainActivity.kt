package com.zipcodepro.zipcodepro

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {

    private var errorTextView: TextView? = null
    private var zipCodeListLabelTextView: TextView? = null
    private var zipCodeListRecyclerView: RecyclerView? = null
    private val apiKey = "gXTMe761UEknkYBpIgAKOX00rBZVwMLWF4YtS1sCoLjOlv85TmL1YBLKzSRqtuYj"
    private val format = "radius.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

    private fun showError(errorCode: Int?) {
        errorTextView?.text = if (404 == errorCode) "The ZIP code you provided was not found." else "Something went wrong. Please try again later."
        errorTextView?.visibility = View.VISIBLE
        zipCodeListLabelTextView?.visibility = View.GONE
        zipCodeListRecyclerView?.visibility = View.GONE
    }

    private fun hideError() {
        zipCodeListLabelTextView?.visibility = View.VISIBLE
        zipCodeListRecyclerView?.visibility = View.VISIBLE
        errorTextView?.visibility = View.GONE
    }

    companion object {
        const val TAG = "ZIPCodeProMainActivity"

    }
}
