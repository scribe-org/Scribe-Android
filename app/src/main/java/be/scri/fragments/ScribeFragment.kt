//package be.scri.fragments
//
//import android.util.Log
//import androidx.fragment.app.Fragment
//import be.scri.R
//import be.scri.activities.MainActivity
//
//abstract class ScribeFragment(
//    val fragmentName: String,
//) : Fragment() {
//    override fun onPause() {
//        super.onPause()
//        (activity as MainActivity).hideHint()
//    }
//
//    protected fun loadOtherFragment(
//        fragment: Fragment,
//        pageName: String?,
//    ) {
//        try {
//            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
//            if (pageName != null) {
//                fragmentTransaction.replace(R.id.fragment_container, fragment, pageName)
//            } else {
//                fragmentTransaction.replace(R.id.fragment_container, fragment)
//            }
//            fragmentTransaction.addToBackStack(pageName)
//            fragmentTransaction.commit()
//        } catch (e: IllegalStateException) {
//            Log.e("${fragmentName}Fragment", "Failed to load fragment", e)
//        }
//    }
//}
