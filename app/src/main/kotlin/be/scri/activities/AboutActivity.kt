package be.scri.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.Menu
import android.view.MotionEvent
import androidx.core.net.toUri
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_about.*
import be.scri.R
import be.scri.dialogs.ConfirmationAdvancedDialog
import be.scri.extensions.*
import be.scri.helpers.*
import kotlin.math.abs

class AboutActivity : BaseSimpleActivity(), GestureDetector.OnGestureListener{
    private var appName = ""
    private var primaryColor = 0

    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0
    private val EASTER_EGG_TIME_LIMIT = 3000L
    private val EASTER_EGG_REQUIRED_CLICKS = 7

    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        gestureDetector = GestureDetector(this)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        primaryColor = getProperPrimaryColor()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.info

        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.about ->  {
                    return@OnNavigationItemSelectedListener true }
                R.id.installation -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
        bottomNavigationView.selectedItemId = R.id.about

        arrayOf(
            about_invite_icon,
            about_email_icon
        ).forEach {
            it.applyColorFilter(textColor)
        }

        arrayOf(about_support, about_help_us).forEach {
            it.setTextColor(textColor)
        }

        arrayOf(about_support_holder, about_help_us_holder).forEach {
            it.background.applyColorFilter(backgroundColor.getContrastColor())
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        }
        else {
            super.onTouchEvent(event)
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(about_scrollview)

        setupEmail()
        setupInvite()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupEmail() {
        about_email_holder.background = resources.getDrawable(R.drawable.ripple_all_corners, theme)

        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            about_email_holder.beGone()
        }

        about_email_holder.setOnClickListener {
            val msg = "${getString(R.string.before_asking_question_read_faq)}\n\n${getString(R.string.make_sure_latest)}"
            if (intent.getBooleanExtra(SHOW_FAQ_BEFORE_MAIL, false) && !baseConfig.wasBeforeAskingShown) {
                baseConfig.wasBeforeAskingShown = true
                ConfirmationAdvancedDialog(this, msg, 0, R.string.read_faq, R.string.skip) { success ->
                    about_email_holder.performClick()
                }
            } else {
                val appVersion = String.format(getString(R.string.app_version, intent.getStringExtra(APP_VERSION_NAME)))
                val deviceOS = String.format(getString(R.string.device_os), Build.VERSION.RELEASE)
                val newline = "\n"
                val separator = "------------------------------"
                val body = "$appVersion$newline$deviceOS$newline$separator$newline$newline"

                val address = getString(R.string.my_email)
                val selectorIntent = Intent(ACTION_SENDTO)
                    .setData("mailto:$address".toUri())
                val emailIntent = Intent(ACTION_SEND).apply {
                    putExtra(EXTRA_EMAIL, arrayOf(address))
                    putExtra(EXTRA_SUBJECT, appName)
                    putExtra(EXTRA_TEXT, body)
                    selector = selectorIntent
                }

                try {
                    startActivity(emailIntent)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.no_app_found)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }

    private fun setupInvite() {
        if (resources.getBoolean(R.bool.hide_google_relations)) {
            about_invite_holder.beGone()
        }

        about_invite_holder.setOnClickListener {
            val text = String.format(getString(R.string.share_text), appName, getStoreUrl())
            Intent().apply {
                action = ACTION_SEND
                putExtra(EXTRA_SUBJECT, appName)
                putExtra(EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(createChooser(this, getString(R.string.invite_via)))
            }
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
       return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        return
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        try {
            val diffY = e2.y - e1!!.y
            val diffX = e2.x - e1.x
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    }
                    else {
                        return false
                    }
                }
            }
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }
}


