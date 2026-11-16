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
 *  - [R.id.keyboard_view] must not anchor its bottom edge to [R.id.floating_drag_bar],
 *    because that view anchors its own top edge back to [R.id.keyboard_view]. The
 *    resulting cycle makes the `wrap_content` root resolve to the full window height
 *    and `onComputeInsets` report a full-screen IME inset to the host app.
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
    fun keyboardView_bottomAnchorIsParent_floatingDragBarCycleIsBroken() {
        val params = keyboardView.layoutParams as ConstraintLayout.LayoutParams

        // Before the fix the bottom edge pointed at floating_drag_bar, whose own top
        // edge pointed back at keyboard_view - a cycle that stretched the wrap_content
        // root to the full IME window height.
        assertEquals(ConstraintLayout.LayoutParams.PARENT_ID, params.bottomToBottom)
        assertEquals(ConstraintLayout.LayoutParams.UNSET, params.bottomToTop)
    }
}
