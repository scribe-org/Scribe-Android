package be.scri.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import be.scri.R
import be.scri.databinding.DialogCustomIntervalPickerBinding
import be.scri.extensions.beVisibleIf
import be.scri.extensions.hideKeyboard
import be.scri.extensions.setupDialogStuff
import be.scri.extensions.showKeyboard
import be.scri.extensions.value
import be.scri.helpers.DAY_SECONDS
import be.scri.helpers.HOUR_SECONDS
import be.scri.helpers.MINUTE_SECONDS

class CustomIntervalPickerDialog(
    val activity: Activity,
    val selectedSeconds: Int = 0,
    val showSeconds: Boolean = false,
    val callback: (minutes: Int) -> Unit,
) {
    var dialog: AlertDialog
    private var binding: DialogCustomIntervalPickerBinding = DialogCustomIntervalPickerBinding.inflate(activity.layoutInflater)
    var view = binding.root

    init {
        view.apply {
            binding.apply {
                dialogRadioSeconds.beVisibleIf(showSeconds)
                when {
                    selectedSeconds == 0 -> dialogRadioView.check(R.id.dialog_radio_minutes)
                    selectedSeconds % DAY_SECONDS == 0 -> {
                        dialogRadioView.check(R.id.dialog_radio_days)
                        dialogCustomIntervalValue.setText((selectedSeconds / DAY_SECONDS).toString())
                    }

                    selectedSeconds % HOUR_SECONDS == 0 -> {
                        dialogRadioView.check(R.id.dialog_radio_hours)
                        dialogCustomIntervalValue.setText((selectedSeconds / HOUR_SECONDS).toString())
                    }

                    selectedSeconds % MINUTE_SECONDS == 0 -> {
                        dialogRadioView.check(R.id.dialog_radio_minutes)
                        dialogCustomIntervalValue.setText((selectedSeconds / MINUTE_SECONDS).toString())
                    }

                    else -> {
                        dialogRadioView.check(R.id.dialog_radio_seconds)
                        dialogCustomIntervalValue.setText(selectedSeconds.toString())
                    }
                }
            }
        }

        dialog =
            AlertDialog
                .Builder(activity)
                .setPositiveButton(R.string.ok) { dialogInterface, i -> confirmReminder() }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .apply {
                    activity.setupDialogStuff(view, this) {
                        showKeyboard(binding.dialogCustomIntervalValue)
                    }
                }
    }

    private fun confirmReminder() {
        val value = binding.dialogCustomIntervalValue.value
        val multiplier = getMultiplier(binding.dialogRadioView.checkedRadioButtonId)
        val minutes = Integer.valueOf(if (value.isEmpty()) "0" else value)
        callback(minutes * multiplier)
        activity.hideKeyboard()
        dialog.dismiss()
    }

    private fun getMultiplier(id: Int) =
        when (id) {
            R.id.dialog_radio_days -> DAY_SECONDS
            R.id.dialog_radio_hours -> HOUR_SECONDS
            R.id.dialog_radio_minutes -> MINUTE_SECONDS
            else -> 1
        }
}
