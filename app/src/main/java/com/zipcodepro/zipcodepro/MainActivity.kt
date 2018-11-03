package com.zipcodepro.zipcodepro

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var zipCode: EditText
    private lateinit var distance: EditText
    private lateinit var searchButton: Button
    private val apiKey = "KD4Q8I2YUz1KzLfxgToinJm51a5FKAMkxftCca1Q238ehmdPEhJLo0kWrbY8fAOw"
    private val format = "radius.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        zipCode = this.findViewById(R.id.main_zipcode)
        distance = findViewById(R.id.main_distance)
        searchButton = findViewById(R.id.main_search_btn)
        viewManager = LinearLayoutManager(this)
        val a = ArrayList<String>()
        viewAdapter = ZIPCodeAdapter()

        recyclerView = findViewById<RecyclerView>(R.id.main_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }

        searchButton.setOnClickListener {
            ZIPCodeApiService.create().searchZIPCodeByRadius(apiKey, format, zipCode.text.toString(), distance.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        (viewAdapter as ZIPCodeAdapter).setData(it.zipCodes)
                    }
        }
    }
}
