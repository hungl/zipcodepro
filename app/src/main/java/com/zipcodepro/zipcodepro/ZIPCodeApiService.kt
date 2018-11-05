package com.zipcodepro.zipcodepro

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryName

/**
 * Created by hunglac on 11/2/18.
 */
interface ZIPCodeApiService {
    @GET("/rest/{apiKey}/{format}/{zipCode}/{distance}/{distanceUnit}")
    fun searchZIPCodeByRadius(
            @Path("apiKey") apiKey: String,
            @Path("format") format: String,
            @Path("zipCode") zipCode: String,
            @Path("distance") distance: String,
            @Path("distanceUnit") distanceUnit: String,
            @QueryName() minimal: String
    ): Observable<ZIPCodeResponse>

    companion object {
        fun create(): ZIPCodeApiService {

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                    .client(client)
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl("https://www.zipcodeapi.com")
                    .build()

            return retrofit.create(ZIPCodeApiService::class.java)
        }
    }
}
