package be.scri.activities

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import be.scri.R
import be.scri.adapters.ViewPagerAdapter
import be.scri.databinding.ActivityMainBinding

class MainActivity : SimpleActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = findViewById(R.id.view_pager)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        if (savedInstanceState == null) {
            viewPager.setCurrentItem(0, false)
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigationView.menu.getItem(position).isChecked = true
                when(position) {
                    0 -> {
                        supportActionBar?.title = getString(R.string.app_title_new)
                        binding.fragmentContainer.visibility = View.GONE
                    }
                    1 -> {
                        supportActionBar?.title = getString(R.string.app_settings_title)
                        binding.fragmentContainer.visibility = View.GONE
                    }
                    2 -> {
                        supportActionBar?.title = getString(R.string.app_about_title)
                        binding.fragmentContainer.visibility = View.GONE
                    }
                }
            }
        })


        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.installation -> {
                    viewPager.setCurrentItem(0, true)
                    supportActionBar?.title = getString(R.string.app_title_new)
                    binding.fragmentContainer.visibility = View.GONE
                    true
                }
                R.id.info -> {
                    viewPager.setCurrentItem(2, true)
                    binding.fragmentContainer.visibility = View.GONE
                    supportActionBar?.title = getString(R.string.app_about_title)
                    true
                }
                R.id.settings -> {
                    viewPager.setCurrentItem(1, true)
                    binding.fragmentContainer.visibility = View.GONE
                    supportActionBar?.title = getString(R.string.app_settings_title)
                    true
                }
                else -> {
                  false
                }
            }
        }
    }
    fun showFragmentContainer() {
        binding.fragmentContainer.visibility = View.VISIBLE
    }

}
