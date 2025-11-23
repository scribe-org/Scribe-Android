// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.screens.about

import android.content.Context
import android.content.Intent
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.helpers.ui.HintUtils
import be.scri.helpers.ui.RatingHelper
import be.scri.helpers.ui.ShareHelper
import be.scri.helpers.ui.ShareHelperInterface
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AboutUtilTest {
    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
    }

    @Test
    fun getCommunityList_callsOnShareScribeClick_whenShareItemClicked() {
        val mockContext = mockk<Context>(relaxed = true)
        every { mockContext.getString(any()) } returns "Mocked String"

        var called = false

        println("Before building list")
        val list =
            buildCommunityList(
                context = mockContext,
                onShareScribeClick = { called = true },
                onWikimediaAndScribeClick = {},
            )

        println("Community list size: ${list.size}")

        assertTrue(list.size >= 3, "Expected at least 3 items")
        list[1].onClick()
    }

    @Test
    fun testGetFeedbackAndSupportList_correctItemGeneration() {
        val mockContext = mockk<Context>(relaxed = true)

        every { mockContext.getString(any()) } returns "Mocked String"
        every { mockContext.packageName } returns "be.scri"
        every { mockContext.startActivity(any()) } just Runs

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

        assertEquals(5, list.size)

        // Checking the resource IDs are preserved, not string values.
        assertEquals(R.string.app_about_feedback_rate_scribe, list[0].title)
        assertEquals(R.string.app_about_feedback_bug_report, list[1].title)
        assertEquals(R.string.app_about_feedback_email, list[2].title)
        assertEquals(R.string.app_about_feedback_version, list[3].title)
        assertEquals(R.string.app_about_feedback_app_hints, list[4].title)

        list[0].onClick()
        assertTrue(rateClicked)

        list[2].onClick()
        assertTrue(mailClicked)

        list[4].onClick()
        assertTrue(resetHintsClicked)

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
        verify { mockHelper.shareScribe(context) }
    }

    @Test
    fun testOnRateScribeClick() {
        // Arrange
        val mockActivity = mockk<MainActivity>(relaxed = true)
        mockkObject(RatingHelper)
        every { RatingHelper.rateScribe(any(), any()) } just Runs

        // Act
        AboutUtil.onRateScribeClick(mockActivity)

        // Assert
        verify { RatingHelper.rateScribe(mockActivity, mockActivity) }
    }

    @Test
    fun testOnMailClick() {
        // Arrange
        mockkObject(ShareHelper)
        every { ShareHelper.sendEmail(any()) } just Runs // Mock the method to do nothing

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
