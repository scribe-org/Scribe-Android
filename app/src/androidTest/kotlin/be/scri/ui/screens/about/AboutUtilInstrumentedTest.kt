// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.ui.screens.about

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.ui.models.ScribeItem
import com.google.common.truth.Truth.assertThat
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for verifying the behavior of [AboutUtil] functions within an Android context.
 *
 * This class tests that various utility functions used in the About screen:
 * - Trigger appropriate Intents
 * - Handle context properly
 * - Return expected list data
 * - Execute callbacks correctly
 *
 * It uses Espresso Intents, ActivityScenario, and Compose test utilities.
 */
@RunWith(AndroidJUnit4::class)
class AboutUtilInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Initializes Espresso Intents before each test.
     */
    @Before
    fun setup() {
        Intents.init()
    }

    /**
     * Releases Espresso Intents after each test to avoid leaks.
     */
    @After
    fun tearDown() {
        Intents.release()
    }

    /**
     * Verifies that [AboutUtil.onShareScribeClick] does not crash when called
     * and launches a share chooser intent from an Activity context.
     */
    @Test
    fun test_onShareScribeClick_doesNotCrash() {
        Intents
            .intending(hasAction(Intent.ACTION_CHOOSER))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            AboutUtil.onShareScribeClick(activity)
        }

        Intents.intended(hasAction(Intent.ACTION_CHOOSER))
    }

    /**
     * Verifies that [AboutUtil.onRateScribeClick] executes without crashing
     * when invoked from an Activity context.
     */
    @Test
    fun test_onRateScribeClick_doesNotCrash() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            AboutUtil.onRateScribeClick(activity)
        }
    }

    /**
     * Asserts that [AboutUtil.onMailClick] launches an email intent wrapped in a chooser.
     */
    @Test
    fun test_onMailClick_launchesEmailIntent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        AboutUtil.onMailClick(context)

        Intents.intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtraWithKey(Intent.EXTRA_INTENT),
                hasExtra(
                    `is`(Intent.EXTRA_INTENT),
                    allOf(
                        hasAction(Intent.ACTION_SEND),
                        hasType("message/rfc822"),
                    ),
                ),
            ),
        )
    }

    /**
     * Tests [AboutUtil.getCommunityList] returns a valid list and
     * triggers correct callbacks for Share and Wikimedia items.
     */
    @Test
    fun testGetCommunityList() {
        var wikimediaClicked = false
        var shareClicked = false
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var extractedItems: List<ScribeItem> = emptyList()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalContext provides context) {
                val communityList = AboutUtil.getCommunityList(
                    onWikimediaAndScribeClick = { wikimediaClicked = true },
                    onShareScribeClick = { shareClicked = true },
                    context = context,
                )
                extractedItems = communityList.items
            }
        }

        composeTestRule.waitForIdle()
        assertThat(extractedItems).hasSize(4)

        val githubItem = extractedItems[0] as ScribeItem.ExternalLinkItem
        val websiteItem = extractedItems[1] as ScribeItem.ExternalLinkItem
        val shareItem = extractedItems[2] as ScribeItem.ExternalLinkItem
        val wikimediaItem = extractedItems[3] as ScribeItem.ExternalLinkItem

        assertThat(githubItem.leadingIcon).isEqualTo(R.drawable.github_logo)

        assertThat(websiteItem.leadingIcon).isEqualTo(R.drawable.globe)
        assertThat(websiteItem.title).isEqualTo(R.string.i18n_app_about_community_visit_website)

        assertThat(shareItem.leadingIcon).isEqualTo(R.drawable.share_icon)
        assertThat(wikimediaItem.leadingIcon).isEqualTo(R.drawable.wikimedia_logo_black)

        shareItem.onClick()
        wikimediaItem.onClick()

        assertThat(shareClicked).isTrue()
        assertThat(wikimediaClicked).isTrue()
    }

    /**
     * Tests [AboutUtil.getFeedbackAndSupportList] returns a valid list and
     * executes all related click callbacks correctly.
     */
    @Test
    fun testGetFeedbackAndSupportList() {
        println("Testing getFeedbackAndSupportList...")

        var rateClicked = false
        var mailClicked = false
        var resetHintsClicked = false
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var extractedItems: List<ScribeItem> = emptyList()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalContext provides context) {
                val feedbackList =
                    AboutUtil.getFeedbackAndSupportList(
                        onRateScribeClick = { rateClicked = true },
                        onMailClick = { mailClicked = true },
                        onResetHintsClick = { resetHintsClicked = true },
                        context = context,
                    )
                extractedItems = feedbackList.items
            }
        }

        // Wait for composition to settle
        composeTestRule.waitForIdle()

        // Test list is not empty.
        assertThat(extractedItems).isNotEmpty()
        assertThat(extractedItems).hasSize(5)

        // Test each item has required fields.
        extractedItems.forEach { item ->
            assertThat(item).isInstanceOf(ScribeItem.ExternalLinkItem::class.java)
            val linkItem = item as ScribeItem.ExternalLinkItem

            assertThat(linkItem.leadingIcon).isNotNull()
            assertThat(linkItem.title).isNotNull()
            assertThat(linkItem.trailingIcon).isNotNull()
            assertThat(linkItem.onClick).isNotNull()
        }

        // Test specific items.
        val rateItem = extractedItems[0] as ScribeItem.ExternalLinkItem
        assertThat(rateItem.leadingIcon).isEqualTo(R.drawable.star)
        assertThat(rateItem.title).isEqualTo(R.string.i18n_app_about_feedback_rate_scribe)

        val mailItem = extractedItems[2] as ScribeItem.ExternalLinkItem
        assertThat(mailItem.leadingIcon).isEqualTo(R.drawable.mail_icon)
        assertThat(mailItem.title).isEqualTo(R.string.i18n_app_about_feedback_send_email)

        val hintsItem = extractedItems[4] as ScribeItem.ExternalLinkItem
        assertThat(hintsItem.leadingIcon).isEqualTo(R.drawable.light_bulb_icon)
        assertThat(hintsItem.title).isEqualTo(R.string.i18n_app_about_feedback_reset_app_hints)

        // Test onClick callbacks OUTSIDE of setContent
        rateItem.onClick()
        mailItem.onClick()
        hintsItem.onClick()

        // Verify callbacks were triggered.
        assertThat(rateClicked).isTrue()
        assertThat(mailClicked).isTrue()
        assertThat(resetHintsClicked).isTrue()

        println("getFeedbackAndSupportList test passed!")
    }

    /**
     * Tests [AboutUtil.getLegalListItems] returns legal items and triggers
     * callbacks for privacy policy and licenses.
     */
    @Test
    fun testGetLegalListItems() {
        println("Testing getLegalListItems...")

        var privacyPolicyClicked = false
        var thirdPartyLicensesClicked = false
        var extractedItems: List<ScribeItem> = emptyList()

        composeTestRule.setContent {
            val legalList =
                AboutUtil.getLegalListItems(
                    onPrivacyPolicyClick = { privacyPolicyClicked = true },
                    onThirdPartyLicensesClick = { thirdPartyLicensesClicked = true },
                )
            extractedItems = legalList.items
        }

        composeTestRule.waitForIdle()

        // Test list is not empty.
        assertThat(extractedItems).isNotEmpty()
        assertThat(extractedItems).hasSize(2)

        // Test each item has required fields.
        extractedItems.forEach { item ->
            assertThat(item).isInstanceOf(ScribeItem.ExternalLinkItem::class.java)
            val linkItem = item as ScribeItem.ExternalLinkItem

            assertThat(linkItem.leadingIcon).isNotNull()
            assertThat(linkItem.title).isNotNull()
            assertThat(linkItem.trailingIcon).isNotNull()
            assertThat(linkItem.onClick).isNotNull()
        }

        // Test specific items.
        val privacyItem = extractedItems[0] as ScribeItem.ExternalLinkItem
        assertThat(privacyItem.leadingIcon).isEqualTo(R.drawable.shield_lock)
        assertThat(privacyItem.title).isEqualTo(R.string.i18n__global_privacy_policy)
        assertThat(privacyItem.trailingIcon).isEqualTo(R.drawable.right_arrow)

        val licenseItem = extractedItems[1] as ScribeItem.ExternalLinkItem
        assertThat(licenseItem.leadingIcon).isEqualTo(R.drawable.license_icon)
        assertThat(licenseItem.title).isEqualTo(R.string.i18n_app_about_legal_third_party)
        assertThat(licenseItem.trailingIcon).isEqualTo(R.drawable.right_arrow)

        privacyItem.onClick()
        licenseItem.onClick()

        // Verify callbacks were triggered.
        assertThat(privacyPolicyClicked).isTrue()
        assertThat(thirdPartyLicensesClicked).isTrue()

        println("getLegalListItems test passed!")
    }

    /**
     * Asserts that all [ExternalLinks] constants are correctly defined and non-empty.
     */
    @Test
    fun testExternalLinksConstants() {
        println("Testing ExternalLinks constants...")

        // Test that external links are properly defined.
        assertThat(be.scri.ui.screens.about.ExternalLinks.GITHUB_SCRIBE).isNotEmpty()
        assertThat(be.scri.ui.screens.about.ExternalLinks.GITHUB_ISSUES).isNotEmpty()
        assertThat(be.scri.ui.screens.about.ExternalLinks.GITHUB_RELEASES).isNotEmpty()
        assertThat(be.scri.ui.screens.about.ExternalLinks.MATRIX).isNotEmpty()
        assertThat(be.scri.ui.screens.about.ExternalLinks.MASTODON).isNotEmpty()

        println("ExternalLinks constants test passed!")
    }
}
