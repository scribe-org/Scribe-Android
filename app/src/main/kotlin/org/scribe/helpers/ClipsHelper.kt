package org.scribe.helpers

import android.content.Context
import org.scribe.extensions.clipsDB
import org.scribe.models.Clip

class ClipsHelper(val context: Context) {

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
