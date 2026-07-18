# complete code
import androidx.core.util.*

class MainActivity : AppCompatActivity() {
    private val outreachManager: OutreachManager = OutreachManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outreachManager.postOnSocialMedia()
        outreachManager.postOnForums()
        outreachManager.postOnLanguageInstitutions()
    }
}