package be.scri.extensions

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import be.scri.helpers.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import be.scri.helpers.isRPlus
import be.scri.helpers.isSPlus
import java.io.File

private const val DOWNLOAD_DIR = "Download"
private const val ANDROID_DIR = "Android"

fun Context.getFirstParentLevel(path: String): Int =
    when {
        isSPlus() && (isInAndroidDir(path) || isInSubFolderInDownloadDir(path)) -> 1
        isRPlus() && isInSubFolderInDownloadDir(path) -> 1
        else -> 0
    }

fun Context.isInSubFolderInDownloadDir(path: String): Boolean {
    if (path.startsWith(recycleBinPath)) {
        return false
    }
    val firstParentDir = path.getFirstParentDirName(this, 1)
    return if (firstParentDir == null) {
        false
    } else {
        val startsWithDownloadDir = firstParentDir.startsWith(DOWNLOAD_DIR, true)
        val hasAtLeast1PathSegment = firstParentDir.split("/").filter { it.isNotEmpty() }.size > 1
        val firstParentPath = path.getFirstParentPath(this, 1)
        startsWithDownloadDir && hasAtLeast1PathSegment && File(firstParentPath).isDirectory
    }
}

fun Context.isInAndroidDir(path: String): Boolean {
    if (path.startsWith(recycleBinPath)) {
        return false
    }
    val firstParentDir = path.getFirstParentDirName(this, 0)
    return firstParentDir.equals(ANDROID_DIR, true)
}

fun Context.createFirstParentTreeUri(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)
    val level = getFirstParentLevel(fullPath)
    val rootParentDirName = fullPath.getFirstParentDirName(this, level)
    val firstParentId = "$storageId:$rootParentDirName"
    return DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, firstParentId)
}

fun Context.buildDocumentUriSdk30(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)

    val relativePath =
        when {
            fullPath.startsWith(internalStoragePath) -> fullPath.substring(internalStoragePath.length).trim('/')
            else -> fullPath.substringAfter(storageId).trim('/')
        }

    val documentId = "$storageId:$relativePath"
    return DocumentsContract.buildDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, documentId)
}
