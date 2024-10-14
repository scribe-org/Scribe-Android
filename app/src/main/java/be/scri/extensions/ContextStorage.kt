package be.scri.extensions

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import be.scri.R
import be.scri.helpers.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import be.scri.helpers.SD_OTG_PATTERN
import be.scri.helpers.SD_OTG_SHORT
import be.scri.helpers.isMarshmallowPlus
import java.io.File
import java.util.Collections
import java.util.regex.Pattern

private const val ANDROID_DATA_DIR = "/Android/data/"
private const val ANDROID_OBB_DIR = "/Android/obb/"
val Context.recycleBinPath: String get() = filesDir.absolutePath

// http://stackoverflow.com/a/40582634/1967672
fun Context.getSDCardPath(): String {
    val directories =
        getStorageDirectories().filter {
            !it.equals(getInternalStoragePath()) &&
                !it.equals(
                    "/storage/emulated/0",
                    true,
                ) &&
                (baseConfig.otgPartition.isEmpty() || !it.endsWith(baseConfig.otgPartition))
        }

    val fullSDpattern = Pattern.compile(SD_OTG_PATTERN)
    var sdCardPath =
        directories.firstOrNull { fullSDpattern.matcher(it).matches() }
            ?: directories.firstOrNull { !physicalPaths.contains(it.toLowerCase()) } ?: ""

    // on some devices no method retrieved any SD card path, so test if its not sdcard1 by any chance. It happened on an Android 5.1
    if (sdCardPath.trimEnd('/').isEmpty()) {
        val file = File("/storage/sdcard1")
        if (file.exists()) {
            return file.absolutePath
        }

        sdCardPath = directories.firstOrNull() ?: ""
    }

    if (sdCardPath.isEmpty()) {
        val sdPattern = Pattern.compile(SD_OTG_SHORT)
        try {
            File("/storage").listFiles()?.forEach {
                if (sdPattern.matcher(it.name).matches()) {
                    sdCardPath = "/storage/${it.name}"
                }
            }
        } catch (e: Exception) {
        }
    }

    val finalPath = sdCardPath.trimEnd('/')
    baseConfig.sdCardPath = finalPath
    return finalPath
}

fun Context.getStorageDirectories(): Array<String> {
    val paths = HashSet<String>()
    val rawExternalStorage = System.getenv("EXTERNAL_STORAGE")
    val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
    val rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
    if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
        if (isMarshmallowPlus()) {
            getExternalFilesDirs(null)
                .filterNotNull()
                .map { it.absolutePath }
                .mapTo(paths) { it.substring(0, it.indexOf("Android/data")) }
        } else {
            if (TextUtils.isEmpty(rawExternalStorage)) {
                paths.addAll(physicalPaths)
            } else {
                paths.add(rawExternalStorage!!)
            }
        }
    } else {
        val path = Environment.getExternalStorageDirectory().absolutePath
        val folders = Pattern.compile("/").split(path)
        val lastFolder = folders[folders.size - 1]
        var isDigit = false
        try {
            Integer.valueOf(lastFolder)
            isDigit = true
        } catch (ignored: NumberFormatException) {
        }

        val rawUserId = if (isDigit) lastFolder else ""
        if (TextUtils.isEmpty(rawUserId)) {
            paths.add(rawEmulatedStorageTarget!!)
        } else {
            paths.add(rawEmulatedStorageTarget + File.separator + rawUserId)
        }
    }

    if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
        val rawSecondaryStorages = rawSecondaryStoragesStr!!.split(File.pathSeparator.toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        Collections.addAll(paths, *rawSecondaryStorages)
    }
    return paths.map { it.trimEnd('/') }.toTypedArray()
}

fun Context.getHumanReadablePath(path: String): String =
    getString(
        when (path) {
            "/" -> R.string.root
            internalStoragePath -> R.string.internal
            otgPath -> R.string.usb
            else -> R.string.sd_card
        },
    )

fun Context.humanizePath(path: String): String {
    val trimmedPath = path.trimEnd('/')
    val basePath = path.getBasePath(this)
    return when (basePath) {
        "/" -> "${getHumanReadablePath(basePath)}$trimmedPath"
        else -> trimmedPath.replaceFirst(basePath, getHumanReadablePath(basePath))
    }
}

fun Context.getInternalStoragePath() =
    if (File("/storage/emulated/0").exists()) "/storage/emulated/0" else Environment.getExternalStorageDirectory().absolutePath.trimEnd('/')

fun Context.isPathOnSD(path: String) = sdCardPath.isNotEmpty() && path.startsWith(sdCardPath)

