// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import be.scri.R
import be.scri.R.color.white
import be.scri.databinding.InputMethodViewBinding
import be.scri.helpers.KeyboardBase
import be.scri.helpers.LanguageMappingConstants.conjugatePlaceholder
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.helpers.LanguageMappingConstants.pluralPlaceholder
import be.scri.helpers.LanguageMappingConstants.translatePlaceholder
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot
import be.scri.helpers.english.ENInterfaceVariables.ALREADY_PLURAL_MSG
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState
import be.scri.views.KeyboardView

/**
 * Manages the UI elements and state transitions for the GeneralKeyboardIME.
 * This class handles View interactions, visibility toggling, and layout updates.
 */
@Suppress("TooManyFunctions", "LargeClass")
class KeyboardUIManager(
    val binding: InputMethodViewBinding,
    private val context: Context,
    private val listener: KeyboardUIListener,
) {
    interface KeyboardUIListener {
        fun onScribeKeyOptionsClicked()

        fun onScribeKeyToolbarClicked()

        fun onTranslateClicked()

        fun onConjugateClicked()

        fun onPluralClicked()

        fun onCloseClicked()

        fun onEmojiSelected(emoji: String)

        fun onSuggestionClicked(suggestion: String)

        fun getKeyboardLayoutXML(): Int

        fun getCurrentEnterKeyType(): Int

        fun commitText(text: String)

        fun onKeyboardActionListener(): KeyboardView.OnKeyboardActionListener

        fun processLinguisticSuggestions(word: String)
    }

    var keyboardView: KeyboardView = binding.keyboardView
    var keyboard: KeyboardBase? = null

    // UI Elements
    var pluralBtn: Button? = binding.pluralBtn
    var emojiBtnPhone1: Button? = binding.emojiBtnPhone1
    var emojiSpacePhone: View? = binding.emojiSpacePhone
    var emojiBtnPhone2: Button? = binding.emojiBtnPhone2
    var emojiBtnTablet1: Button? = binding.emojiBtnTablet1
    var emojiSpaceTablet1: View? = binding.emojiSpaceTablet1
    var emojiBtnTablet2: Button? = binding.emojiBtnTablet2
    var emojiSpaceTablet2: View? = binding.emojiSpaceTablet2
    var emojiBtnTablet3: Button? = binding.emojiBtnTablet3
    var genderSuggestionLeft: Button? = binding.translateBtnLeft
    var genderSuggestionRight: Button? = binding.translateBtnRight

    // State Variables specific to UI rendering
    var currentCommandBarHint: String = ""
    var commandBarHintColor: Int = Color.GRAY
    var commandBarTextColor: Int = Color.BLACK
    private var earlierValue: Int? = keyboardView.setEnterKeyIcon(ScribeState.IDLE)

    private var currentPage = 0
    private val totalPages = 3
    private val explanationStrings =
        arrayOf(
            R.string.i18n_app_keyboard_not_in_wikidata_explanation_1,
            R.string.i18n_app_keyboard_not_in_wikidata_explanation_2,
            R.string.i18n_app_keyboard_not_in_wikidata_explanation_3,
        )

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.scribeKeyOptions.setOnClickListener { listener.onScribeKeyOptionsClicked() }
        binding.scribeKeyToolbar.setOnClickListener { listener.onScribeKeyToolbarClicked() }

        binding.translateBtn.setOnClickListener { listener.onTranslateClicked() }
        binding.conjugateBtn.setOnClickListener { listener.onConjugateClicked() }
        binding.pluralBtn.setOnClickListener { listener.onPluralClicked() }

        binding.scribeKeyClose.setOnClickListener { listener.onCloseClicked() }

        // Info button listener for INVALID state
        binding.ivInfo.setOnClickListener { showInvalidInfo() }
    }

    /**
     * Updates the color of the Enter key based on the current Scribe state and theme (dark/light mode).
     *
     * @param isDarkMode The current dark mode status. If null, it will be determined from context.
     * @param currentState The current state of the keyboard.
     */
    fun updateEnterKeyColor(
        isDarkMode: Boolean?,
        currentState: ScribeState,
    ) {
        val resolvedIsDarkMode = isDarkMode ?: getIsDarkModeOrNot(context)
        when (currentState) {
            ScribeState.IDLE, ScribeState.SELECT_COMMAND -> {
                keyboardView.setEnterKeyIcon(ScribeState.IDLE, earlierValue)
                keyboardView.setEnterKeyColor(null, isDarkMode = resolvedIsDarkMode)
            }
            else -> {
                keyboardView.setEnterKeyColor(context.getColor(R.color.color_primary))
                keyboardView.setEnterKeyIcon(ScribeState.PLURAL, earlierValue)
            }
        }
        val scribeKeyTint = if (resolvedIsDarkMode) R.color.light_key_color else R.color.light_key_text_color
        binding.scribeKeyOptions.foregroundTintList = ContextCompat.getColorStateList(context, scribeKeyTint)
        binding.scribeKeyToolbar.foregroundTintList = ContextCompat.getColorStateList(context, scribeKeyTint)
    }

    /**
     * The main dispatcher for updating the entire keyboard UI. It calls the appropriate setup function
     * based on the current [ScribeState].
     */
    fun updateUI(
        currentState: ScribeState,
        language: String,
        emojiAutoSuggestionEnabled: Boolean,
        autoSuggestEmojis: MutableList<String>?,
        conjugateOutput: Map<String, Map<String, Collection<String>>>?,
        conjugateLabels: Set<String>?,
        selectedConjugationSubCategory: String?,
        currentVerbForConjugation: String?,
    ) {
        val isUserDarkMode = getIsDarkModeOrNot(context)

        when (currentState) {
            ScribeState.IDLE -> setupIdleView(language, emojiAutoSuggestionEnabled, autoSuggestEmojis)
            ScribeState.SELECT_COMMAND -> setupSelectCommandView(language)
            ScribeState.INVALID -> setupInvalidView(language)
            ScribeState.TRANSLATE -> {
                setupToolbarView(currentState, language, conjugateOutput, conjugateLabels, selectedConjugationSubCategory, currentVerbForConjugation)
                binding.translateBtn.text = translatePlaceholder[getLanguageAlias(language)] ?: "Translate"
                binding.translateBtn.visibility = View.VISIBLE
            }
            ScribeState.CONJUGATE, ScribeState.SELECT_VERB_CONJUNCTION, ScribeState.PLURAL -> {
                setupToolbarView(currentState, language, conjugateOutput, conjugateLabels, selectedConjugationSubCategory, currentVerbForConjugation)
            }
            ScribeState.ALREADY_PLURAL -> setupAlreadyPluralView()
        }

        updateEnterKeyColor(isUserDarkMode, currentState)
    }

    /**
     * Configures the UI for the `IDLE` state, showing default suggestions or emoji suggestions.
     */
    private fun setupIdleView(
        language: String,
        emojiAutoSuggestionEnabled: Boolean,
        autoSuggestEmojis: MutableList<String>?,
    ) {
        binding.commandOptionsBar.visibility = View.VISIBLE
        binding.toolbarBar.visibility = View.GONE

        val isUserDarkMode = getIsDarkModeOrNot(context)

        binding.commandOptionsBar.setBackgroundColor(
            ContextCompat.getColor(
                context,
                if (isUserDarkMode) R.color.dark_keyboard_bg_color else R.color.light_keyboard_bg_color,
            ),
        )

        val textColor = if (isUserDarkMode) Color.WHITE else "#1E1E1E".toColorInt()

        listOf(binding.translateBtn, binding.conjugateBtn, binding.pluralBtn).forEachIndexed { index, button ->
            button.visibility = View.VISIBLE
            button.background = null
            button.setTextColor(textColor)
            button.text = HintUtils.getBaseAutoSuggestions(language).getOrNull(index)
            button.isAllCaps = false
            button.textSize = GeneralKeyboardIME.SUGGESTION_SIZE
            button.setOnClickListener(null)
        }

        listOf(binding.separator2, binding.separator3).forEach { separator ->
            separator.setBackgroundColor(ContextCompat.getColor(context, R.color.special_key_light))
            val params = separator.layoutParams
            // Convert 0.5dp to pixels. coerceAtLeast(1) ensures it's never zero.
            params.width = (0.5f * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
            separator.layoutParams = params
            separator.visibility = View.VISIBLE
        }

        binding.separator1.visibility = View.GONE
        binding.ivInfo.visibility = View.GONE
        binding.conjugateGridContainer.visibility = View.GONE
        binding.keyboardView.visibility = View.VISIBLE

        binding.scribeKeyOptions.foreground = AppCompatResources.getDrawable(context, R.drawable.ic_scribe_icon_vector)

        initializeKeyboard(listener.getKeyboardLayoutXML())

        updateButtonVisibility(ScribeState.IDLE, emojiAutoSuggestionEnabled, autoSuggestEmojis)
        updateEmojiSuggestion(ScribeState.IDLE, emojiAutoSuggestionEnabled, autoSuggestEmojis)
        binding.commandBar.setText("")
        disableAutoSuggest(language)
    }

    /**
     * Configures the UI for the `SELECT_COMMAND` state, showing the main command buttons
     * (Translate, Conjugate, Plural).
     */
    private fun setupSelectCommandView(language: String) {
        binding.commandOptionsBar.visibility = View.VISIBLE
        binding.toolbarBar.visibility = View.GONE

        val isUserDarkMode = getIsDarkModeOrNot(context)
        binding.commandOptionsBar.setBackgroundColor(
            ContextCompat.getColor(
                context,
                if (isUserDarkMode) R.color.dark_keyboard_bg_color else R.color.light_keyboard_bg_color,
            ),
        )

        val langAlias = getLanguageAlias(language)

        updateButtonVisibility(ScribeState.SELECT_COMMAND, false, null)

        binding.translateBtn.setOnClickListener { listener.onTranslateClicked() }
        binding.conjugateBtn.setOnClickListener { listener.onConjugateClicked() }
        binding.pluralBtn.setOnClickListener { listener.onPluralClicked() }

        val buttonTextColor = if (isUserDarkMode) Color.WHITE else Color.BLACK

        listOf(binding.translateBtn, binding.conjugateBtn, binding.pluralBtn).forEach { button ->
            button.visibility = View.VISIBLE
            button.background = ContextCompat.getDrawable(context, R.drawable.button_background_rounded)
            button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.theme_scribe_blue)
            button.setTextColor(buttonTextColor)
            button.textSize = GeneralKeyboardIME.SUGGESTION_SIZE
        }

        binding.translateBtn.text = translatePlaceholder[langAlias] ?: "Translate"
        binding.conjugateBtn.text = conjugatePlaceholder[langAlias] ?: "Conjugate"
        binding.pluralBtn.text = pluralPlaceholder[langAlias] ?: "Plural"

        val separatorColor = (if (isUserDarkMode) GeneralKeyboardIME.DARK_THEME else GeneralKeyboardIME.LIGHT_THEME).toColorInt()
        binding.separator2.setBackgroundColor(separatorColor)
        binding.separator3.setBackgroundColor(separatorColor)

        val spaceInDp = 4
        val spaceInPx = (spaceInDp * context.resources.displayMetrics.density).toInt()
        listOf(binding.separator2, binding.separator3).forEach { separator ->
            separator.setBackgroundColor(Color.TRANSPARENT)
            val params = separator.layoutParams
            params.width = spaceInPx
            separator.layoutParams = params
        }

        binding.separator1.visibility = View.GONE
        binding.separator2.visibility = View.VISIBLE
        binding.separator3.visibility = View.VISIBLE
        binding.separator4.visibility = View.GONE
        binding.separator5.visibility = View.GONE
        binding.separator6.visibility = View.GONE
        binding.ivInfo.visibility = View.GONE
        binding.scribeKeyOptions.foreground = AppCompatResources.getDrawable(context, R.drawable.close)
    }

    /**
     * Configures the UI for command modes (`TRANSLATE`, `CONJUGATE`, etc.), showing the command bar and toolbar.
     */
    @SuppressLint("InflateParams")
    private fun setupToolbarView(
        currentState: ScribeState,
        language: String,
        conjugateOutput: Map<String, Map<String, Collection<String>>>?,
        conjugateLabels: Set<String>?,
        selectedConjugationSubCategory: String?,
        currentVerbForConjugation: String?,
    ) {
        binding.commandOptionsBar.visibility = View.GONE
        binding.toolbarBar.visibility = View.VISIBLE
        val isDarkMode = getIsDarkModeOrNot(context)
        binding.toolbarBar.setBackgroundColor(
            ContextCompat.getColor(
                context,
                if (isDarkMode) R.color.dark_keyboard_bg_color else R.color.light_keyboard_bg_color,
            ),
        )
        binding.ivInfo.visibility = View.GONE

        binding.scribeKeyToolbar.foreground = AppCompatResources.getDrawable(context, R.drawable.close)

        var hintWord: String? = null
        var promptText: String? = null

        if (currentState == ScribeState.SELECT_VERB_CONJUNCTION) {
            binding.conjugateGridContainer.visibility = View.VISIBLE
            binding.keyboardView.visibility = View.GONE

            val grid = binding.conjugateGrid
            grid.removeAllViews()

            val conjugateIndex = getValidatedConjugateIndex(conjugateOutput)
            val title = conjugateOutput?.keys?.elementAtOrNull(conjugateIndex)
            val languageOutput = title?.let { conjugateOutput[it] }

            val isSubSelection = selectedConjugationSubCategory != null
            val showCategories = !isSubSelection && (languageOutput?.containsKey(title) != true)

            val forms =
                if (isSubSelection) {
                    languageOutput?.get(selectedConjugationSubCategory)?.toList() ?: listOf("", "", "", "")
                } else if (showCategories) {
                    languageOutput?.map { (_, values) ->
                        if (values.size == 1) values.first() else values.joinToString(" / ")
                    } ?: listOf("", "", "", "")
                } else {
                    languageOutput?.get(title)?.toList() ?: listOf("", "", "", "")
                }

            val layoutResId =
                when {
                    isSubSelection -> R.layout.conjugate_grid_2x1
                    language == "English" && forms.size <= 4 -> R.layout.conjugate_grid_2x2
                    language in listOf("Russian", "Swedish") && forms.size <= 4 -> R.layout.conjugate_grid_2x2
                    forms.size > 4 -> R.layout.conjugate_grid_3x2
                    else -> R.layout.conjugate_grid_2x2
                }

            val layoutInflater = LayoutInflater.from(context)
            val gridContent = layoutInflater.inflate(layoutResId, grid, false) as LinearLayout
            grid.addView(gridContent)

            val buttonIds =
                listOf(
                    R.id.conjugate_btn_1,
                    R.id.conjugate_btn_2,
                    R.id.conjugate_btn_3,
                    R.id.conjugate_btn_4,
                    R.id.conjugate_btn_5,
                    R.id.conjugate_btn_6,
                )

            buttonIds.forEachIndexed { i, btnId ->
                val btn = gridContent.findViewById<Button?>(btnId)
                if (btn != null) {
                    btn.text = forms.getOrNull(i) ?: ""
                    btn.setOnClickListener {
                        val label = btn.text.toString()
                        if (label.isNotEmpty()) {
                            var handledAsCategory = false
                            if (showCategories) {
                                val matchingEntry =
                                    languageOutput?.entries?.find { (_, values) ->
                                        if (values.size == 1) values.first() == label else values.joinToString(" / ") == label
                                    }

                                if (matchingEntry != null) {
                                    val (key, values) = matchingEntry
                                    if (values.size > 1) {
                                        // Category logic is handled in IME's commitText
                                    }
                                }
                            }

                            if (!handledAsCategory) {
                                listener.commitText("$label ")
                                listener.processLinguisticSuggestions(label)
                            }
                        }
                    }
                }
            }

            setupConjugateArrows(gridContent, context)

            promptText = if (isSubSelection) selectedConjugationSubCategory else (title ?: "___")
            hintWord = conjugateLabels?.lastOrNull()
        } else {
            binding.conjugateGridContainer.visibility = View.GONE
            binding.keyboardView.visibility = View.VISIBLE
        }

        updateCommandBarHintAndPrompt(currentState, language, promptText, isDarkMode, hintWord, currentVerbForConjugation)
    }

    /**
     * Sets up the navigation arrow buttons for the conjugation grid view.
     */
    private fun setupConjugateArrows(
        gridContent: View,
        context: Context,
    ) {
        val arrowButtonIds =
            listOf(
                "conjugate_arrow_left_1",
                "conjugate_arrow_right_1",
                "conjugate_arrow_left_2",
                "conjugate_arrow_right_2",
                "conjugate_arrow_left_3",
                "conjugate_arrow_right_3",
                "conjugate_arrow_left",
                "conjugate_arrow_right",
            )

        arrowButtonIds.forEach { arrowBtnName ->
            val arrowBtnId = context.resources.getIdentifier(arrowBtnName, "id", context.packageName)
            if (arrowBtnId != 0) {
                val arrowBtn = gridContent.findViewById<Button?>(arrowBtnId)
                arrowBtn?.setOnClickListener {
                    val isLeft = arrowBtnName.contains("left")
                    val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
                    val current = prefs.getInt("conjugate_index", 0)
                    val newValue = if (isLeft) current - 1 else current + 1
                    prefs.edit { putInt("conjugate_index", newValue) }

                    listener.onConjugateClicked()
                }
            }
        }
    }

    /**
     * Configures the UI for the `INVALID` state, which is shown when a command (e.g., translation) fails.
     */
    @SuppressLint("SetTextI18n")
    private fun setupInvalidView(language: String) {
        binding.commandOptionsBar.visibility = View.GONE
        binding.toolbarBar.visibility = View.VISIBLE
        // Original logic: Invalid state actually uses the toolbarBar layout initially
        binding.invalidInfoBar.visibility = View.GONE

        val isDarkMode = getIsDarkModeOrNot(context)

        // Restore original logic: Set background on toolbarBar, not invalidInfoBar
        binding.toolbarBar.setBackgroundColor(
            if (isDarkMode) "#1E1E1E".toColorInt() else "#d2d4da".toColorInt(),
        )

        binding.ivInfo.visibility = View.VISIBLE
        binding.promptText.text = HintUtils.getInvalidHint(language = language) + ": "
        binding.commandBar.hint = ""
        binding.scribeKeyToolbar.foreground = AppCompatResources.getDrawable(context, R.drawable.ic_scribe_icon_vector)
    }

    /**
     * Configures the UI for the `ALREADY_PLURAL` state, which is shown when the user
     * attempts to pluralize a word that is already plural.
     */
    @SuppressLint("SetTextI18n")
    private fun setupAlreadyPluralView() {
        binding.commandOptionsBar.visibility = View.GONE
        binding.toolbarBar.visibility = View.VISIBLE
        val isDarkMode = getIsDarkModeOrNot(context)
        binding.toolbarBar.setBackgroundColor(if (isDarkMode) "#1E1E1E".toColorInt() else "#d2d4da".toColorInt())
        binding.ivInfo.visibility = View.VISIBLE
        binding.promptText.text = "$ALREADY_PLURAL_MSG: "
        binding.commandBar.hint = ""
        binding.scribeKeyToolbar.foreground = AppCompatResources.getDrawable(context, R.drawable.ic_scribe_icon_vector)
    }

    /**
     * Updates the hint and prompt text displayed in the command bar area based on the current state.
     *
     * @param currentState The current keyboard state.
     * @param language The current language.
     * @param text Specific text for the prompt (optional).
     * @param isUserDarkMode The current dark mode status.
     * @param word A word to include in the hint (optional).
     */
    @SuppressLint("SetTextI18n")
    fun updateCommandBarHintAndPrompt(
        currentState: ScribeState,
        language: String,
        text: String? = null,
        isUserDarkMode: Boolean? = null,
        word: String? = null,
        currentVerbForConjugation: String? = null,
    ) {
        val resolvedIsDarkMode = isUserDarkMode ?: getIsDarkModeOrNot(context)
        val commandBarEditText = binding.commandBar
        val promptTextView = binding.promptText

        commandBarHintColor = if (resolvedIsDarkMode) context.getColor(R.color.hint_white) else context.getColor(R.color.hint_black)
        commandBarTextColor = if (resolvedIsDarkMode) context.getColor(white) else Color.BLACK
        val backgroundColor = if (resolvedIsDarkMode) R.color.command_bar_color_dark else white
        binding.commandBarLayout.backgroundTintList = ContextCompat.getColorStateList(context, backgroundColor)

        val promptTextStr = HintUtils.getPromptText(currentState, language, context, text)
        promptTextView.text = promptTextStr
        promptTextView.setTextColor(commandBarTextColor)
        promptTextView.setBackgroundColor(context.getColor(backgroundColor))

        if (currentState == ScribeState.SELECT_VERB_CONJUNCTION) {
            val verbInfinitive = currentVerbForConjugation ?: ""
            commandBarEditText.setText(": $verbInfinitive")
            commandBarEditText.setTextColor(commandBarTextColor)
            commandBarEditText.isFocusable = false
            commandBarEditText.isFocusableInTouchMode = false
        } else {
            currentCommandBarHint = HintUtils.getCommandBarHint(currentState, language, word)
            commandBarEditText.isFocusable = true
            commandBarEditText.isFocusableInTouchMode = true
            commandBarEditText.setTextColor(commandBarHintColor)
            setCommandBarTextWithCursor(currentCommandBarHint, cursorAtStart = true)
            commandBarEditText.requestFocus()
        }
    }

    /**
     * Initializes or re-initializes the keyboard with a new layout.
     *
     * @param xmlId The resource ID of the keyboard layout XML.
     */
    fun initializeKeyboard(xmlId: Int) {
        val enterKeyType = listener.getCurrentEnterKeyType()
        keyboard = KeyboardBase(context, xmlId, enterKeyType)
        keyboardView.setKeyboard(keyboard!!)
        keyboardView.mOnKeyboardActionListener = listener.onKeyboardActionListener()
        keyboardView.requestLayout()
    }

    /**
     * Sets up the currency symbol on the keyboard based on user preferences.
     *
     * @param language The current language.
     */
    fun setupCurrencySymbol(language: String) {
        val currencySymbol = PreferencesHelper.getDefaultCurrencySymbol(context, language)
        keyboardView.setKeyLabel(currencySymbol, "", KeyboardBase.CODE_CURRENCY)
    }

    /**
     * Retrieves and validates the stored index for the current conjugation view.
     * Ensures the index is within the bounds of available conjugation types.
     */
    private fun getValidatedConjugateIndex(conjugateOutput: Map<String, Any>?): Int {
        val prefs = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        var index = prefs.getInt("conjugate_index", 0)
        val maxIndex = conjugateOutput?.keys?.count()?.minus(1) ?: -1
        index = if (maxIndex >= 0) index.coerceIn(0, maxIndex) else 0
        prefs.edit { putInt("conjugate_index", index) }
        return index
    }

    // --- Helpers for suggestions and visibility ---

    /**
     * Updates the visibility of the suggestion buttons based on device type (phone/tablet)
     * and whether auto-suggestions are currently active.
     */
    fun updateButtonVisibility(
        currentState: ScribeState,
        isAutoSuggestEnabled: Boolean,
        autoSuggestEmojis: MutableList<String>?,
    ) {
        if (currentState != ScribeState.IDLE) {
            setupDefaultButtonVisibility()
            return
        }

        val isTablet =
            (context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE

        val emojiCount = if (isAutoSuggestEnabled) autoSuggestEmojis?.size ?: 0 else 0

        if (isTablet) updateTabletButtonVisibility(emojiCount) else updatePhoneButtonVisibility(emojiCount)
    }

    /**
     * Sets the default visibility for buttons when not in the `IDLE` state.
     * Hides all suggestion-related buttons.
     */
    private fun setupDefaultButtonVisibility() {
        pluralBtn?.visibility = View.VISIBLE
        emojiBtnPhone1?.visibility = View.GONE
        emojiBtnPhone2?.visibility = View.GONE
        emojiBtnTablet1?.visibility = View.GONE
        emojiBtnTablet2?.visibility = View.GONE
        emojiBtnTablet3?.visibility = View.GONE
        binding.separator4.visibility = View.GONE
        binding.separator5.visibility = View.GONE
        binding.separator6.visibility = View.GONE
    }

    /**
     * Handles the logic for showing/hiding suggestion buttons specifically on tablet layouts.
     *
     * @param emojiCount The number of available emoji suggestions.
     */
    private fun updateTabletButtonVisibility(emojiCount: Int) {
        pluralBtn?.visibility = if (emojiCount > 0) View.INVISIBLE else View.VISIBLE

        when (emojiCount) {
            0 -> {
                emojiBtnTablet1?.visibility = View.GONE
                emojiSpaceTablet1?.visibility = View.GONE
                emojiBtnTablet2?.visibility = View.GONE
                emojiSpaceTablet2?.visibility = View.GONE
                emojiBtnTablet3?.visibility = View.GONE
            }
            1 -> {
                emojiBtnTablet1?.visibility = View.VISIBLE
                emojiSpaceTablet1?.visibility = View.GONE
                emojiBtnTablet2?.visibility = View.GONE
                emojiSpaceTablet2?.visibility = View.GONE
                emojiBtnTablet3?.visibility = View.GONE
            }
            2 -> {
                emojiBtnTablet1?.visibility = View.VISIBLE
                emojiSpaceTablet1?.visibility = View.VISIBLE
                emojiBtnTablet2?.visibility = View.VISIBLE
                emojiSpaceTablet2?.visibility = View.GONE
                emojiBtnTablet3?.visibility = View.GONE
            }
            else -> {
                emojiBtnTablet1?.visibility = View.VISIBLE
                emojiSpaceTablet1?.visibility = View.VISIBLE
                emojiBtnTablet2?.visibility = View.VISIBLE
                emojiSpaceTablet2?.visibility = View.VISIBLE
                emojiBtnTablet3?.visibility = View.VISIBLE
            }
        }

        binding.separator5.visibility = View.GONE
        binding.separator6.visibility = View.GONE
        emojiBtnPhone1?.visibility = View.GONE
        emojiSpacePhone?.visibility = View.GONE
        emojiBtnPhone2?.visibility = View.GONE
        binding.separator4.visibility = View.GONE
    }

    /**
     * Handles the logic for showing/hiding suggestion buttons specifically on phone layouts.
     *
     * @param emojiCount The number of available emoji suggestions.
     */
    private fun updatePhoneButtonVisibility(emojiCount: Int) {
        pluralBtn?.visibility = if (emojiCount > 0) View.INVISIBLE else View.VISIBLE

        when {
            emojiCount == 1 -> {
                emojiBtnPhone1?.visibility = View.VISIBLE
                emojiSpacePhone?.visibility = View.GONE
                emojiBtnPhone2?.visibility = View.GONE
            }
            emojiCount >= 2 -> {
                emojiBtnPhone1?.visibility = View.VISIBLE
                emojiSpacePhone?.visibility = View.VISIBLE
                emojiBtnPhone2?.visibility = View.VISIBLE
            }
            else -> {
                emojiBtnPhone1?.visibility = View.GONE
                emojiSpacePhone?.visibility = View.GONE
                emojiBtnPhone2?.visibility = View.GONE
            }
        }

        binding.separator4.visibility = if (emojiCount > 1) View.VISIBLE else View.GONE

        emojiBtnTablet1?.visibility = View.GONE
        emojiSpaceTablet1?.visibility = View.GONE
        emojiBtnTablet2?.visibility = View.GONE
        emojiSpaceTablet2?.visibility = View.GONE
        emojiBtnTablet3?.visibility = View.GONE
        binding.separator5.visibility = View.GONE
        binding.separator6.visibility = View.GONE
    }

    /**
     * Updates the text of the suggestion buttons, primarily for displaying emoji suggestions.
     *
     * @param currentState The current state of the keyboard.
     * @param isAutoSuggestEnabled true if suggestions are active.
     * @param autoSuggestEmojis The list of emojis to display.
     */
    fun updateEmojiSuggestion(
        currentState: ScribeState,
        isAutoSuggestEnabled: Boolean,
        autoSuggestEmojis: MutableList<String>?,
    ) {
        if (currentState != ScribeState.IDLE) return

        val tabletButtons = listOf(binding.emojiBtnTablet1, binding.emojiBtnTablet2, binding.emojiBtnTablet3)
        val phoneButtons = listOf(binding.emojiBtnPhone1, binding.emojiBtnPhone2)

        if (isAutoSuggestEnabled && autoSuggestEmojis != null) {
            val emojiListener = { emoji: String ->
                View.OnClickListener { listener.onEmojiSelected(emoji) }
            }

            tabletButtons.forEachIndexed { index, button ->
                val emoji = autoSuggestEmojis.getOrNull(index) ?: ""
                button.text = emoji
                button.setOnClickListener(if (emoji.isNotEmpty()) emojiListener(emoji) else null)
            }

            phoneButtons.forEachIndexed { index, button ->
                val emoji = autoSuggestEmojis.getOrNull(index) ?: ""
                button.text = emoji
                button.setOnClickListener(if (emoji.isNotEmpty()) emojiListener(emoji) else null)
            }
        } else {
            (tabletButtons + phoneButtons).forEach { button ->
                button.text = ""
                button.setOnClickListener(null)
            }
        }
    }

    /**
     * Disables all auto-suggestions and resets the suggestion buttons to their default, inactive state.
     */
    fun disableAutoSuggest(language: String) {
        binding.translateBtnRight.visibility = View.INVISIBLE
        binding.translateBtnLeft.visibility = View.INVISIBLE
        binding.translateBtn.visibility = View.VISIBLE

        val createSuggestionClickListener = { suggestion: String ->
            View.OnClickListener { listener.onSuggestionClicked(suggestion) }
        }

        val suggestions = HintUtils.getBaseAutoSuggestions(language)

        val suggestion1 = suggestions.getOrNull(0) ?: ""
        binding.translateBtn.text = suggestion1
        binding.translateBtn.background = null
        binding.translateBtn.setOnClickListener(createSuggestionClickListener(suggestion1))

        val suggestion2 = suggestions.getOrNull(1) ?: ""
        binding.conjugateBtn.text = suggestion2
        binding.conjugateBtn.setOnClickListener(createSuggestionClickListener(suggestion2))

        val suggestion3 = suggestions.getOrNull(2) ?: ""
        binding.pluralBtn.text = suggestion3
        binding.pluralBtn.setOnClickListener(createSuggestionClickListener(suggestion3))

        handleTextSizeForSuggestion(binding.translateBtn)
    }

    /**
     * Sets the text size and color for a default, non-active suggestion button.
     *
     * @param button The button to style.
     */
    private fun handleTextSizeForSuggestion(button: Button) {
        button.textSize = GeneralKeyboardIME.SUGGESTION_SIZE
        val isUserDarkMode = getIsDarkModeOrNot(context)
        val colorRes = if (isUserDarkMode) R.color.white else android.R.color.black
        button.setTextColor(ContextCompat.getColor(context, colorRes))
    }

    /**
     * Sets the command bar text and ensures it ends with the custom cursor.
     *
     * @param text The text to set (without cursor).
     * @param cursorAtStart The flag to check if the text in the EditText is empty to determine the position of the cursor.
     */
    internal fun setCommandBarTextWithCursor(
        text: String,
        cursorAtStart: Boolean = false,
    ) {
        if (cursorAtStart) {
            val hintWithCursor = GeneralKeyboardIME.CUSTOM_CURSOR + text
            val spannable = SpannableString(hintWithCursor)
            spannable.setSpan(
                ForegroundColorSpan(commandBarTextColor),
                0,
                1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            binding.commandBar.setText(spannable, TextView.BufferType.SPANNABLE)
        } else {
            val textWithCursor = text + GeneralKeyboardIME.CUSTOM_CURSOR
            binding.commandBar.setText(textWithCursor)
        }
        binding.commandBar.setSelection(binding.commandBar.text.length)
    }

    /**
     * Gets the current text in the command bar without the cursor.
     *
     * @return The text content without the trailing cursor character.
     */
    internal fun getCommandBarTextWithoutCursor(): String {
        val currentText = binding.commandBar.text.toString()
        return when {
            currentText.startsWith(GeneralKeyboardIME.CUSTOM_CURSOR) -> currentText.drop(1)
            currentText.endsWith(GeneralKeyboardIME.CUSTOM_CURSOR) -> currentText.dropLast(1)
            else -> currentText
        }
    }

    /**
     * Show information about Wikidata when the user clicks the information icon.
     */
    private fun showInvalidInfo() {
        binding.ivInfo.isClickable = true
        binding.ivInfo.isFocusable = true
        keyboardView.visibility = View.GONE
        binding.invalidInfoBar.visibility = View.VISIBLE
        setupWikidataButtons()
        updateWikidataPage()
    }

    private fun setupWikidataButtons() {
        binding.buttonLeft.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateWikidataPage()
            }
        }
        binding.buttonRight.setOnClickListener {
            if (currentPage < totalPages - 1) {
                currentPage++
                updateWikidataPage()
            }
        }
    }

    /**
     * Update Wikidata information based on current navigation state.
     */
    private fun updateWikidataPage() {
        binding.middleTextview.setText(explanationStrings[currentPage])
        updateDotIndicators()
    }

    /**
     * Update page indicators to show which Wikidata explanation the user is currently viewing.
     */
    private fun updateDotIndicators() {
        val pageIndicators = binding.pageIndicators
        for (i in 0 until pageIndicators.childCount) {
            val dot = pageIndicators.getChildAt(i)
            dot.background =
                ContextCompat.getDrawable(
                    context,
                    if (i == currentPage) R.drawable.dot_active else R.drawable.dot_inactive,
                )
        }
    }
}
