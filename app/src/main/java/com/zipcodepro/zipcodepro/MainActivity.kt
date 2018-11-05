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
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


class MainActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null

    private var zipCodeMainLL: LinearLayout? = null
    private var zipCodeNetworkErrorLL: LinearLayout? = null
    private var reloadBtn: Button? = null

    private var zipCodeEt: EditText? = null
    private var distanceEt: EditText? = null
    private var searchButton: Button? = null
    private var errorTv: TextView? = null
    private var zipCodeListLabelTv: TextView? = null

    private var viewAdapter = ZIPCodeAdapter()
    private var zipCodeListRecyclerView: RecyclerView? = null

    private val format = Constants.RESPONSE_FORMAT_JSON
    private val distanceUnit = Constants.DISTANCE_UNIT_KM

    // Query string to show only ZIP codes
    // TODO: This has to go along with ZIP Code Response model
    private val responseMinimal = Constants.RESPONSE_MININAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.main_progressBar)

        // Layout with network connection
        zipCodeMainLL = findViewById(R.id.main_linearlayout)

        // Layout with no network connection
        zipCodeNetworkErrorLL = findViewById(R.id.main_network_error_linearlayout)
        reloadBtn = findViewById(R.id.main_reload_button)
        reloadBtn?.setOnClickListener {
            checkNetworkConnection()
        }

        // Inputs, labels and button
        zipCodeEt = findViewById(R.id.main_zipcode_et)
        (findViewById<TextView?>(R.id.main_distance_unit_textview))?.text = distanceUnit
        searchButton = findViewById<Button?>(R.id.main_search_btn)
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
        searchButton?.apply {
            isEnabled = !(zipCodeEt?.text.isNullOrEmpty() || distanceEt?.text.isNullOrEmpty())
            setOnClickListener {
                if (isEnabled) {
                    dismissKeyboard(currentFocus)
                    findZIPCodeInTheRadius()
                }
            }
        }

        // Result list
        val viewManager = LinearLayoutManager(this)
        zipCodeListRecyclerView = findViewById<RecyclerView?>(R.id.main_recyclerview)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }

        // Adding text watcher for field validation
        zipCodeEt?.addTextChangedListener(ZIPCodeProTextWatcher(zipCodeEt, distanceEt, searchButton))
        distanceEt?.addTextChangedListener(ZIPCodeProTextWatcher(zipCodeEt, distanceEt, searchButton))

        // Check network connection to update UI in first launch
        checkNetworkConnection()
    }

    override fun onResume() {
        super.onResume()
        // Check network connection and update ui onResume
        checkNetworkConnection()
    }

    private fun checkNetworkConnection(): Boolean {
        return if (NetworkUtils.isConnected(this)) {
            zipCodeMainLL?.visibility = VISIBLE
            zipCodeNetworkErrorLL?.visibility = GONE
            true
        } else {
            zipCodeMainLL?.visibility = GONE
            zipCodeNetworkErrorLL?.visibility = VISIBLE
            false
        }
    }

    // Validating ZIP Code input
    private fun checkZIPCode(): Boolean {
        val zipCodeLength = zipCodeEt?.text?.length ?: 0
        if (zipCodeLength < 5) {
            showUserError(getString(R.string.error_user_invalid_zipcode))
            return false
        }
        return true
    }

    // Validating ZIP distance input
    private fun checkDistance(): Boolean {
        val distanceIntValue = distanceEt?.text?.toString()?.toInt() ?: 0
        if (distanceIntValue <= 0) {
            showUserError(getString(R.string.error_user_invalid_distance))
            return false
        }
        return true
    }

    private fun showAPIError(errorCode: Int?) {
        progressBar?.visibility = View.GONE
        errorTv?.text = if (404 == errorCode) getString(R.string.error_api_zipcode_not_found) else getString(R.string.error_generic)
        errorTv?.visibility = VISIBLE
        zipCodeListLabelTv?.visibility = GONE
        zipCodeListRecyclerView?.visibility = GONE
    }

    private fun showUserError(errorMessage: String) {
        progressBar?.visibility = View.GONE
        errorTv?.text = errorMessage
        errorTv?.visibility = VISIBLE
        zipCodeListLabelTv?.visibility = GONE
        zipCodeListRecyclerView?.visibility = GONE
    }

    private fun hideError() {
        progressBar?.visibility = View.GONE
        zipCodeListLabelTv?.visibility = VISIBLE
        zipCodeListRecyclerView?.visibility = VISIBLE
        errorTv?.visibility = GONE
    }

    private fun dismissKeyboard(currentView: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentView.windowToken, 0)
    }

    private fun findZIPCodeInTheRadius() {
        if (!checkNetworkConnection()) {
            return
        }

        if (!checkZIPCode()) {
            return
        }
        if (!checkDistance()) {
            return
        }

        progressBar?.visibility = View.VISIBLE

        ZIPCodeApiService.create().searchZIPCodeByRadius(resources.getString(R.string.zipcodeAPIKey), format, zipCodeEt?.text.toString(), distanceEt?.text.toString(), distanceUnit, responseMinimal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            hideError()
                            // Remove ZIP code that user had entered from result list
                            val data = result.zipCodes.apply {
                                remove(zipCodeEt?.text?.toString())
                            }
                            viewAdapter.setData(data)
                            Log.d(TAG, "ZIPCode API request successful! ZIP Codes : $data")
                        },
                        { error ->
                            showAPIError((error as? HttpException)?.code())
                            Log.d(TAG, "ZIPCode API request error: ${error.message}")
                        },
                        {
                            Log.d(TAG, "ZIPCode API request completed")
                        }
                )
    }

    // Custom text watcher for ZIP code and distance input validation
    private class ZIPCodeProTextWatcher(var zipcode: EditText?, var distance: EditText?, var searchButton: Button?): TextWatcher {

        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            validateFields()
        }

        private fun validateFields(){
            searchButton?.isEnabled = !(zipcode?.text.isNullOrEmpty() || distance?.text.isNullOrEmpty())
        }
    }

    companion object {
        const val TAG = "ZIPCodeProMainActivity"
    }
}
