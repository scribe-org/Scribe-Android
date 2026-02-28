// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object to provide Retrofit client instance for API calls.
 */
object RetrofitClient {
    private const val BASE_URL = "https://scribe-server.toolforge.org/api/v1/"

    val apiService: ApiService by lazy {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        retrofit.create(ApiService::class.java)
    }
}
