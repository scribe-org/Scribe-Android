// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.util.Xml
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import androidx.annotation.XmlRes
import be.scri.R
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import kotlin.math.roundToInt

@Suppress("LongMethod", "NestedBlockDepth", "CyclomaticComplexMethod")
class KeyboardBase {
    interface KeyboardContextProvider {
        val language: String
        val keyboardMode: Int
        val keyboardLetters: Int

        fun isSearchBar(): Boolean

        fun isFloatingModeActive(): Boolean
    }

    private var mDefaultHorizontalGap = 0

    private var mDefaultWidth = 0

    private var mDefaultHeight = 0

    var mShiftState = SHIFT_OFF

    var mHeight = 0

    var mMinWidth = 0

    private var mIsConjugateLayout = false

    var mKeys: MutableList<Key?>? = null

    private var mDisplayWidth = 0

    var mEnterKeyType = IME_ACTION_NONE

    private val mRows = ArrayList<Row?>()

    companion object {
        private const val TAG_KEYBOARD = "Keyboard"
        private const val TAG_ROW = "Row"
        private const val TAG_KEY = "Key"
        private const val EDGE_LEFT = 0x01
        private const val EDGE_RIGHT = 0x02
        private const val WIDTH_DIVIDER = 10
        const val KEYCODE_SHIFT = -1
        const val KEYCODE_MODE_CHANGE = -2
        const val KEYCODE_FLOAT_TOGGLE = -10
        const val KEYCODE_ENTER = -4
        const val KEYCODE_DELETE = -5
        const val KEYCODE_SPACE = 32
        const val KEYCODE_EMOJI = -6
        const val KEYCODE_TAB = -30
        const val KEYCODE_CAPS_LOCK = -50
        const val KEYCODE_LEFT_ARROW = -55
        const val KEYCODE_RIGHT_ARROW = -56
        const val KEYCODE_CLIPBOARD = -60
        const val SHIFT_OFF = 0
        const val SHIFT_ON = 1
        const val SHIFT_ON_PERMANENT = 2
        const val SHIFT_LOCKED = 2
        const val DISPLAY_LEFT = 2002
        const val DISPLAY_RIGHT = 2001
        const val CODE_FPS = 1001
        const val CODE_FPP = 1002
        const val CODE_SPS = 1003
        const val CODE_SPP = 1004
        const val CODE_TPS = 1005
        const val CODE_TPP = 1006
        const val CODE_TR = 1011
        const val CODE_TL = 1012
        const val CODE_BR = 1013
        const val CODE_BL = 1014
        const val CODE_1X1 = 1041
        const val CODE_1X3_LEFT = 1021
        const val CODE_1X3_CENTER = 1022
        const val CODE_1X3_RIGHT = 1023
        const val CODE_2X1_TOP = 1031
        const val CODE_2X1_BOTTOM = 1032
        const val CODE_CURRENCY = 1050
        private const val MAX_KEYS_PER_MINI_ROW = 10

        val NAVIGATION_KEYS =
            setOf(
                KEYCODE_LEFT_ARROW,
                KEYCODE_RIGHT_ARROW,
            )

        val SCRIBE_VIEW_KEYS =
            setOf(
                DISPLAY_LEFT,
                DISPLAY_RIGHT,
                CODE_FPS,
                CODE_FPP,
                CODE_SPS,
                CODE_SPP,
                CODE_TPS,
                CODE_TPP,
                CODE_TR,
                CODE_TL,
                CODE_BR,
                CODE_BL,
                CODE_1X1,
                CODE_1X3_LEFT,
                CODE_1X3_CENTER,
                CODE_1X3_RIGHT,
                CODE_2X1_TOP,
                CODE_2X1_BOTTOM,
            )

        fun getDimensionOrFraction(
            a: TypedArray,
            index: Int,
            base: Int,
            defValue: Int,
        ): Int {
            val value = a.peekValue(index) ?: return defValue
            return when (value.type) {
                TypedValue.TYPE_DIMENSION -> a.getDimensionPixelOffset(index, defValue)
                TypedValue.TYPE_FRACTION -> a.getFraction(index, base, base, defValue.toFloat()).roundToInt()
                else -> defValue
            }
        }
    }

