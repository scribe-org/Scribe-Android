package be.scri.helpers

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import be.scri.R

class EmojiButtonController(private val context: Context) {
    private var pluralBtn: Button? = null
    private var emojiBtnPhone1: Button? = null
    private var emojiSpacePhone: View? = null
    private var emojiBtnPhone2: Button? = null

    private val TAG = "EmojiButtonController"

    fun initializeViews(activity: AppCompatActivity) {
        pluralBtn = activity.findViewById(R.id.plural_btn)
        Log.d(TAG, "pluralBtn initialized: $pluralBtn")
        Log.d(TAG, "pluralBtn visibility: ${pluralBtn?.visibility}")
        emojiBtnPhone1 = activity.findViewById(R.id.emoji_btn_phone_1)
        emojiSpacePhone = activity.findViewById(R.id.emoji_space_phone)
        emojiBtnPhone2 = activity.findViewById(R.id.emoji_btn_phone_2)
    }

    fun updateButtonVisibility(isAutoSuggestEnabled: Boolean) {
        pluralBtn?.visibility = if (isAutoSuggestEnabled) View.INVISIBLE else View.VISIBLE
        emojiBtnPhone1?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
        emojiSpacePhone?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
        emojiBtnPhone2?.visibility = if (isAutoSuggestEnabled) View.VISIBLE else View.INVISIBLE
        val message = if (isAutoSuggestEnabled) "Emoji suggestions enabled" else "Emoji suggestions disabled"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
