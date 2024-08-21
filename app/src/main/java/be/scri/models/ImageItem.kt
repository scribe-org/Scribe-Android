package be.scri.models

import android.app.Activity
import kotlin.reflect.KFunction0


data class ImageItem(val image: Int, val textResId: Int, val image2:Int, val url: String? = null, val activity: Class<out Activity>? = null, val action: KFunction0<Unit>? = null):Item()
