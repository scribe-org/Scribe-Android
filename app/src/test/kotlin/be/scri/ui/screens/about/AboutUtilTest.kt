// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.screens.about

// Assuming these are accessible — adjust package path if needed
import android.content.Context
import android.content.Intent
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.HintUtils
import be.scri.helpers.RatingHelper
import be.scri.helpers.ShareHelper
import be.scri.helpers.ShareHelperInterface
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AboutUtilTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
    }

    @Test
    fun getCommunityList_callsOnShareScribeClick_whenShareItemClicked() {
        val mockContext = mockk<Context>(relaxed = true)
        var called = false

        val list =
            buildCommunityList(
                context = mockContext,
                onShareScribeClick = { called = true },
                onWikimediaAndScribeClick = {},
            )

        val shareItem = list[3] // Index of share scribe
        shareItem.onClick()
        assertTrue(called)
    }

    @Test
    fun testGetFeedbackAndSupportList_correctItemGeneration() {
        // Create a mocked Context, relaxed = true to avoid extra stubs for unused methods
        val mockContext = mockk<Context>(relaxed = true)

        // Mock startActivity to just record calls (optional)
        every { mockContext.startActivity(any()) }

        // Flags to check if callbacks were called
        var rateClicked = false
        var mailClicked = false
        var resetHintsClicked = false

        val list =
            feedbackAndSupportList(
                context = mockContext,
                onRateScribeClick = { rateClicked = true },
                onMailClick = { mailClicked = true },
                onResetHintsClick = { resetHintsClicked = true },
            )

        // Assert the list size is exactly 5 items (as expected)
        assertEquals(5, list.size)

        // Check the titles correspond to the expected resource IDs
        assertEquals(R.string.app_about_feedback_rate_scribe, list[0].title)
        assertEquals(R.string.app_about_feedback_bug_report, list[1].title)
        assertEquals(R.string.app_about_feedback_email, list[2].title)
        assertEquals(R.string.app_about_feedback_version, list[3].title)
        assertEquals(R.string.app_about_feedback_app_hints, list[4].title)

        // Check the onClick callbacks call the correct lambdas or startActivity

        // Item 0: Rate Scribe calls rateClicked
        list[0].onClick()
        assertTrue(rateClicked)

        // Item 2: Mail click calls mailClicked
        list[2].onClick()
        assertTrue(mailClicked)

        // Item 4: Reset hints calls resetHintsClicked
        list[4].onClick()
        assertTrue(resetHintsClicked)

        // Items with URLs should startActivity called - verify startActivity called twice for index
        // 1 and 3
        list[1].onClick()
        list[3].onClick()

        verify(exactly = 2) { mockContext.startActivity(any<Intent>()) }
    }

    @Test
    fun `getLegalItemSpecs returns expected list`() {
        val specs = getLegalItemSpecs()

        assertEquals(2, specs.size)
        assertEquals(R.drawable.shield_lock, specs[0].icon)
        assertEquals(R.string.app_about_legal_privacy_policy, specs[0].title)
        assertEquals(Destination.PrivacyPolicy, specs[0].destination)

        assertEquals(R.drawable.license_icon, specs[1].icon)
        assertEquals(R.string.app_about_legal_third_party, specs[1].title)
        assertEquals(Destination.ThirdPartyLicenses, specs[1].destination)
    }

    @Test
    fun testOnShareScribeClick() {
        // Arrange
        val mockHelper = mockk<ShareHelperInterface>(relaxed = true)
        AboutUtil.shareHelper = mockHelper

        // Act
        AboutUtil.onShareScribeClick(context)

        // Assert
        verify { mockHelper.shareScribe(context) }
    }

    @Test
    fun testOnRateScribeClick() {
        // Arrange
        val mockActivity = mockk<MainActivity>(relaxed = true)

        // Act
        AboutUtil.onRateScribeClick(mockActivity)

        // Assert
        verify { RatingHelper.rateScribe(mockActivity, mockActivity) }
    }

    @Test
    fun testOnMailClick() {
        // Arrange
        mockkObject(ShareHelper)

        // Act
        AboutUtil.onMailClick(context)

        // Assert
        verify { ShareHelper.sendEmail(context) }
    }

    @Test
    fun `onResetHintsClick calls HintUtils resetHints`() {
        val mockContext = mockk<Context>(relaxed = true)
        mockkObject(HintUtils)

        every { HintUtils.resetHints(mockContext) } returns Unit

        var called = false
        val list =
            feedbackAndSupportList(
                context = mockContext,
                onRateScribeClick = {},
                onMailClick = {},
                onResetHintsClick = {
                    called = true
                    HintUtils.resetHints(mockContext)
                },
            )

        list[4].onClick()

        verify(exactly = 1) { HintUtils.resetHints(mockContext) }
        assertTrue(called)
    }
}
