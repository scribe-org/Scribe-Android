package be.scri.activities


import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.Menu
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_about.*
import be.scri.R
import be.scri.extensions.*
import be.scri.helpers.*
import be.scri.models.ItemsViewModel
import kotlin.math.abs

class AboutActivity : BaseSimpleActivity(), GestureDetector.OnGestureListener{
    private var appName = ""
    private var primaryColor = 0

    private val recyclerView1 by lazy { findViewById<RecyclerView>(R.id.recycleView2) }
    private val recyclerView2 by lazy { findViewById<RecyclerView>(R.id.recycleView) }
    private  val EASTER_EGG_TIME_LIMIT = 3000L
    private  val EASTER_EGG_REQUIRED_CLICKS = 7
    private  val SWIPE_THRESHOLD = 100
    private  val SWIPE_VELOCITY_THRESHOLD = 100

    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        gestureDetector = GestureDetector(this)
        setupRecyclerViews()
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

                R.id.info ->  {
                    return@OnNavigationItemSelectedListener true }
                R.id.installation -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
        bottomNavigationView.selectedItemId = R.id.info
    }


    private fun setupRecyclerViews() {
        val recyclerView1 = findViewById<RecyclerView>(R.id.recycleView2)
        recyclerView1.layoutManager = LinearLayoutManager(this)
        recyclerView1.adapter = CustomAdapter(getFirstRecyclerViewData(), this)
        recyclerView1.suppressLayout(true)

        val recyclerView2 = findViewById<RecyclerView>(R.id.recycleView)
        recyclerView2.layoutManager = LinearLayoutManager(this)
        recyclerView2.adapter = CustomAdapter(getSecondRecyclerViewData(), this)
        recyclerView2.suppressLayout(true)

        val recyclerView3 = findViewById<RecyclerView>(R.id.recycleView3)
        recyclerView3.layoutManager = LinearLayoutManager(this)
        recyclerView3.adapter = CustomAdapter(getThirdRecyclerViewData(), this)
        recyclerView3.suppressLayout(true)
    }

    private fun getFirstRecyclerViewData(): List<Any> = listOf(
        ItemsViewModel(image = R.drawable.github_logo, textResId = R.string.app_about_github, image2 = R.drawable.external_link, url = "https://github.com/scribe-org/Scribe-Android", activity = null, action = null),
        ItemsViewModel(image = R.drawable.matrix_icon, textResId = R.string.app_about_matrix, image2 = R.drawable.external_link, url = "https://matrix.to/%23/%23scribe_community:matrix.org", activity = null, action = null),
        ItemsViewModel(image = R.drawable.mastodon_svg_icon, textResId = R.string.app_about_mastodon, image2 = R.drawable.external_link, url = "https://wikis.world/@scribe", activity = null, action = null),
        ItemsViewModel(image = R.drawable.share_icon, textResId = R.string.app_about_share, image2 = R.drawable.external_link, url = null, activity = null, action = ::shareScribe),
        ItemsViewModel(image = R.drawable.scribe_icon, textResId = R.string.app_about_scribe, image2 = R.drawable.external_link, url = null, activity = null, action = null),
        ItemsViewModel(image = R.drawable.wikimedia_logo_black, textResId = R.string.app_about_wikimedia, image2 = R.drawable.right_arrow, url = null, activity = WikimediaScribeActivity::class.java, action = null)
    )

    private fun getSecondRecyclerViewData(): List<Any> = listOf(
        ItemsViewModel(image = R.drawable.star, textResId = R.string.app_about_rate, image2 = R.drawable.external_link, url = null, activity = null, action = null),
        ItemsViewModel(image = R.drawable.bug_report_icon, textResId = R.string.app_about_bugReport, image2 = R.drawable.external_link, url = "https://github.com/scribe-org/Scribe-Android/issues", activity = null, action = null),
        ItemsViewModel(image = R.drawable.mail_icon, textResId = R.string.app_about_email, image2 = R.drawable.external_link, url = null, activity = null, action = ::sendEmail),
        ItemsViewModel(image = R.drawable.bookmark_icon, textResId = R.string.app_version, image2 = R.drawable.right_arrow, url = null, activity = null, action = null),
        ItemsViewModel(image = R.drawable.light_bulb_icon, textResId = R.string.app_about_appHints, image2 = R.drawable.counter_clockwise_icon, url = null, activity = null, action = null)
    )

    private fun getThirdRecyclerViewData(): List<Any> = listOf(
        ItemsViewModel(image = R.drawable.shield_lock, textResId = R.string.app_about_privacyPolicy, image2 = R.drawable.right_arrow, url = null, activity = PrivacyPolicyActivity::class.java, action = null),
        ItemsViewModel(image = R.drawable.license_icon, textResId = R.string.app_about_thirdParty, image2 = R.drawable.right_arrow, url = null, activity = ThirdPartyActivity::class.java, action = null)
    )


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        }
        else {
            super.onTouchEvent(event)
        }
    }

    fun shareScribe() {
        val sharingIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://github.com/scribe-org/Scribe-Android")
        }
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun sendEmail() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf("team@scri.be"))
            putExtra(Intent.EXTRA_SUBJECT, "Hey Scribe!")
            type = "message/rfc822"
        }
        startActivity(Intent.createChooser(intent, "Choose an Email client:"))
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(about_scrollview)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
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


