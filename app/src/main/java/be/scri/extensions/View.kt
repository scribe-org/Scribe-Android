// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.extensions

import android.content.res.AssetFileDescriptor
import android.media.SoundPool
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import be.scri.R

/**
 * Sets the view's visibility to VISIBLE if [beVisible] is true; otherwise, sets it to GONE.
 *
 * @receiver View to update visibility
 * @param beVisible Boolean flag indicating whether the view should be visible
 */
fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

/**
 * Sets the view's visibility to GONE if [beGone] is true; otherwise, sets it to VISIBLE.
 *
 * @receiver View to update visibility
 * @param beGone Boolean flag indicating whether the view should be gone
 */
fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

/**
 * Sets the view's visibility to VISIBLE.
 *
 * @receiver View to make visible
 */
fun View.beVisible() {
    visibility = View.VISIBLE
}

/**
 * Sets the view's visibility to GONE.
 *
 * @receiver View to make gone
 */
fun View.beGone() {
    visibility = View.GONE
}

/**
 * Performs haptic feedback using the VIRTUAL_KEY feedback type.
 *
 * @receiver View on which to perform haptic feedback
 */
fun View.performHapticFeedback() = performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)


private var soundPool: SoundPool? = null
private var soundId: Int = 0
private var soundLoaded = false
private var initialized = false

fun View.performSoundFeedback() {
    val context = this.context

    if (!initialized) {
        soundPool = SoundPool.Builder().setMaxStreams(1).build().also { pool ->
            try {
                val afd: AssetFileDescriptor = context.assets.openFd("media/click.wav")
                soundId = pool.load(afd, 1)
                pool.setOnLoadCompleteListener { _, _, status ->
                    soundLoaded = (status == 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        initialized = true
    }

    if (soundLoaded) {
        soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
    }
}
