// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")

package be.scri.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import be.scri.R
import be.scri.databinding.ActivityConjugationBinding
import be.scri.repository.KeyboardLanguageCodes
import be.scri.ui.conjugation.ConjugationAdapter
import be.scri.ui.conjugation.ConjugationViewModel
import com.google.android.material.snackbar.Snackbar

class ConjugationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConjugationBinding
    private val viewModel: ConjugationViewModel by viewModels {
        androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
    private val listAdapter =
        ConjugationAdapter {
            Snackbar
                .make(binding.root, R.string.app_conjugate_copied, Snackbar.LENGTH_SHORT)
                .show()
        }

    private var suppressTenseSpinnerCallback = false

    private var languageSpinnerInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConjugationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.conjugationList.adapter = listAdapter

        val languages = KeyboardLanguageCodes.supportedDisplayLanguages
        val initialLanguage =
            intent.getStringExtra(EXTRA_DISPLAY_LANGUAGE)?.takeIf { it in languages }
                ?: languages.first()
        binding.languageSpinner.adapter =
            ArrayAdapter(
                this,
                R.layout.spinner_item_conjugation,
                languages,
            ).also { it.setDropDownViewResource(R.layout.spinner_dropdown_item_conjugation) }
        binding.languageSpinner.setSelection(languages.indexOf(initialLanguage).coerceAtLeast(0))
        binding.languageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long,
                ) {
                    if (!languageSpinnerInitialized) {
                        languageSpinnerInitialized = true
                        return
                    }
                    viewModel.resetResultsForLanguageChange(
                        getString(R.string.app_conjugate_filter_all),
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.searchPillButton.setOnClickListener { runSearch() }
        binding.clearVerbButton.setOnClickListener { binding.verbInput.text?.clear() }
        binding.verbInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {}

                override fun afterTextChanged(s: Editable?) {
                    binding.clearVerbButton.isVisible = !s.isNullOrBlank()
                }
            },
        )
        binding.verbInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                runSearch()
                true
            } else {
                false
            }
        }

        binding.tenseSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long,
                ) {
                    if (suppressTenseSpinnerCallback) return
                    viewModel.applyTenseFilter(
                        spinnerPosition = position,
                        allTensesLabel = getString(R.string.app_conjugate_filter_all),
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        viewModel.rows.observe(this) { rows ->
            listAdapter.submitList(rows)
        }

        viewModel.tenseOptions.observe(this) { options ->
            if (options.isEmpty()) return@observe
            suppressTenseSpinnerCallback = true
            binding.tenseSpinner.adapter =
                ArrayAdapter(
                    this,
                    R.layout.spinner_item_conjugation,
                    options,
                ).also { it.setDropDownViewResource(R.layout.spinner_dropdown_item_conjugation) }
            val pos = (viewModel.selectedTensePosition.value ?: 0).coerceIn(0, options.lastIndex)
            binding.tenseSpinner.setSelection(pos)
            suppressTenseSpinnerCallback = false
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.loadingIndicator.isVisible = loading == true
        }

        viewModel.verbNotFound.observe(this) { notFound ->
            if (notFound == true) {
                Snackbar
                    .make(
                        binding.root,
                        R.string.app_conjugate_browser_not_found,
                        Snackbar.LENGTH_LONG,
                    ).show()
                viewModel.consumeVerbNotFoundEvent()
            }
        }

        viewModel.loadError.observe(this) { failed ->
            if (failed == true) {
                Snackbar
                    .make(
                        binding.root,
                        R.string.app_conjugate_load_failed,
                        Snackbar.LENGTH_LONG,
                    ).show()
                viewModel.consumeLoadErrorEvent()
            }
        }
    }

    private fun runSearch() {
        val lemma = binding.verbInput.text?.toString().orEmpty()
        if (lemma.isBlank()) {
            Snackbar
                .make(binding.root, R.string.app_conjugate_browser_empty_query, Snackbar.LENGTH_SHORT)
                .show()
            return
        }
        val lang =
            binding.languageSpinner.selectedItem as? String
                ?: return
        viewModel.loadVerb(
            displayLanguage = lang,
            lemma = lemma,
            allTensesLabel = getString(R.string.app_conjugate_filter_all),
        )
    }

    companion object {
        const val EXTRA_DISPLAY_LANGUAGE = "extra_display_language"
    }
}
