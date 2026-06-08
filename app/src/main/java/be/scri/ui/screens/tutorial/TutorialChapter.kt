// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.tutorial

/**
 * Represents a single tutorial chapter in the home screen.
 *
 * @property title The display name of the chapter.
 * @property chapterIndex The index used to navigate to this chapter.
 */
data class TutorialChapter(
    val title: String,
    val chapterIndex: Int,
)
