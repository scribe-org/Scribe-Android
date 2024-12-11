package be.scri.ui.screens.about

import android.content.Context
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.HintUtils
import be.scri.helpers.RatingHelper
import be.scri.helpers.ShareHelper

object AboutUtil {
    fun resetHints(context: Context) {
        HintUtils.resetHints(context)
        (context as MainActivity).showHint("hint_shown_about", R.string.app_about_app_hint)
    }

    fun showAboutHint(context: Context) {
        (context as MainActivity).showHint("hint_shown_about", R.string.app_about_app_hint)
    }

    fun onShareScribeClick(context: Context) {
        ShareHelper.shareScribe(context)
    }

    fun onRateScribeClick(context: Context) {
        RatingHelper.rateScribe(context, context as MainActivity)
    }

    fun onMailClick(context: Context) {
        ShareHelper.sendEmail(context)
    }
}