fun Context.isPathOnOTG(path: String) = otgPath.isNotEmpty() && path.startsWith(otgPath)

fun isAndroidDataDir(path: String): Boolean {
    val resolvedPath = "${path.trimEnd('/')}/"
    return resolvedPath.contains(ANDROID_DATA_DIR)
}

fun Context.storeAndroidTreeUri(
    path: String,
    treeUri: String,
) = when {
    isPathOnOTG(path) -> if (isAndroidDataDir(path)) baseConfig.otgAndroidDataTreeUri = treeUri else baseConfig.otgAndroidObbTreeUri = treeUri
    isPathOnSD(path) -> if (isAndroidDataDir(path)) baseConfig.sdAndroidDataTreeUri = treeUri else baseConfig.otgAndroidObbTreeUri = treeUri
    else -> if (isAndroidDataDir(path)) baseConfig.primaryAndroidDataTreeUri = treeUri else baseConfig.primaryAndroidObbTreeUri = treeUri
}

fun Context.getSAFStorageId(fullPath: String): String =
    if (fullPath.startsWith('/')) {
        when {
            fullPath.startsWith(internalStoragePath) -> "primary"
            else -> fullPath.substringAfter("/storage/", "").substringBefore('/')
        }
    } else {
        fullPath.substringBefore(':', "").substringAfterLast('/')
    }

fun Context.createDocumentUriFromRootTree(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)

    val relativePath =
        when {
            fullPath.startsWith(internalStoragePath) -> fullPath.substring(internalStoragePath.length).trim('/')
            else -> fullPath.substringAfter(storageId).trim('/')
        }

    val treeUri = DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, "$storageId:")
    val documentId = "$storageId:$relativePath"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}

fun Context.createAndroidDataOrObbPath(fullPath: String): String =
    if (isAndroidDataDir(fullPath)) {
        fullPath.getBasePath(this).trimEnd('/').plus(ANDROID_DATA_DIR)
    } else {
        fullPath.getBasePath(this).trimEnd('/').plus(ANDROID_OBB_DIR)
    }

fun Context.createAndroidDataOrObbUri(fullPath: String): Uri {
    val path = createAndroidDataOrObbPath(fullPath)
    return createDocumentUriFromRootTree(path)
}

fun Context.getOTGFastDocumentFile(
    path: String,
    otgPathToUse: String? = null,
): DocumentFile? {
    if (baseConfig.otgTreeUri.isEmpty()) {
        return null
    }

    val otgPath = otgPathToUse ?: baseConfig.otgPath
    if (baseConfig.otgPartition.isEmpty()) {
        baseConfig.otgPartition =
            baseConfig.otgTreeUri
                .removeSuffix("%3A")
                .substringAfterLast('/')
                .trimEnd('/')
        updateOTGPathFromPartition()
    }

    val relativePath = Uri.encode(path.substring(otgPath.length).trim('/'))
    val fullUri = "${baseConfig.otgTreeUri}/document/${baseConfig.otgPartition}%3A$relativePath"
    return DocumentFile.fromSingleUri(this, Uri.parse(fullUri))
}

fun Context.updateOTGPathFromPartition() {
    val otgPath = "/storage/${baseConfig.otgPartition}"
    baseConfig.otgPath =
        if (getOTGFastDocumentFile(otgPath, otgPath)?.exists() == true) {
            "/storage/${baseConfig.otgPartition}"
        } else {
            "/mnt/media_rw/${baseConfig.otgPartition}"
        }
}

// avoid these being set as SD card paths
private val physicalPaths =
    arrayListOf(
        "/storage/sdcard1", // Motorola Xoom
        "/storage/extsdcard", // Samsung SGS3
        "/storage/sdcard0/external_sdcard", // User request
        "/mnt/extsdcard",
        "/mnt/sdcard/external_sd", // Samsung galaxy family
        "/mnt/external_sd",
        "/mnt/media_rw/sdcard1", // 4.4.2 on CyanogenMod S3
        "/removable/microsd", // Asus transformer prime
        "/mnt/emmc",
        "/storage/external_SD", // LG
        "/storage/ext_sd", // HTC One Max
        "/storage/removable/sdcard1", // Sony Xperia Z1
        "/data/sdext",
        "/data/sdext2",
        "/data/sdext3",
        "/data/sdext4",
        "/sdcard1", // Sony Xperia Z
        "/sdcard2", // HTC One M8s
        "/storage/usbdisk0",
        "/storage/usbdisk1",
        "/storage/usbdisk2",
    )
