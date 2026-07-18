# complete code
import com.google.android.gms:play-services-social:*

class OutreachSocialMedia(private val context: Context) {
    private val outreachManager: OutreachManager = OutreachManager(context)

    fun postOnSocialMedia() {
        outreachManager.postOnSocialMedia()
    }
}