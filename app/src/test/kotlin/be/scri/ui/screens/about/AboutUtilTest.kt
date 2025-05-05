import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.scri.ui.models.ScribeItem
import be.scri.ui.models.ScribeItemList
import be.scri.ui.screens.about.AboutUtil
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class AboutUtilTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testGetLegalListItemsTriggersCallbacks() {
        var privacyClicked = false
        var licensesClicked = false

        lateinit var list: ScribeItemList

        composeTestRule.setContent {
            list =
                AboutUtil.getLegalListItems(
                    onPrivacyPolicyClick = { privacyClicked = true },
                    onThirdPartyLicensesClick = { licensesClicked = true },
                )
        }

        // Simulate clicks
        val privacyItem = list.items[0] as ScribeItem.ExternalLinkItem
        val licenseItem = list.items[1] as ScribeItem.ExternalLinkItem

        privacyItem.onClick()
        licenseItem.onClick()

        assertTrue(privacyClicked)
        assertTrue(licensesClicked)
    }

    @Test
    fun testGetCommunityListTriggersCallbacks() {
        var shareClicked = false
        var wikiClicked = false

        lateinit var list: ScribeItemList
        val mockContext = mock<Context>()

        composeTestRule.setContent {
            list =
                AboutUtil.getCommunityList(
                    onWikimediaAndScribeClick = { wikiClicked = true },
                    onShareScribeClick = { shareClicked = true },
                    context = mockContext,
                )
        }

        // Last two are share and Wikimedia
        (list.items[0] as ScribeItem.ExternalLinkItem).onClick()
        (list.items[1] as ScribeItem.ExternalLinkItem).onClick()
        (list.items[2] as ScribeItem.ExternalLinkItem).onClick()
        (list.items[3] as ScribeItem.ExternalLinkItem).onClick()
        (list.items[4] as ScribeItem.ExternalLinkItem).onClick()

        assertTrue(shareClicked)
        assertTrue(wikiClicked)

        // Verify that the URLs for GitHub, Matrix, and Mastodon are correctly opened
        verify(mockContext).startActivity(
            check {
                assertEquals(Intent.ACTION_VIEW, it.action)
                assertEquals(Uri.parse("https://github.com/scribe-org/Scribe-Android"), it.data)
            },
        )

        verify(mockContext).startActivity(
            check {
                assertEquals(Intent.ACTION_VIEW, it.action)
                assertEquals(Uri.parse("https://matrix.to/%23/%23scribe_community:matrix.org"), it.data)
            },
        )

        verify(mockContext).startActivity(
            check {
                assertEquals(Intent.ACTION_VIEW, it.action)
                assertEquals(Uri.parse("https://wikis.world/@scribe"), it.data)
            },
        )
    }

    @Test
    fun testGetFeedbackAndSupportListTriggersCallbacks() {
        var rateClicked = false
        var mailClicked = false
        var resetClicked = false

        lateinit var list: ScribeItemList
        val mockContext = mock<Context>()
        val mockPackageManager = mock<PackageManager>()

        // Stub context methods
        whenever(mockContext.packageManager).thenReturn(mockPackageManager)

        composeTestRule.setContent {
            list =
                AboutUtil.getFeedbackAndSupportList(
                    onRateScribeClick = { rateClicked = true },
                    onMailClick = { mailClicked = true },
                    onResetHintsClick = { resetClicked = true },
                    context = mockContext,
                )
        }

        // Check correct item clicks
        (list.items[0] as ScribeItem.ExternalLinkItem).onClick() // rate
        (list.items[1] as ScribeItem.ExternalLinkItem).onClick() // report Bug
        (list.items[2] as ScribeItem.ExternalLinkItem).onClick() // mail
        (list.items[3] as ScribeItem.ExternalLinkItem).onClick() // version
        (list.items[4] as ScribeItem.ExternalLinkItem).onClick() // reset

        assertTrue(rateClicked)
        assertTrue(mailClicked)
        assertTrue(resetClicked)

        // Verify Report Bug opens the correct URL
        verify(mockContext).startActivity(
            check {
                assertEquals(Intent.ACTION_VIEW, it.action)
                assertEquals(Uri.parse("https://github.com/scribe-org/Scribe-Android/issues"), it.data)
            },
        )

        // Verify Version opens the correct URL
        verify(mockContext).startActivity(
            check {
                assertEquals(Intent.ACTION_VIEW, it.action)
                assertEquals(Uri.parse("https://github.com/scribe-org/Scribe-Android/releases/"), it.data)
            },
        )
    }

    @Test
    fun testOnShareScribeClick() {
        val mockContext = mock(Context::class.java)

        // Call the function under test
        AboutUtil.onShareScribeClick(mockContext)

        // Capture arguments
        val intentCaptor = argumentCaptor<Intent>()
        val bundleCaptor = argumentCaptor<Bundle>()

        verify(mockContext).startActivity(intentCaptor.capture(), bundleCaptor.capture())

        val chooserIntent = intentCaptor.firstValue

        // Check that it's a chooser
        assertEquals(Intent.ACTION_CHOOSER, chooserIntent.action)

        // Extract the wrapped share intent
        val actualShareIntent = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

        // Validate share intent
        assertEquals(Intent.ACTION_SEND, actualShareIntent?.action)
        assertEquals("text/plain", actualShareIntent?.type)
        assertEquals(
            "https://github.com/scribe-org/Scribe-Android",
            actualShareIntent?.getStringExtra(Intent.EXTRA_TEXT),
        )

        // Optional: Validate chooser title
        assertEquals(
            "Share via",
            chooserIntent.getStringExtra(Intent.EXTRA_TITLE),
        ) // Replace with actual title if needed

        // Bundle (2nd arg to startActivity) is expected to be null
        assertNull(bundleCaptor.firstValue)
    }

    @Test
    fun testOnMailClick_sendsEmailIntent() {
        val mockContext = mock<Context>()

        // Stub startActivity to avoid crash
        doNothing().whenever(mockContext).startActivity(any(), anyOrNull())

        // Call the function
        AboutUtil.onMailClick(mockContext)

        // Capture intent passed to startActivity
        argumentCaptor<Intent>().apply {
            verify(mockContext).startActivity(capture(), eq(null))
            val chooserIntent = firstValue

            // Extract the actual email intent from the chooser
            val actualEmailIntent = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
            assertNotNull(actualEmailIntent)
            assertEquals(Intent.ACTION_SEND, actualEmailIntent?.action)
            assertEquals("message/rfc822", actualEmailIntent?.type)
            assertArrayEquals(arrayOf("team@scri.be"), actualEmailIntent?.getStringArrayExtra(Intent.EXTRA_EMAIL))
            assertEquals("Hey Scribe!", actualEmailIntent?.getStringExtra(Intent.EXTRA_SUBJECT))
        }
    }
}
