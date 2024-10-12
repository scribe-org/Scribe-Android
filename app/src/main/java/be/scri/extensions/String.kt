package be.scri.extensions

import android.content.Context

fun String.getBasePath(context: Context): String =
    when {
        startsWith(context.internalStoragePath) -> context.internalStoragePath
        context.isPathOnSD(this) -> context.sdCardPath
        context.isPathOnOTG(this) -> context.otgPath
        else -> "/"
    }

fun String.getFirstParentDirName(
    context: Context,
    level: Int,
): String? {
    val basePath = getBasePath(context)
    val startIndex = basePath.length + 1
    return if (length > startIndex) {
        val pathWithoutBasePath = substring(startIndex)
        val pathSegments = pathWithoutBasePath.split("/")
        if (level < pathSegments.size) {
            pathSegments.slice(0..level).joinToString("/")
        } else {
            null
        }
    } else {
        null
    }
}

fun String.getFirstParentPath(
    context: Context,
    level: Int,
): String {
    val basePath = getBasePath(context)
    val startIndex = basePath.length + 1
    return if (length > startIndex) {
        val pathWithoutBasePath = substring(basePath.length + 1)
        val pathSegments = pathWithoutBasePath.split("/")
        val firstParentPath =
            if (level < pathSegments.size) {
                pathSegments.slice(0..level).joinToString("/")
            } else {
                pathWithoutBasePath
            }
        "$basePath/$firstParentPath"
    } else {
        basePath
    }
}
