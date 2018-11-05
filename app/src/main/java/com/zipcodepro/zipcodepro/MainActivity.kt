package com.zipcodepro.zipcodepro

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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

    private var zipCodeEt: EditText? = null
    private var distanceEt: EditText? = null
    private var viewAdapter = ZIPCodeAdapter()
    private var errorTv: TextView? = null
    private var zipCodeListLabelTv: TextView? = null
    private var zipCodeListRecyclerView: RecyclerView? = null
    private val apiKey = "gXTMe761UEknkYBpIgAKOX00rBZVwMLWF4YtS1sCoLjOlv85TmL1YBLKzSRqtuYj"
    private val format = "radius.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        zipCodeMainLL = findViewById(R.id.main_linearlayout)
        zipCodeNetworkErrorLL = findViewById(R.id.main_network_error_linearlayout)

        reloadBtn = findViewById(R.id.main_reload_button)
        reloadBtn?.setOnClickListener {
            checkNetworkConnection()
        }

        zipCodeEt = findViewById(R.id.main_zipcode_et)
        val searchButton = findViewById<Button?>(R.id.main_search_btn)

        distanceEt = findViewById<EditText?>(R.id.main_distance_et)?.apply {
            setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_SEARCH -> {
                        searchButton?.performClick()
                        true
                    }
                    else -> false
                }
            }
        }
        errorTv = findViewById(R.id.main_error_tv)
        zipCodeListLabelTv = findViewById(R.id.main_list_label_tv)
        val viewManager = LinearLayoutManager(this)

        zipCodeListRecyclerView = findViewById<RecyclerView?>(R.id.main_recyclerview)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }

        searchButton?.apply {
            isEnabled = !(zipCodeEt?.text.isNullOrEmpty() || distanceEt?.text.isNullOrEmpty())
            setOnClickListener {
                if (isEnabled) {
                    dismissKeyboard(currentFocus)
                    findZIPCodeInTheRadius()
                }
            }
        }

        zipCodeEt?.addTextChangedListener(ZIPCodeProTextWatcher(zipCodeEt, distanceEt, searchButton))
        distanceEt?.addTextChangedListener(ZIPCodeProTextWatcher(zipCodeEt, distanceEt, searchButton))

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
        errorTv?.text = if (404 == errorCode) "The ZIP code you provided was not found." else "Something went wrong. Please try again later."
        errorTv?.visibility = VISIBLE
        zipCodeListLabelTv?.visibility = GONE
        zipCodeListRecyclerView?.visibility = GONE
    }

    private fun hideError() {
        zipCodeListLabelTv?.visibility = VISIBLE
        zipCodeListRecyclerView?.visibility = VISIBLE
        errorTv?.visibility = GONE
    }

    private fun dismissKeyboard(currentView: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentView.windowToken, 0)
    }

    private fun findZIPCodeInTheRadius() {
        ZIPCodeApiService.create().searchZIPCodeByRadius(apiKey, format, zipCodeEt?.text.toString(), distanceEt?.text.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            hideError()
                            val data = result.zipCodes.apply {
                                remove(zipCodeEt?.text?.toString())
                            }
                            viewAdapter.setData(data)
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

    private class ZIPCodeProTextWatcher(var zipcode: EditText?, var distance: EditText?, var searchButton: Button?): TextWatcher {

        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            checkEmptyFields()
        }

        private fun checkEmptyFields(){
            searchButton?.isEnabled = !(zipcode?.text.isNullOrEmpty() || distance?.text.isNullOrEmpty())
        }
    }

    companion object {
        const val TAG = "ZIPCodeProMainActivity"
    }
}