    class Row {
        var defaultWidth = 0

        var defaultHeight = 0

        var defaultHorizontalGap = 0

        var mKeys = ArrayList<Key>()

        var parent: KeyboardBase

        constructor(parent: KeyboardBase) {
            this.parent = parent
        }

        constructor(res: Resources, parent: KeyboardBase, parser: XmlResourceParser?, context: Context) {
            this.parent = parent
            val a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.KeyboardBase)
            defaultWidth =
                getDimensionOrFraction(
                    a,
                    R.styleable.KeyboardBase_keyWidth,
                    parent.mDisplayWidth,
                    parent.mDefaultWidth,
                )

            val resources = Resources.getSystem()
            val sharedPreferences = context.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
            val conjugateMode = sharedPreferences.getString("conjugate_mode_type", "2x1")
            defaultHeight =
                if (parent.mIsConjugateLayout && conjugateMode != "none") {
                    when (conjugateMode) {
                        "2x2" -> res.getDimension(R.dimen.conjugate_view_key_height_2x2).toInt()
                        "3x3" -> res.getDimension(R.dimen.conjugate_view_key_height_3x3).toInt()
                        "2x1" -> res.getDimension(R.dimen.conjugate_view_key_height_2x1).toInt()
                        else -> res.getDimension(R.dimen.conjugate_view_key_height_3x3).toInt()
                    }
                } else {
                    Log.i("≠", "The current state is not conjugate view")
                    when (resources.configuration.orientation) {
                        Configuration.ORIENTATION_LANDSCAPE -> {
                            res.getDimension(R.dimen.key_height_landscape).toInt()
                        }

                        Configuration.ORIENTATION_PORTRAIT -> {
                            res.getDimension(R.dimen.key_height).toInt()
                        }

                        else -> {
                            res.getDimension(R.dimen.key_height).toInt()
                        }
                    }
                }

