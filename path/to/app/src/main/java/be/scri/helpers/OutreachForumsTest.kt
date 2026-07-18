# complete code
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class OutreachForumsTest {
    @Test
    fun testPostOnForums() {
        val context = Application()
        val outreachForums = OutreachForums(context)
        outreachForums.postOnForums()
        verify(outreachForums).postOnForums()
    }
}