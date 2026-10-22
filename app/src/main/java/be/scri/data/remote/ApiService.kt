// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.data.remote

import be.scri.data.model.DataResponse
import be.scri.data.model.DataVersionResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Defines the API service for fetching data, data version information, and data contracts.
 */
interface ApiService {
    @GET("data/{lang}")
    suspend fun getData(
        @Path("lang") language: String,
    ): DataResponse

    @GET("data-version/{lang}")
    suspend fun getDataVersion(
        @Path("lang") language: String,
    ): DataVersionResponse

    @GET("contracts")
    suspend fun getContract(
        @Query("lang") language: String,
    ): ResponseBody
}

