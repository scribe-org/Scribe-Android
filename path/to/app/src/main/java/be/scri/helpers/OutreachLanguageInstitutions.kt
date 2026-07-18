# complete code
import androidx.core.util.*

class OutreachLanguageInstitutions(private val context: Context) {
    private val outreachManager: OutreachManager = OutreachManager(context)

    fun postOnLanguageInstitutions() {
        outreachManager.postOnLanguageInstitutions()
    }
}