package be.scri.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import be.scri.R
import be.scri.activities.MainActivity
import be.scri.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {
    private lateinit var binding: FragmentPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                viewpager.setCurrentItem(2, true)
                (requireActivity() as MainActivity).unsetActionBarLayoutMargin()
            }
        (requireActivity() as MainActivity).setActionBarButtonVisible()
        (requireActivity() as MainActivity).setActionBarTitle(R.string.app_about_legal_privacy_policy)
        (requireActivity() as MainActivity).setActionBarLayoutMargin()
        val textView =
            (requireActivity() as MainActivity)
                .supportActionBar
                ?.customView
                ?.findViewById<TextView>(R.id.name)

        (requireActivity() as MainActivity)
            .supportActionBar
            ?.customView
            ?.findViewById<Button>(R.id.button)
            ?.text = getString(R.string.app_about_title)

        textView?.let {
            val params = it.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = -50
            it.layoutParams = params
        }

        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).showFragmentContainer()
        (requireActivity() as MainActivity).setActionBarTitle(R.string.app_about_legal_privacy_policy)
        (requireActivity() as MainActivity).setActionBarButtonFunction(2, R.string.app_about_title)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val viewpager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
                    val frameLayout = requireActivity().findViewById<ViewGroup>(R.id.fragment_container)
                    (requireActivity() as MainActivity).setActionBarTitle(R.string.app_about_title)
                    (requireActivity() as MainActivity).setActionBarButtonInvisible()
                    (requireActivity() as MainActivity).unsetActionBarLayoutMargin()
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
