// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the data version response for a specific language.
 */
data class DataVersionResponse(
    @SerializedName("language")
    val language: String,
    @SerializedName("versions")
    val versions: Map<String, String>,
)
