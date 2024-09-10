package be.scri.services

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.databinding.KeyboardViewKeyboardBinding
import be.scri.helpers.MyKeyboard
import be.scri.services.EnglishKeyboardIME.ScribeState
import be.scri.views.MyKeyboardView

class GermanKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_german



    enum class ScribeState {
        IDLE, SELECT_COMMAND, TRANSLATE, CONJUGATE, PLURAL , SELECT_VERB_CONJUNCTION , SELECT_CASE_DECLENSION , ALREADY_PLURAL, INVALID , DISPLAY_INFORMATION
    }
    private var currentState: ScribeState = ScribeState.IDLE
    private lateinit var keyboardBinding: KeyboardViewKeyboardBinding
    private lateinit var commandBinding: KeyboardViewCommandOptionsBinding
    private lateinit var binding: KeyboardViewCommandOptionsBinding
    private var keyboardView: MyKeyboardView? = null
    private var keyboard: MyKeyboard? = null
    private var enterKeyType = IME_ACTION_NONE

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        keyboard = MyKeyboard(this, getKeyboardLayoutXML(), enterKeyType)
    }

    private var lastSpaceTime: Long = 0
    private val doubleTapThreshold: Long = 300

    private fun shouldCommitPeriodAfterSpace(language: String): Boolean {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("period_on_double_tap_$language", false)
    }

    override fun commitPeriodAfterSpace() {
        if (shouldCommitPeriodAfterSpace("German")) {
            val inputConnection = currentInputConnection ?: return
            inputConnection.deleteSurroundingText(1, 0)
            inputConnection.commitText(". ", 1)
        }
    }

    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        val keyboardHolder = binding.root
        Log.i("MY-TAG","From German Keyboard IME")
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.setKeyboardHolder(binding.keyboardHolder)
        keyboardView!!.mOnKeyboardActionListener = this
        updateUI()
        return keyboardHolder
    }


    private fun setupIdleView() {
        binding.translateBtn.setBackgroundColor(getColor(R.color.you_keyboard_background_color))
        binding.conjugateBtn.setBackgroundColor(getColor(R.color.you_keyboard_background_color))
        binding.pluralBtn.setBackgroundColor(getColor(R.color.you_keyboard_background_color))
        binding.translateBtn.text = ""
        binding.conjugateBtn.text = ""
        binding.pluralBtn.text = ""
        binding.scribeKey.setOnClickListener{
            currentState = ScribeState.SELECT_COMMAND
            Log.i("MY-TAG","SELECT COMMAND STATE")
            binding.scribeKey.foreground = getDrawable(R.drawable.close)
            updateUI()
        }
    }

    private fun setupSelectCommandView() {

        binding.translateBtn.setBackgroundDrawable(getDrawable(R.drawable.button_background_rounded))
        binding.conjugateBtn.setBackgroundDrawable(getDrawable(R.drawable.button_background_rounded))
        binding.pluralBtn.setBackgroundDrawable(getDrawable(R.drawable.button_background_rounded))
        binding.translateBtn.text = "Translate"
        binding.conjugateBtn.text = "Conjugate"
        binding.pluralBtn.text = "Plural"
        binding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            Log.i("MY-TAG","IDLE STATE")
            binding.scribeKey.foreground = getDrawable(R.drawable.ic_scribe_icon_vector)
            updateUI()
        }
        binding.translateBtn.setOnClickListener {
            currentState = ScribeState.TRANSLATE
            Log.i("MY-TAG","TRANSLATE STATE")
            updateUI()
        }
        binding.conjugateBtn.setOnClickListener {
            Log.i("MY-TAG","CONJUGATE STATE")
            currentState = ScribeState.CONJUGATE
            updateUI()
        }
        binding.pluralBtn.setOnClickListener {
            Log.i("MY-TAG","PLURAL STATE")
            currentState = ScribeState.PLURAL
            updateUI()
        }
    }

    private fun switchToToolBar() {
        val keyboardBinding = KeyboardViewKeyboardBinding.inflate(layoutInflater)
        this.keyboardBinding = keyboardBinding
        val keyboardHolder = keyboardBinding.root
        keyboardView = keyboardBinding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.mOnKeyboardActionListener = this
        keyboardBinding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            switchToCommandToolBar()
            updateUI()
        }
        setInputView(keyboardHolder)
    }

    private fun switchToCommandToolBar() {
        val binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        this.binding = binding
        val keyboardHolder = binding.root
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        keyboardView!!.mOnKeyboardActionListener = this
        keyboardBinding.scribeKey.setOnClickListener {
            currentState = ScribeState.IDLE
            setupSelectCommandView()
            updateUI()
        }
        setInputView(keyboardHolder)
    }


    private fun updateUI() {
        when (currentState) {
            ScribeState.IDLE -> setupIdleView()
            ScribeState.SELECT_COMMAND -> setupSelectCommandView()
            else -> switchToToolBar()
        }
    }

}
