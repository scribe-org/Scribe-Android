package be.scri.extensions

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import be.scri.R
import be.scri.activities.BaseSimpleActivity
import be.scri.helpers.isOnMainThread
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

fun AppCompatActivity.updateActionBarTitle(
    text: String,
    color: Int = getProperStatusBarColor(),
) {
    val colorToUse =
        if (baseConfig.isUsingSystemTheme) {
            getProperTextColor()
        } else {
            color.getContrastColor()
        }

    supportActionBar?.title = Html.fromHtml("<font color='${colorToUse.toHex()}'>$text</font>")
}

fun Activity.hideKeyboard() {
    if (isOnMainThread()) {
        hideKeyboardSync()
    } else {
        Handler(Looper.getMainLooper()).post {
            hideKeyboardSync()
        }
    }
}

fun Activity.hideKeyboardSync() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun BaseSimpleActivity.showFileCreateError(path: String) {
    val error = String.format(getString(R.string.could_not_create_file), path)
    baseConfig.sdTreeUri = ""
    showErrorToast(error)
}

fun BaseSimpleActivity.getFileOutputStreamSync(
    path: String,
    mimeType: String,
    parentDocumentFile: DocumentFile? = null,
): OutputStream? {
    val targetFile = File(path)

    return when {
        isRestrictedSAFOnlyRoot(path) -> {
            val uri = getAndroidSAFUri(path)
            if (!getDoesFilePathExist(path)) {
                createAndroidSAFFile(path)
            }
            applicationContext.contentResolver.openOutputStream(uri)
        }
        needsStupidWritePermissions(path) -> {
            var documentFile =
                parentDocumentFile ?: run {
                    if (getDoesFilePathExist(targetFile.parentFile.absolutePath)) {
                        getDocumentFile(targetFile.parent)
                    } else {
                        getDocumentFile(targetFile.parentFile.parent)
                            ?.createDirectory(targetFile.parentFile.name)
                            ?: getDocumentFile(targetFile.parentFile.absolutePath)
                    }
                }

            if (documentFile == null) {
                val casualOutputStream = createCasualFileOutputStream(this, targetFile)
                return casualOutputStream ?: run {
                    showFileCreateError(targetFile.parent)
                    null
                }
            }

            try {
                val newDocument = getDocumentFile(path) ?: documentFile.createFile(mimeType, path.getFilenameFromPath())
                applicationContext.contentResolver.openOutputStream(newDocument!!.uri)
            } catch (e: FileNotFoundException) {
                showErrorToast("File not found: ${e.message}")
                null
            } catch (e: IOException) {
                showErrorToast("I/O error: ${e.message}")
                null
            }
        }
        isAccessibleWithSAFSdk30(path) -> {
            try {
                val uri = createDocumentUriUsingFirstParentTreeUri(path)
                if (!getDoesFilePathExist(path)) {
                    createSAFFileSdk30(path)
                }
                applicationContext.contentResolver.openOutputStream(uri)
            } catch (e: FileNotFoundException) {
                showErrorToast("File not found: ${e.message}")
                null
            } catch (e: IOException) {
                showErrorToast("I/O error: ${e.message}")
                null
            } ?: createCasualFileOutputStream(this, targetFile)
        }
        else -> createCasualFileOutputStream(this, targetFile)
    }
}

private fun createCasualFileOutputStream(
    activity: BaseSimpleActivity,
    targetFile: File,
): OutputStream? {
    targetFile.parentFile?.takeIf { !it.exists() }?.mkdirs()

    return try {
        FileOutputStream(targetFile)
    } catch (e: FileNotFoundException) {
        activity.showErrorToast("File not found: ${e.message}")
        null
    } catch (e: IOException) {
        activity.showErrorToast("I/O error: ${e.message}")
        null
    }
}

fun BaseSimpleActivity.createDirectorySync(directory: String): Boolean {
    if (getDoesFilePathExist(directory)) {
        return true
    }

    if (needsStupidWritePermissions(directory)) {
        val documentFile = getDocumentFile(directory.getParentPath()) ?: return false
        val newDir = documentFile.createDirectory(directory.getFilenameFromPath()) ?: getDocumentFile(directory)
        return newDir != null
    }

    if (isRestrictedSAFOnlyRoot(directory)) {
        return createAndroidSAFDirectory(directory)
    }

    if (isAccessibleWithSAFSdk30(directory)) {
        return createSAFDirectorySdk30(directory)
    }

    return File(directory).mkdirs()
}
