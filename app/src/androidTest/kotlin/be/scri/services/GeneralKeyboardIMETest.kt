package be.scri.services

//import android.content.Context
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
//import be.scri.helpers.KeyboardBase
//import be.scri.views.KeyboardView
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

@RunWith(AndroidJUnit4::class)
class GeneralKeyboardIMETest {

    private lateinit var ime: TestGeneralKeyboardIME
    private lateinit var inputConnection: InputConnection

    @Before
    fun setUp() {
        inputConnection = mockk {
            every { getTextBeforeCursor(any(), any()) } returns ""
        }

        ime = TestGeneralKeyboardIME("English", inputConnection)
    }

    @Test
    fun hasTextBeforeCursor_returnsTrue_whenTextExists() {
        every { inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0) } returns "hello"

        println("Result: ${ime.hasTextBeforeCursor}")
        assertTrue(ime.hasTextBeforeCursor)
    }

    @Test
    fun hasTextBeforeCursor_returnsFalse_whenTextIsEmpty() {
        every { inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0) } returns ""

        assertFalse(ime.hasTextBeforeCursor)
    }

    @Test
    fun hasTextBeforeCursor_returnsFalse_whenTextIsPeriod() {
        every { inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0) } returns ". "

        assertFalse(ime.hasTextBeforeCursor)
    }
}

class TestGeneralKeyboardIME(
    language: String,
    private val testInputConnection: InputConnection
) : GeneralKeyboardIME(language) {
    override fun getKeyboardLayoutXML(): Int = 0
    override val keyboardLetters: Int = 0
    override val keyboardSymbols: Int = 0
    override val keyboardSymbolShift: Int = 0
    override var lastShiftPressTS: Long = 0
    override var keyboardMode: Int = 0
    override var inputTypeClass: Int = 0
    override var enterKeyType: Int = 0
    override var switchToLetters: Boolean = false

    // override getter to return test InputConnection
    override fun getCurrentInputConnection(): InputConnection? = testInputConnection

    override var hasTextBeforeCursor: Boolean
        get() {
            val text = testInputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0)?.toString() ?: ""
            return text.isNotEmpty() && text.trim() != "." && text.trim() != ""
        }
        set(value) {}

    override fun onCreate() {}
    override fun onInitializeInterface() {}
    override fun onBindInput() {}
    override fun onUnbindInput() {}
    override fun onCreateInputView(): View = mockk()
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {}
    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {}
    override fun onFinishInputView(finishingInput: Boolean) {}
    override fun onFinishInput() {}
    override fun onPress(primaryCode: Int) {}
    override fun onActionUp() {}
    override fun moveCursorLeft() {}
    override fun moveCursorRight() {}
    override fun onText(text: String) {}
    override fun onKey(code: Int) {}
//    override fun updateShiftKeyState(attr: EditorInfo?) {}
}