            defaultHorizontalGap =
                getDimensionOrFraction(
                    a,
                    R.styleable.KeyboardBase_horizontalGap,
                    parent.mDisplayWidth,
                    parent.mDefaultHorizontalGap,
                )
            a.recycle()
        }
    }

    class Key(
        parent: Row,
    ) {
        var code = 0

        var label: CharSequence = ""

        var topSmallNumber: String = ""

        var icon: Drawable? = null

        var width: Int

        var height: Int

        var gap: Int

        var x = 0

        var y = 0

        var pressed = false

        var focused = false

        var popupCharacters: CharSequence? = null

        private var edgeFlags = 0

        private val keyboard = parent.parent

        var popupResId = 0

        var repeatable = false

        constructor(res: Resources, parent: Row, x: Int, y: Int, parser: XmlResourceParser?) : this(parent) {
            this.x = x
            this.y = y
            var a =
                res.obtainAttributes(
                    Xml.asAttributeSet(parser),
                    R.styleable.KeyboardBase,
                )

            width =
                getDimensionOrFraction(
                    a,
                    R.styleable.KeyboardBase_keyWidth,
                    keyboard.mDisplayWidth,
                    parent.defaultWidth,
                )

            height = parent.defaultHeight

            gap =
                getDimensionOrFraction(
                    a,
                    R.styleable.KeyboardBase_horizontalGap,
                    keyboard.mDisplayWidth,
                    parent.defaultHorizontalGap,
                )
            this.x += gap

            a.recycle()
            a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.KeyboardBase_Key)
            code = a.getInt(R.styleable.KeyboardBase_Key_code, 0)

            popupCharacters = a.getText(R.styleable.KeyboardBase_Key_popupCharacters)
            popupResId = a.getResourceId(R.styleable.KeyboardBase_Key_popupKeyboard, 0)
            repeatable = a.getBoolean(R.styleable.KeyboardBase_Key_isRepeatable, false)
            edgeFlags = a.getInt(R.styleable.KeyboardBase_Key_keyEdgeFlags, 0)
            icon = a.getDrawable(R.styleable.KeyboardBase_Key_keyIcon)
            icon?.setBounds(0, 0, icon!!.intrinsicWidth, icon!!.intrinsicHeight)

            label = a.getText(R.styleable.KeyboardBase_Key_keyLabel) ?: ""
            topSmallNumber = a.getString(R.styleable.KeyboardBase_Key_topSmallNumber) ?: ""

            if (label.isNotEmpty() && code == 0) {
                code = label[0].code
            }

            a.recycle()
        }

        init {
            height = parent.defaultHeight
            width = parent.defaultWidth
            gap = parent.defaultHorizontalGap
        }

        fun isInside(
            x: Int,
            y: Int,
        ): Boolean {
            val leftEdge = edgeFlags and EDGE_LEFT > 0
            val rightEdge = edgeFlags and EDGE_RIGHT > 0
            return (
                (x >= this.x || leftEdge && x <= this.x + width) &&
                    (x < this.x + width || rightEdge && x >= this.x) &&
                    (y >= this.y && y <= this.y + height) &&
                    (y < this.y + height && y >= this.y)
            )
        }
    }

    @JvmOverloads
    constructor(
        context: Context,
        @XmlRes xmlLayoutResId: Int,
        enterKeyType: Int,
        customWidth: Int? = null,
    ) {
        mDisplayWidth = customWidth ?: context.resources.displayMetrics.widthPixels
        mDefaultHorizontalGap = 0
        mDefaultWidth = mDisplayWidth / WIDTH_DIVIDER
        mDefaultHeight = mDefaultWidth
        mKeys = ArrayList()
        mEnterKeyType = enterKeyType
        mIsConjugateLayout =
            runCatching {
                context.resources.getResourceEntryName(xmlLayoutResId).startsWith("conjugate_view")
            }.getOrDefault(false)
        loadKeyboard(context, context.resources.getXml(xmlLayoutResId))
    }

    constructor(context: Context, layoutTemplateResId: Int, characters: CharSequence, keyWidth: Int) :
        this(context, layoutTemplateResId, 0) {
        var x = 0
        var y = 0
        var column = 0
        mMinWidth = 0
        val row = Row(this)
        row.defaultHeight = mDefaultHeight
        row.defaultWidth = keyWidth
        row.defaultHorizontalGap = mDefaultHorizontalGap

        characters.forEachIndexed { index, character ->
            val key = Key(row)
            if (column >= MAX_KEYS_PER_MINI_ROW) {
                column = 0
                x = 0
                y += mDefaultHeight
                mRows.add(row)
                row.mKeys.clear()
            }

            key.x = x
            key.y = y
            key.label = character.toString()
            key.code = character.code
            column++
            x += key.width + key.gap
            mKeys!!.add(key)
            row.mKeys.add(key)
            if (x > mMinWidth) {
                mMinWidth = x
            }
        }

        mHeight = y + mDefaultHeight
        mRows.add(row)
    }

    fun setShifted(shiftState: Int): Boolean {
        if (mShiftState != shiftState) {
            mShiftState =
                when (shiftState) {
                    SHIFT_ON_PERMANENT -> SHIFT_LOCKED
                    else -> shiftState and 0x1
                }
            return true
        }
        return false
    }

    private fun createRowFromXml(
        res: Resources,
        parser: XmlResourceParser?,
        context: Context,
    ): Row = Row(res, this, parser, context = context)

    private fun createKeyFromXml(
        res: Resources,
        parent: Row,
        x: Int,
        y: Int,
        parser: XmlResourceParser?,
    ): Key = Key(res, parent, x, y, parser)

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadKeyboard(
        context: Context,
        parser: XmlResourceParser,
    ) {
        var inKey = false
        var inRow = false
        var row = 0
        var x = 0
        var y = 0
        var key: Key? = null
        var currentRow: Row? = null
        val res = context.resources

        val provider = context as? KeyboardContextProvider
        val language = provider?.language
        val currentKeyboardMode = provider?.keyboardMode
        val keyboardLettersMode = provider?.keyboardLetters

        val isSearchBar = provider?.isSearchBar() == true
        val periodAndCommaEnabled: Boolean =
            if (language != null) {
                PreferencesHelper.getEnablePeriodAndCommaABC(context, language)
            } else {
                true
            }

        val hideComma =
            isSearchBar &&
                !periodAndCommaEnabled &&
                currentKeyboardMode == keyboardLettersMode

        var widthToRedistribute = 0
        var rowToAdjust: Row? = null

        try {
            var event: Int
            while (parser.next().also { event = it } != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    when (parser.name) {
                        TAG_ROW -> {
                            inRow = true
                            x = 0
                            currentRow = createRowFromXml(res, parser, context)
                            mRows.add(currentRow)
                        }

                        TAG_KEY -> {
                            inKey = true
                            key = createKeyFromXml(res, currentRow!!, x, y, parser)

                            if (hideComma && key.code == ','.code) {
                                widthToRedistribute = key.width + key.gap
                                rowToAdjust = currentRow

                                key.width = 0
                                key.gap = 0
                            }

                            mKeys!!.add(key)
                            if (key.code == KEYCODE_ENTER) {
                                val enterResourceId =
                                    when (mEnterKeyType) {
                                        EditorInfo.IME_ACTION_SEARCH ->
                                            R.drawable.ic_search_vector

                                        EditorInfo.IME_ACTION_NEXT,
                                        EditorInfo.IME_ACTION_GO,
                                        ->
                                            R.drawable.ic_arrow_right_vector

                                        EditorInfo.IME_ACTION_SEND ->
                                            R.drawable.ic_send_vector

                                        MyCustomActions.IME_ACTION_COMMAND ->
                                            R.drawable.play_button

                                        else ->
                                            R.drawable.ic_enter_vector
                                    }
                                key.icon = context.resources.getDrawable(enterResourceId, context.theme)
                            }
                            currentRow.mKeys.add(key)
                        }

                        TAG_KEYBOARD -> {
                            parseKeyboardAttributes(res, parser)
                        }
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        inKey = false
                        x += key!!.gap + key.width
                        if (x > mMinWidth) {
                            mMinWidth = x
                        }
                    } else if (inRow) {
                        inRow = false
                        y += currentRow!!.defaultHeight
                        row++
                    }
                }
            }
        } catch (e: XmlPullParserException) {
            Log.e("KeyboardBase", "XML Parsing error: ${e.message}")
        } catch (e: IOException) {
            Log.e("KeyboardBase", "I/O error: ${e.message}")
        }

        if (rowToAdjust != null && widthToRedistribute > 0) {
            val spaceKey = rowToAdjust.mKeys.find { it.code == KEYCODE_SPACE }
            spaceKey?.let {
                it.width += widthToRedistribute

                val spaceKeyIndex = rowToAdjust.mKeys.indexOf(it)
                if (spaceKeyIndex != -1) {
                    for (i in (spaceKeyIndex + 1) until rowToAdjust.mKeys.size) {
                        val prevKey = rowToAdjust.mKeys[i - 1]
                        val currentKey = rowToAdjust.mKeys[i]
                        currentKey.x = prevKey.x + prevKey.width + prevKey.gap
                    }
                }
            }
        }

        mHeight = y
    }

    private fun parseKeyboardAttributes(
        res: Resources,
        parser: XmlResourceParser,
    ) {
        val a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.KeyboardBase)
        val keyWidthResId = R.styleable.KeyboardBase_keyWidth
        val defaultWidth = mDisplayWidth / WIDTH_DIVIDER
        mDefaultWidth = getDimensionOrFraction(a, keyWidthResId, mDisplayWidth, defaultWidth)
        mDefaultHeight = res.getDimension(R.dimen.key_height).toInt()
        mDefaultHorizontalGap = getDimensionOrFraction(a, R.styleable.KeyboardBase_horizontalGap, mDisplayWidth, 0)
        a.recycle()
    }

    object MyCustomActions {
        const val IME_ACTION_COMMAND = 0x00000008
    }
}
