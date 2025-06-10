package be.scri.helpers.keyboardDBHelper

import ContractDataLoader
import DataContract
import android.content.Context
import android.util.Log

class ConjugateDataManager (
    private val context: Context,
){
    fun getTheConjugateLabels(language: String ,  jsonData: DataContract?) {
        if (jsonData != null) {
            val output = jsonData.conjugations.keys
            Log.i("ALPHA","The conjugate keys are $output")
        }
        else {
            Log.i("ALPHA","The contract data is null")
        }

    }
}
