package be.scri.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import be.scri.R
import be.scri.databinding.DialogMessageBinding
import be.scri.extensions.setupDialogStuff

// similar fo ConfirmationDialog, but has a callback for negative button too
class ConfirmationAdvancedDialog(
    activity: Activity,
    message: String = "",
    messageId: Int = R.string.proceed_with_deletion,
    positive: Int = R.string.yes,
    negative: Int,
    val callback: (result: Boolean) -> Unit,
) {
    var dialog: AlertDialog
    private var binding: DialogMessageBinding = DialogMessageBinding.inflate(activity.layoutInflater)

    init {
        val view = binding.root
        binding.message.text =
            if (message.isEmpty()) activity.resources.getString(messageId) else message

        val builder =
            AlertDialog
                .Builder(activity)
                .setPositiveButton(positive) { dialog, which -> positivePressed() }

        if (negative != 0) {
            builder.setNegativeButton(negative) { dialog, which -> negativePressed() }
        }

        dialog =
            builder.create().apply {
                activity.setupDialogStuff(view, this)
            }
    }

    private fun positivePressed() {
        dialog.dismiss()
        callback(true)
    }

    private fun negativePressed() {
        dialog.dismiss()
        callback(false)
    }
}
