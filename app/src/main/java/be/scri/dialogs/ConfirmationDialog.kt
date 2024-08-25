package be.scri.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import be.scri.R
import be.scri.databinding.DialogMessageBinding
import be.scri.extensions.setupDialogStuff

/**
 * A simple dialog without any view, just a messageId, a positive button and optionally a negative button
 *
 * @param activity has to be activity context to avoid some Theme.AppCompat issues
 * @param message the dialogs message, can be any String. If empty, messageId is used
 * @param messageId the dialogs messageId ID. Used only if message is empty
 * @param positive positive buttons text ID
 * @param negative negative buttons text ID (optional)
 * @param callback an anonymous function
 */
class ConfirmationDialog(
    activity: Activity, message: String = "", messageId: Int = R.string.proceed_with_deletion, positive: Int = R.string.yes,
    negative: Int = R.string.no, val cancelOnTouchOutside: Boolean = true, val callback: () -> Unit
) {
    var dialog: AlertDialog
    private var binding: DialogMessageBinding = DialogMessageBinding.inflate(activity.layoutInflater)


    init {
        val view = binding.root
        binding.message.text = if (message.isEmpty()) activity.resources.getString(messageId) else message

        val builder = AlertDialog.Builder(activity)
            .setPositiveButton(positive) { dialog, which -> dialogConfirmed() }

        if (negative != 0) {
            builder.setNegativeButton(negative, null)
        }

        dialog = builder.create().apply {
            activity.setupDialogStuff(view, this, cancelOnTouchOutside = cancelOnTouchOutside)
        }
    }

    private fun dialogConfirmed() {
        dialog.dismiss()
        callback()
    }
}
