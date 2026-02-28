// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.data.remote

import be.scri.data.model.DataResponse
import be.scri.data.model.DataVersionResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Defines the API service for fetching data and data version information.
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
}
