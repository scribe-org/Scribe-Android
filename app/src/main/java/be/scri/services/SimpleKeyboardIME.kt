package be.scri.services

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.text.InputType.TYPE_CLASS_DATETIME
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_MASK_CLASS
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_ENTER_ACTION
import android.view.inputmethod.EditorInfo.IME_MASK_ACTION
import android.view.inputmethod.ExtractedTextRequest
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.databinding.KeyboardViewKeyboardBinding
import be.scri.helpers.MyKeyboard
import be.scri.helpers.SHIFT_OFF
import be.scri.helpers.SHIFT_ON_ONE_CHAR
import be.scri.helpers.SHIFT_ON_PERMANENT
import be.scri.views.MyKeyboardView
// based on https://www.androidauthority.com/lets-build-custom-keyboard-android-832362/

abstract class SimpleKeyboardIME :
    InputMethodService(),
    MyKeyboardView.OnKeyboardActionListener {
    abstract fun getKeyboardLayoutXML(): Int

    abstract var shiftPermToggleSpeed: Int // how quickly do we have to doubletap shift to enable permanent caps lock
    abstract val keyboardLetters: Int
    abstract val keyboardSymbols: Int
    abstract val keyboardSymbolShift: Int

    abstract var keyboard: MyKeyboard?
    abstract var keyboardView: MyKeyboardView?
    abstract var lastShiftPressTS: Long
    abstract var keyboardMode: Int
    abstract var inputTypeClass: Int
    abstract var enterKeyType: Int
    abstract var switchToLetters: Boolean
    abstract var hasTextBeforeCursor: Boolean
    abstract var binding: KeyboardViewCommandOptionsBinding

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)
    }

    override fun hasTextBeforeCursor(): Boolean {
        val inputConnection = currentInputConnection ?: return false
        val textBeforeCursor = inputConnection.getTextBeforeCursor(Int.MAX_VALUE, 0)
        if (textBeforeCursor.isNullOrBlank()) {
            return false
        }
        val trimmedText = textBeforeCursor.trim()
        val lastChar = trimmedText.lastOrNull()
        return lastChar != '.'
    }

    override fun commitPeriodAfterSpace() {
        val inputConnection = currentInputConnection ?: return
        inputConnection.deleteSurroundingText(1, 0)
        inputConnection.commitText(". ", 1)
    }

    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        val keyboardHolder = binding.root
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.setKeyboardHolder(binding.keyboardHolder)
        keyboardView!!.mOnKeyboardActionListener = this
        return keyboardHolder
    }

    override fun onPress(primaryCode: Int) {
        if (primaryCode != 0) {
            keyboardView?.vibrateIfNeeded()
        }
    }

    override fun onStartInput(
        attribute: EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInput(attribute, restarting)

        inputTypeClass = attribute!!.inputType and TYPE_MASK_CLASS
        enterKeyType = attribute.imeOptions and (IME_MASK_ACTION or IME_FLAG_NO_ENTER_ACTION)
        val inputConnection = currentInputConnection
        hasTextBeforeCursor = inputConnection?.getTextBeforeCursor(1, 0)?.isNotEmpty() == true

        val keyboardXml =
            when (inputTypeClass) {
                TYPE_CLASS_NUMBER, TYPE_CLASS_DATETIME, TYPE_CLASS_PHONE -> {
                    keyboardMode = keyboardSymbols
                    R.xml.keys_symbols
                }
                else -> {
                    keyboardMode = keyboardLetters
                    getKeyboardLayoutXML()
                }
            }

        keyboard = MyKeyboard(this, keyboardXml, enterKeyType)
        keyboardView?.setKeyboard(keyboard!!)
    }

    fun updateShiftKeyState() {
        if (keyboardMode == keyboardLetters) {
            val editorInfo = currentInputEditorInfo
            if (editorInfo != null && editorInfo.inputType != InputType.TYPE_NULL && keyboard?.mShiftState != SHIFT_ON_PERMANENT) {
                if (currentInputConnection.getCursorCapsMode(editorInfo.inputType) != 0) {
                    keyboard?.setShifted(SHIFT_ON_ONE_CHAR)
                    keyboardView?.invalidateAllKeys()
                }
            }
        }
    }

    override fun onActionUp() {
        if (switchToLetters) {
            keyboardMode = keyboardLetters
            keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)

            val editorInfo = currentInputEditorInfo
            if (editorInfo != null && editorInfo.inputType != InputType.TYPE_NULL && keyboard?.mShiftState != SHIFT_ON_PERMANENT) {
                if (currentInputConnection.getCursorCapsMode(editorInfo.inputType) != 0) {
                    keyboard?.setShifted(SHIFT_ON_ONE_CHAR)
                }
            }

            keyboardView!!.setKeyboard(keyboard!!)
            switchToLetters = false
        }
    }

    override fun moveCursorLeft() {
        moveCursor(false)
    }

    override fun moveCursorRight() {
        moveCursor(true)
    }

    override fun onText(text: String) {
        currentInputConnection?.commitText(text, 0)
    }

    private fun moveCursor(moveRight: Boolean) {
        val extractedText = currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0) ?: return
        var newCursorPosition = extractedText.selectionStart
        newCursorPosition =
            if (moveRight) {
                newCursorPosition + 1
            } else {
                newCursorPosition - 1
            }

        currentInputConnection?.setSelection(newCursorPosition, newCursorPosition)
    }

    private fun getImeOptionsActionId(): Int =
        if (currentInputEditorInfo.imeOptions and IME_FLAG_NO_ENTER_ACTION != 0) {
            IME_ACTION_NONE
            Log.i("MYT-TAG", "Hello from ime")
        } else {
            currentInputEditorInfo.imeOptions and IME_MASK_ACTION
            Log.i("MYT-TAG", "Hello from ime")
        }

    fun handleKeycodeEnter() {
        val inputConnection = currentInputConnection
        val imeOptionsActionId = getImeOptionsActionId()
        if (imeOptionsActionId != IME_ACTION_NONE) {
            inputConnection.performEditorAction(imeOptionsActionId)
        } else {
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    fun handleModeChange(
        keyboardMode: Int,
        keyboardView: MyKeyboardView?,
        context: Context,
    ) {
        val keyboardXml =
            if (keyboardMode == keyboardLetters) {
                this.keyboardMode = keyboardSymbols
                R.xml.keys_symbols
            } else {
                this.keyboardMode = keyboardLetters
                getKeyboardLayoutXML()
            }
        keyboard = MyKeyboard(context, keyboardXml, enterKeyType)
        keyboardView?.invalidateAllKeys()
        keyboardView?.setKeyboard(keyboard!!)
    }

    fun handleKeyboardLetters(
        keyboardMode: Int,
        keyboardView: MyKeyboardView?,
        context: Context,
    ) {
        if (keyboardMode == keyboardLetters) {
            when {
                keyboard!!.mShiftState == SHIFT_ON_PERMANENT -> keyboard!!.mShiftState = SHIFT_OFF
                System.currentTimeMillis() - lastShiftPressTS < shiftPermToggleSpeed -> keyboard!!.mShiftState = SHIFT_ON_PERMANENT
                keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR -> keyboard!!.mShiftState = SHIFT_OFF
                keyboard!!.mShiftState == SHIFT_OFF -> keyboard!!.mShiftState = SHIFT_ON_ONE_CHAR
            }

            lastShiftPressTS = System.currentTimeMillis()
        } else {
            val keyboardXml =
                if (keyboardMode == keyboardSymbols) {
                    this.keyboardMode = keyboardSymbolShift
                    R.xml.keys_symbols_shift
                } else {
                    this.keyboardMode = keyboardSymbols
                    R.xml.keys_symbols
                }
            keyboard = MyKeyboard(this, keyboardXml, enterKeyType)
            keyboardView!!.setKeyboard(keyboard!!)
        }
    }

    fun handleDelete() {
        val inputConnection = currentInputConnection
        if (keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR) {
            keyboard!!.mShiftState = SHIFT_OFF
        }

        val selectedText = inputConnection.getSelectedText(0)
        if (TextUtils.isEmpty(selectedText)) {
            inputConnection.deleteSurroundingText(1, 0)
        } else {
            inputConnection.commitText("", 1)
        }
    }

    fun handleElseCondition(
        code: Int,
        keyboardMode: Int,
    ) {
        val inputConnection = currentInputConnection
        var codeChar = code.toChar()
        if (Character.isLetter(codeChar) && keyboard!!.mShiftState > SHIFT_OFF) {
            codeChar = Character.toUpperCase(codeChar)
        }

        // If the keyboard is set to symbols and the user presses space, we usually should switch back to the letters keyboard.
        // However, avoid doing that in cases when the EditText for example requires numbers as the input.
        // We can detect that by the text not changing on pressing Space.
        if (keyboardMode != keyboardLetters && code == MyKeyboard.KEYCODE_SPACE) {
            val originalText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
            inputConnection.commitText(codeChar.toString(), 1)
            val newText = inputConnection.getExtractedText(ExtractedTextRequest(), 0).text
            switchToLetters = originalText != newText
        } else {
            inputConnection.commitText(codeChar.toString(), 1)
        }

        if (keyboard!!.mShiftState == SHIFT_ON_ONE_CHAR && keyboardMode == keyboardLetters) {
            keyboard!!.mShiftState = SHIFT_OFF
            keyboardView!!.invalidateAllKeys()
        }
    }

    fun setupToolBarTheme(binding: KeyboardViewKeyboardBinding) {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", true)
        when (isUserDarkMode) {
            true -> {
                binding.commandField.setBackgroundColor(getColor(R.color.md_grey_black_dark))
            }
            else -> {
                binding.commandField.setBackgroundColor(getColor(R.color.light_cmd_bar_border_color))
            }
        }
    }

    fun setupCommandBarTheme(binding: KeyboardViewCommandOptionsBinding) {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", true)
        when (isUserDarkMode) {
            true -> {
                binding.commandField.setBackgroundColor(getColor(R.color.md_grey_black_dark))
            }
            else -> {
                binding.commandField.setBackgroundColor(getColor(R.color.light_cmd_bar_border_color))
            }
        }
    }
}
