package com.zipcodepro.zipcodepro.data

import com.zipcodepro.zipcodepro.utils.Constants
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
 * ZIP Code API service
 */
interface ZIPCodeApiService {
    @GET("/rest/{apiKey}/{format}/{zipCode}/{distance}/{distanceUnit}")
    fun searchZIPCodeByRadius(
        @Path("apiKey") apiKey: String,
        @Path("format") format: String,
        @Path("zipCode") zipCode: String,
        @Path("distance") distance: String,
        @Path("distanceUnit") distanceUnit: String,
            // Whether to get full address or only ZIP code in the response
            // Empty string for full
            // "minimal" for ZIP code only
        @QueryName() minimal: String
    ): Observable<ZIPCodeResponse>

    companion object {
        fun create(): ZIPCodeApiService {

            // For debugging
            val interceptor = HttpLoggingInterceptor()
            // Level of logging can be determined by build types
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                    .client(client)
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(Constants.ZIPCODE_API_BASE_URL)
                    .build()

            return retrofit.create(ZIPCodeApiService::class.java)
        }
    }
}
