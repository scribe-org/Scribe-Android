// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.english.ENInterfaceVariables
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the IME layout against the regressions described in issue #770 (follow-up to #759):
 *
 *  - [R.id.empty_state_banner_container] lives inside a vertical `LinearLayout`, where
 *    `layout_width="0dp"` means a literal zero width. Its `wrap_content` text then wraps
 *    one character per line and inflates the whole IME window far beyond the keyboard.
 *
 *  - [R.id.keyboard_view] must not anchor its bottom edge to [R.id.floating_drag_bar]
 *    or to the parent. The drag bar anchors its top edge to [R.id.keyboard_view], so a
 *    reverse anchor is a cycle that stretches the `wrap_content` root to the full
 *    window height, and pinning the keyboard to the parent bottom leaves the drag bar
 *    no room when floating mode makes it visible.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InputMethodViewLayoutTest {
    private lateinit var context: Context
    private lateinit var binding: InputMethodViewBinding

    private val banner get() = binding.emptyStateBanner
    private val bannerContainer get() = binding.emptyStateBannerContainer
    private val keyboardView get() = binding.keyboardView

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        binding = InputMethodViewBinding.inflate(LayoutInflater.from(context))
    }

    @Test
    fun emptyStateBannerContainer_widthIsMatchParent_notZero() {
        // The container sits inside `top_container`, a vertical LinearLayout without
        // weights, so 0dp is zero width rather than "fill". See issue #770.
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, bannerContainer.layoutParams.width)
    }

    @Test
    fun emptyStateBanner_textWrapsNormally_whenVisibleWithoutLanguageData() {
        bannerContainer.visibility = View.VISIBLE
        banner.text = ENInterfaceVariables.DOWNLOAD_DATA_LBL

        val displayMetrics = context.resources.displayMetrics
        binding.root.measure(
            View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.AT_MOST),
        )
        binding.root.layout(0, 0, binding.root.measuredWidth, binding.root.measuredHeight)

        // With the old 0dp width the banner measured zero wide and wrapped one
        // character per line, so a 30 character prompt produced ~30 lines and
        // blew the IME window up past the screen height.
        assertTrue("banner container must have a real width", bannerContainer.width > 0)
        assertTrue("banner text wrapped to ${banner.lineCount} lines", banner.lineCount in 1..3)
        assertTrue(
            "IME root expanded to ${binding.root.height}px, expected it to hug its content",
            binding.root.height < displayMetrics.heightPixels / 2,
        )
    }

    @Test
    fun keyboardView_hasNoBottomAnchor_dragBarHangsBelowItInOneDirection() {
        val keyboardParams = keyboardView.layoutParams as ConstraintLayout.LayoutParams
        val dragParams = binding.floatingDragBar.layoutParams as ConstraintLayout.LayoutParams

        // A bottom anchor on the keyboard, whether to the drag bar or to the parent,
        // either recreates the cycle or leaves the drag bar no slot in floating mode.
        assertEquals(ConstraintLayout.LayoutParams.UNSET, keyboardParams.bottomToBottom)
        assertEquals(ConstraintLayout.LayoutParams.UNSET, keyboardParams.bottomToTop)

        assertEquals(keyboardView.id, dragParams.topToBottom)
        assertEquals(ConstraintLayout.LayoutParams.UNSET, dragParams.bottomToBottom)
        assertEquals(ConstraintLayout.LayoutParams.UNSET, dragParams.bottomToTop)
    }

    @Test
    fun floatingDragBar_sitsBelowKeyboard_withoutStretchingTheCard() {
        binding.floatingDragBar.visibility = View.VISIBLE

        val displayMetrics = context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        binding.root.measure(
            View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(screenHeight, View.MeasureSpec.EXACTLY),
        )
        binding.root.layout(0, 0, binding.root.measuredWidth, binding.root.measuredHeight)

        val card = binding.keyboardCard
        val dragBar = binding.floatingDragBar

        assertTrue(
            "keyboard card stretched to ${card.height}px against a ${screenHeight}px window",
            card.height in 1 until screenHeight / 2,
        )
        assertTrue("drag bar must keep its own height", dragBar.height > 0)
        assertEquals(
            "drag bar must start where the keyboard ends, not overlap it",
            keyboardView.bottom,
            dragBar.top,
        )
    }
}
