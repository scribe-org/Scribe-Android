// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object to provide Retrofit client instance for API calls.
 */
object RetrofitClient {
    private const val BASE_URL = "https://scribe-server.toolforge.org/api/v1/"

    private const val CONNECT_TIMEOUT_SECONDS = 60L
    private const val IO_TIMEOUT_SECONDS = 300L

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(IO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        retrofit.create(ApiService::class.java)
    }
}
