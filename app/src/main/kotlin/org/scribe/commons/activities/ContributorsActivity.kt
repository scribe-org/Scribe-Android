package org.scribe.commons.activities

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Menu
import kotlinx.android.synthetic.main.activity_contributors.*
import org.scribe.R
import org.scribe.commons.extensions.*
import org.scribe.commons.helpers.APP_ICON_IDS
import org.scribe.commons.helpers.APP_LAUNCHER_NAME

class ContributorsActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contributors)

        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        val primaryColor = getProperPrimaryColor()

        updateTextColors(contributors_holder)
        contributors_development_label.setTextColor(primaryColor)
        contributors_translation_label.setTextColor(primaryColor)

        contributors_label.apply {
            setTextColor(textColor)
            text = Html.fromHtml(getString(R.string.contributors_label))
            setLinkTextColor(primaryColor)
            movementMethod = LinkMovementMethod.getInstance()
            removeUnderlines()
        }

        contributors_development_icon.applyColorFilter(textColor)
        contributors_footer_icon.applyColorFilter(textColor)

        arrayOf(contributors_development_holder, contributors_translation_holder).forEach {
            it.background.applyColorFilter(backgroundColor.getContrastColor())
        }

        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            contributors_footer_icon.beGone()
            contributors_label.beGone()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }
}
