package be.scri.models

import android.content.Context
import android.net.Uri
import be.scri.extensions.formatDate
import be.scri.extensions.formatSize
import be.scri.extensions.getAlbum
import be.scri.extensions.getAndroidSAFDirectChildrenCount
import be.scri.extensions.getAndroidSAFFileCount
import be.scri.extensions.getAndroidSAFFileSize
import be.scri.extensions.getAndroidSAFLastModified
import be.scri.extensions.getArtist
import be.scri.extensions.getDirectChildrenCount
import be.scri.extensions.getDocumentFile
import be.scri.extensions.getDuration
import be.scri.extensions.getFastDocumentFile
import be.scri.extensions.getFileCount
import be.scri.extensions.getFormattedDuration
import be.scri.extensions.getImageResolution
import be.scri.extensions.getItemSize
import be.scri.extensions.getMediaStoreLastModified
import be.scri.extensions.getParentPath
import be.scri.extensions.getProperSize
import be.scri.extensions.getResolution
import be.scri.extensions.getSizeFromContentUri
import be.scri.extensions.getTitle
import be.scri.extensions.getVideoResolution
import be.scri.extensions.isPathOnOTG
import be.scri.extensions.isRestrictedSAFOnlyRoot
import be.scri.extensions.normalizeString
import be.scri.helpers.AlphanumericComparator
import be.scri.helpers.SORT_BY_DATE_MODIFIED
import be.scri.helpers.SORT_BY_EXTENSION
import be.scri.helpers.SORT_BY_NAME
import be.scri.helpers.SORT_BY_SIZE
import be.scri.helpers.SORT_DESCENDING
import be.scri.helpers.SORT_USE_NUMERIC_VALUE
import be.scri.helpers.isNougatPlus
import com.bumptech.glide.signature.ObjectKey
import java.io.File

open class FileDirItem(
    val path: String,
    val name: String = "",
    var isDirectory: Boolean = false,
    var children: Int = 0,
    var size: Long = 0L,
    var modified: Long = 0L,
) : Comparable<FileDirItem> {
    companion object {
        var sorting = 0
    }

    override fun toString() = "FileDirItem(path=$path, name=$name, isDirectory=$isDirectory, children=$children, size=$size, modified=$modified)"

    override fun compareTo(other: FileDirItem): Int =
        if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else {
            var result: Int
            when {
                sorting and SORT_BY_NAME != 0 -> {
                    result =
                        if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                            AlphanumericComparator().compare(name.normalizeString().toLowerCase(), other.name.normalizeString().toLowerCase())
                        } else {
                            name.normalizeString().toLowerCase().compareTo(other.name.normalizeString().toLowerCase())
                        }
                }
                sorting and SORT_BY_SIZE != 0 ->
                    result =
                        when {
                            size == other.size -> 0
                            size > other.size -> 1
                            else -> -1
                        }
                sorting and SORT_BY_DATE_MODIFIED != 0 -> {
                    result =
                        when {
                            modified == other.modified -> 0
                            modified > other.modified -> 1
                            else -> -1
                        }
                }
                else -> {
                    result = getExtension().toLowerCase().compareTo(other.getExtension().toLowerCase())
                }
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }

    fun getExtension() = if (isDirectory) name else path.substringAfterLast('.', "")

    fun getBubbleText(
        context: Context,
        dateFormat: String? = null,
        timeFormat: String? = null,
    ) = when {
        sorting and SORT_BY_SIZE != 0 -> size.formatSize()
        sorting and SORT_BY_DATE_MODIFIED != 0 -> modified.formatDate(context, dateFormat, timeFormat)
        sorting and SORT_BY_EXTENSION != 0 -> getExtension().toLowerCase()
        else -> name
    }

    fun getProperSize(
        context: Context,
        countHidden: Boolean,
    ): Long =
        when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFFileSize(path)
            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.getItemSize(countHidden) ?: 0
            isNougatPlus() && path.startsWith("content://") -> {
                try {
                    context.contentResolver
                        .openInputStream(Uri.parse(path))
                        ?.available()
                        ?.toLong() ?: 0L
                } catch (e: Exception) {
                    context.getSizeFromContentUri(Uri.parse(path))
                }
            }
            else -> File(path).getProperSize(countHidden)
        }

    fun getProperFileCount(
        context: Context,
        countHidden: Boolean,
    ): Int =
        when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFFileCount(path, countHidden)
            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.getFileCount(countHidden) ?: 0
            else -> File(path).getFileCount(countHidden)
        }

    fun getDirectChildrenCount(
        context: Context,
        countHiddenItems: Boolean,
    ): Int =
        when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFDirectChildrenCount(path, countHiddenItems)
            context.isPathOnOTG(path) ->
                context
                    .getDocumentFile(path)
                    ?.listFiles()
                    ?.filter { if (countHiddenItems) true else !it.name!!.startsWith(".") }
                    ?.size
                    ?: 0
            else -> File(path).getDirectChildrenCount(context, countHiddenItems)
        }

    fun getLastModified(context: Context): Long =
        when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFLastModified(path)
            context.isPathOnOTG(path) -> context.getFastDocumentFile(path)?.lastModified() ?: 0L
            isNougatPlus() && path.startsWith("content://") -> context.getMediaStoreLastModified(path)
            else -> File(path).lastModified()
        }

    fun getParentPath() = path.getParentPath()

    fun getDuration(context: Context) = context.getDuration(path)?.getFormattedDuration()

    fun getFileDurationSeconds(context: Context) = context.getDuration(path)

    fun getArtist(context: Context) = context.getArtist(path)

    fun getAlbum(context: Context) = context.getAlbum(path)

    fun getTitle(context: Context) = context.getTitle(path)

    fun getResolution(context: Context) = context.getResolution(path)

    fun getVideoResolution(context: Context) = context.getVideoResolution(path)

    fun getImageResolution(context: Context) = context.getImageResolution(path)

    fun getPublicUri(context: Context) = context.getDocumentFile(path)?.uri ?: ""

    fun getSignature(): String {
        val lastModified =
            if (modified > 1) {
                modified
            } else {
                File(path).lastModified()
            }

        return "$path-$lastModified-$size"
    }

    fun getKey() = ObjectKey(getSignature())
}
