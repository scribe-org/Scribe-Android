package be.scri.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.ActivityPrivacyPolicyBinding


class PrivacyPolicyFragment : Fragment(){


    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
        val frameLayout = requireActivity().findViewById<ViewGroup>(R.id.fragment_container)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            viewpager.setCurrentItem(2, true);
        }
        callback.isEnabled = true


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityPrivacyPolicyBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.app_about_privacyPolicy)
        (requireActivity() as MainActivity).showFragmentContainer()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
                val frameLayout = requireActivity().findViewById<ViewGroup>(R.id.fragment_container)
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

                (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.app_about_title)
            }
        })
        return binding.root

    }

}



