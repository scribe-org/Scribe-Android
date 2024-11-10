package be.scri.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentWikimediaScribeBinding

class WikimediaScribeFragment : Fragment() {
    private lateinit var binding: FragmentWikimediaScribeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
        val frameLayout = requireActivity().findViewById<ViewGroup>(R.id.fragment_container)
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                viewpager.setCurrentItem(2, true)
                (requireActivity() as MainActivity).setActionBarVisibility(false)
            }
        (requireActivity() as MainActivity).setActionBarTitle(R.string.app_about_community_wikimedia)
        (requireActivity() as MainActivity).setActionBarButtonVisibility(true)
        (requireActivity() as MainActivity).setActionBarButtonFunction(2, R.string.app_about_title)
        (requireActivity() as MainActivity).setActionBarVisibility(true)
        (requireActivity() as MainActivity)
            .supportActionBar
            ?.customView
            ?.findViewById<Button>(R.id.button)
            ?.text = getString(R.string.app_about_title)
        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWikimediaScribeBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).showFragmentContainer()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
                    val frameLayout = requireActivity().findViewById<ViewGroup>(R.id.fragment_container)
                    (requireActivity() as MainActivity).setActionBarTitle(R.string.app_about_title)
                    (requireActivity() as MainActivity).setActionBarButtonVisibility(false)
                    (requireActivity() as MainActivity).setActionBarVisibility(false)
                    if (viewpager.currentItem == 2) {
                        viewpager.setCurrentItem(2, true)
                        frameLayout.visibility = View.GONE
                    } else {
                        if (parentFragmentManager.backStackEntryCount > 0) {
                            parentFragmentManager.popBackStack()
                        } else {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                }
            },
        )
        return binding.root
    }
}
