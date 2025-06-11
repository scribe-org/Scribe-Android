// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.keyboardDBHelper

import DataContract
import android.util.Log

/**
 * Manages operations related to conjugate data.
 *
 * This class provides methods to process and retrieve information
 * from a `DataContract` object, specifically focusing on its
 * conjugation data.
 */
class ConjugateDataManager {
    /**
     * Logs the keys of the conjugations from the provided JSON data.
     *
     * This function checks if the input `jsonData` is not null.
     * If `jsonData` is valid, it retrieves the keys (labels) from the `conjugations` map
     * and logs them with the tag "ALPHA".
     * If `jsonData` is null, it logs a message indicating that the contract data is null.
     *
     * @param jsonData An optional `DataContract` object that may contain conjugation data.
     *                 If null, no conjugation keys will be processed.
     */
    fun getTheConjugateLabels(jsonData: DataContract?) {
        if (jsonData != null) {
            val output = jsonData.conjugations.keys
            Log.i("ALPHA", "The conjugate keys are $output")
        } else {
            Log.i("ALPHA", "The contract data is null")
        }
    }
}
