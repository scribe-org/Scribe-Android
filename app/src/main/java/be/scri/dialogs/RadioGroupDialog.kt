package be.scri.dialogs

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import be.scri.R
import be.scri.databinding.DialogRadioGroupBinding
import be.scri.extensions.onGlobalLayout
import be.scri.extensions.setupDialogStuff
import be.scri.models.RadioItem

class RadioGroupDialog(
    val activity: Activity,
    val items: ArrayList<RadioItem>,
    val checkedItemId: Int = -1,
    val titleId: Int = 0,
    showOKButton: Boolean = false,
    val cancelCallback: (() -> Unit)? = null,
    val callback: (newValue: Any) -> Unit,
) {
    private val dialog: AlertDialog
    private var wasInit = false
    private var selectedItemId = -1

    private var binding: DialogRadioGroupBinding = DialogRadioGroupBinding.inflate(activity.layoutInflater)

    init {
        val view = binding.root
        binding.dialogRadioGroup.apply {
            for (i in 0 until items.size) {
                val radioButton =
                    (activity.layoutInflater.inflate(R.layout.radio_button, null) as RadioButton).apply {
                        text = items[i].title
                        isChecked = items[i].id == checkedItemId
                        id = i
                        setOnClickListener { itemSelected(i) }
                    }

                if (items[i].id == checkedItemId) {
                    selectedItemId = i
                }

                addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }

        val builder =
            AlertDialog
                .Builder(activity)
                .setOnCancelListener { cancelCallback?.invoke() }

        if (selectedItemId != -1 && showOKButton) {
            builder.setPositiveButton(R.string.ok) { dialog, which -> itemSelected(selectedItemId) }
        }

        dialog =
            builder.create().apply {
                activity.setupDialogStuff(view, this, titleId)
            }

        if (selectedItemId != -1) {
            binding.dialogRadioHolder.apply {
                onGlobalLayout {
                    scrollY = binding.dialogRadioGroup.findViewById<View>(selectedItemId).bottom - height
                }
            }
        }

        wasInit = true
    }

    private fun itemSelected(checkedId: Int) {
        if (wasInit) {
            callback(items[checkedId].value)
            dialog.dismiss()
        }
    }
}
