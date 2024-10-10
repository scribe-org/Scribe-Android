package be.scri.models

import android.app.Activity
import androidx.annotation.StringRes
import kotlin.reflect.KFunction0

data class ItemsViewModel(
    val image: Int,
    val text: Text,
    val image2: Int,
    val url: String? = null,
    val activity: Class<out Activity>? = null,
    val action: KFunction0<Unit>? = null,
) : Item() {
    class Text(
        @StringRes
        val resId: Int,
        vararg val formatArgs: Any,
    )
}
