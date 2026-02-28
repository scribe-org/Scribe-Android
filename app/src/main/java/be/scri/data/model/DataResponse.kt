// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the complete data response for a specific language,
 * including its contract and associated data tables.
 */
data class DataResponse(
    @SerializedName("language")
    val language: String,
    @SerializedName("contract")
    val contract: Contract,
    @SerializedName("data")
    val data: Map<String, List<Map<String, Any>>>,
)

/**
 * Represents the contract details for the data response,
 * including version, update timestamp, and field definitions.
 */
data class Contract(
    @SerializedName("version")
    val version: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("fields")
    val fields: Map<String, Map<String, String>>,
)
