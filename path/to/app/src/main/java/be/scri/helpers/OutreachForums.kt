# complete code
import androidx.core.util.*

class OutreachForums(private val context: Context) {
    private val outreachManager: OutreachManager = OutreachManager(context)

    fun postOnForums() {
        outreachManager.postOnForums()
    }
}