package be.scri.helpers

import android.content.Context
import be.scri.extensions.clipsDB
import be.scri.models.Clip

class ClipsHelper(
    val context: Context,
) {
    // make sure clips have unique values
    fun insertClip(clip: Clip): Long {
        clip.value = clip.value.trim()
        return if (context.clipsDB.getClipWithValue(clip.value) == null) {
            context.clipsDB.insertOrUpdate(clip)
        } else {
            -1
        }
    }
}
