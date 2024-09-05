package be.scri.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import be.scri.R
import be.scri.databinding.DialogTextviewBinding
import be.scri.extensions.getStringsPackageName
import be.scri.extensions.launchViewIntent
import be.scri.extensions.setupDialogStuff

class AppSideloadedDialog(
    val activity: Activity,
    val callback: () -> Unit,
) {
    private var dialog: AlertDialog
    private val url = "https://play.google.com/store/apps/details?id=${activity.getStringsPackageName()}"
    private var binding: DialogTextviewBinding = DialogTextviewBinding.inflate(activity.layoutInflater)

    init {
        val view =
            binding.root.apply {
                val text = String.format(activity.getString(R.string.sideloaded_app), url)
                binding.textView.text = Html.fromHtml(text)
                binding.textView.movementMethod = LinkMovementMethod.getInstance()
            }

        dialog =
            AlertDialog
                .Builder(activity)
                .setNegativeButton(R.string.cancel) { dialog, which -> negativePressed() }
                .setPositiveButton(R.string.download, null)
                .setOnCancelListener { negativePressed() }
                .create()
                .apply {
                    activity.setupDialogStuff(view, this)
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        downloadApp()
                    }
                }
    }

    private fun downloadApp() {
        activity.launchViewIntent(url)
    }

    private fun negativePressed() {
        dialog.dismiss()
        callback()
    }
}
