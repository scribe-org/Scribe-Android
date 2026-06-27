import re

path = 'app/src/keyboards/java/be/scri/services/GeneralKeyboardIME.kt'
s = open(path, encoding='utf-8').read()

replacements = {
    "uiManager.disableAutoSuggest(language)": "disableAutoSuggest(language)",
    
    """            uiManager.genderSuggestionLeft?.visibility = View.INVISIBLE
            uiManager.genderSuggestionRight?.visibility = View.INVISIBLE
            uiManager.binding.translateBtn.apply {
                visibility = View.VISIBLE
                text = "PL"
                textSize = NOUN_TYPE_SIZE
                background = ContextCompat.getDrawable(context, R.drawable.button_background_rounded)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.annotateOrange)
                setTextColor(ContextCompat.getColor(context, white))
                isClickable = false
                setOnClickListener(null)
            }""": """            keyboardViewModel.setGenderSuggestions(null, null)""",
            
    """        uiManager.genderSuggestionLeft?.visibility = View.INVISIBLE
        uiManager.genderSuggestionRight?.visibility = View.INVISIBLE
        uiManager.binding.translateBtn.textSize = NOUN_TYPE_SIZE

        uiManager.binding.translateBtn.apply {
            visibility = View.VISIBLE
            text = type?.uppercase(Locale.getDefault())

            if (colorRes != R.color.transparent) {
                background = ContextCompat.getDrawable(context, R.drawable.button_background_rounded)
                backgroundTintList = ContextCompat.getColorStateList(context, colorRes)
                setTextColor(ContextCompat.getColor(context, white))
            } else {
                background = null
                val isUserDarkMode = getIsDarkModeOrNot(applicationContext)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.transparent)
                setTextColor(ContextCompat.getColor(context, if (isUserDarkMode) white else android.R.color.black))
            }
        }""": """        keyboardViewModel.setGenderSuggestions(null, null)""",
        
    """        uiManager.genderSuggestionLeft?.visibility = View.VISIBLE
        uiManager.genderSuggestionRight?.visibility = View.VISIBLE
        uiManager.binding.translateBtn.visibility = View.INVISIBLE

        uiManager.genderSuggestionLeft?.let {
            applyInformativeSuggestionStyle(
                it,
                leftSuggestion.first,
                leftSuggestion.second,
                R.drawable.rounded_button_left_half,
            )
        }

        uiManager.genderSuggestionRight?.let {
            applyInformativeSuggestionStyle(
                it,
                rightSuggestion.first,
                rightSuggestion.second,
                R.drawable.rounded_button_right_half,
            )
        }""": """        keyboardViewModel.setGenderSuggestions(leftSuggestion.second, rightSuggestion.second)""",
        
    """        setSuggestionButton(uiManager.binding.conjugateBtn, suggestion1)

        when {
            hasLinguisticSuggestions && emojiCount != 0 -> {
                uiManager.updateButtonVisibility(currentState, true, autoSuggestEmojis)
            }
            hasLinguisticSuggestions && emojiCount == 0 -> {
                setSuggestionButton(uiManager.pluralBtn!!, suggestion2)
            }
            !hasLinguisticSuggestions && emojiCount != 0 -> {
                setSuggestionButton(uiManager.binding.translateBtn, suggestion2)
                uiManager.updateButtonVisibility(currentState, true, autoSuggestEmojis)
            }
            else -> {
                setSuggestionButton(uiManager.binding.translateBtn, suggestion2)
                setSuggestionButton(uiManager.pluralBtn!!, suggestion3)
            }
        }""": """        var sTranslate: String? = null
        var sConjugate: String? = suggestion1
        var sPlural: String? = null

        when {
            hasLinguisticSuggestions && emojiCount != 0 -> {
                // Emojis handled via State
            }
            hasLinguisticSuggestions && emojiCount == 0 -> {
                sPlural = suggestion2
            }
            !hasLinguisticSuggestions && emojiCount != 0 -> {
                sTranslate = suggestion2
            }
            else -> {
                sTranslate = suggestion2
                sPlural = suggestion3
            }
        }

        keyboardViewModel.setSuggestions(sTranslate, sConjugate, sPlural)""",
        
    """        setAutocompleteButton(uiManager.binding.conjugateBtn, completion1)
        setAutocompleteButton(uiManager.binding.translateBtn, completion2)
        setAutocompleteButton(uiManager.pluralBtn!!, completion3)

        uiManager.binding.separator1.visibility = View.VISIBLE
        uiManager.binding.separator2.visibility = View.VISIBLE""": """        keyboardViewModel.setSuggestions(completion1, completion2, completion3)"""
}

for k, v in replacements.items():
    if k in s:
        s = s.replace(k, v)
        print("Replaced chunk")
    else:
        print("NOT FOUND:", k[:50])

# Also need to append `disableAutoSuggest` implementation.
if "private fun disableAutoSuggest(language: String)" not in s:
    s = s.replace("}\n", """
    private fun disableAutoSuggest(language: String) {
        val suggestions = be.scri.helpers.HintUtils.getBaseAutoSuggestions(language)
        keyboardViewModel.setGenderSuggestions(null, null)
        
        if (isNumericKeyboardActive) {
            keyboardViewModel.setSuggestions(suggestions.getOrNull(0), null, null)
        } else {
            keyboardViewModel.setSuggestions(
                suggestions.getOrNull(0),
                suggestions.getOrNull(1),
                suggestions.getOrNull(2)
            )
        }
    }
}
""", 1) # Only replace the LAST brace!

# To replace the LAST brace securely:
s = s.rstrip()
if s.endswith("}"):
    s = s[:-1] + """
    private fun disableAutoSuggest(language: String) {
        val suggestions = be.scri.helpers.HintUtils.getBaseAutoSuggestions(language)
        keyboardViewModel.setGenderSuggestions(null, null)
        
        if (isNumericKeyboardActive) {
            keyboardViewModel.setSuggestions(suggestions.getOrNull(0), null, null)
        } else {
            keyboardViewModel.setSuggestions(
                suggestions.getOrNull(0),
                suggestions.getOrNull(1),
                suggestions.getOrNull(2)
            )
        }
    }
}
"""

open(path, 'w', encoding='utf-8').write(s)
