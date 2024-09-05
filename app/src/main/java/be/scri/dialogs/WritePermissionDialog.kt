package be.scri.dialogs

import android.app.Activity
import android.text.Html
import androidx.appcompat.app.AlertDialog
import be.scri.R
import be.scri.activities.BaseSimpleActivity
import be.scri.databinding.DialogWritePermissionBinding
import be.scri.databinding.DialogWritePermissionOtgBinding
import be.scri.extensions.humanizePath
import be.scri.extensions.setupDialogStuff
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class WritePermissionDialog(
    activity: Activity,
    val mode: Mode,
    val callback: () -> Unit,
) {
    sealed class Mode {
        object Otg : Mode()

        object SdCard : Mode()

        data class OpenDocumentTreeSDK30(
            val path: String,
        ) : Mode()

        object CreateDocumentSDK30 : Mode()
    }

    private lateinit var otgBinding: DialogWritePermissionOtgBinding
    private lateinit var binding: DialogWritePermissionBinding

    var dialog: AlertDialog

    init {
        val view = if (mode == Mode.SdCard) binding.root else otgBinding.root

        val glide = Glide.with(activity)
        val crossFade = DrawableTransitionOptions.withCrossFade()
        when (mode) {
            Mode.Otg -> {
                otgBinding.writePermissionsDialogOtgText.setText(R.string.confirm_usb_storage_access_text)
                glide.load(R.drawable.img_write_storage_otg).transition(crossFade).into(otgBinding.writePermissionsDialogOtgImage)
            }
            Mode.SdCard -> {
                glide.load(R.drawable.img_write_storage).transition(crossFade).into(binding.writePermissionsDialogImage)
                glide.load(R.drawable.img_write_storage_sd).transition(crossFade).into(binding.writePermissionsDialogImageSd)
            }
            is Mode.OpenDocumentTreeSDK30 -> {
                val humanizedPath = activity.humanizePath(mode.path)
                otgBinding.writePermissionsDialogOtgText.text =
                    Html.fromHtml(activity.getString(R.string.confirm_storage_access_android_text_specific, humanizedPath))
                glide.load(R.drawable.img_write_storage_sdk_30).transition(crossFade).into(otgBinding.writePermissionsDialogOtgImage)

                otgBinding.writePermissionsDialogOtgImage.setOnClickListener {
                    dialogConfirmed()
                }
            }
            Mode.CreateDocumentSDK30 -> {
                otgBinding.writePermissionsDialogOtgText.text = Html.fromHtml(activity.getString(R.string.confirm_create_doc_for_new_folder_text))
                glide.load(R.drawable.img_write_storage_create_doc_sdk_30).transition(crossFade).into(otgBinding.writePermissionsDialogOtgImage)

                otgBinding.writePermissionsDialogOtgImage.setOnClickListener {
                    dialogConfirmed()
                }
            }
        }

        dialog =
            AlertDialog
                .Builder(activity)
                .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
                .setOnCancelListener {
                    BaseSimpleActivity.funAfterSAFPermission?.invoke(false)
                    BaseSimpleActivity.funAfterSAFPermission = null
                }.create()
                .apply {
                    activity.setupDialogStuff(view, this, R.string.confirm_storage_access_title)
                }
    }

    private fun dialogConfirmed() {
        dialog.dismiss()
        callback()
    }
}
