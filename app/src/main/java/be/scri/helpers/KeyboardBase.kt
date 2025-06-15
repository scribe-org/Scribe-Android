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

/**
 * Loads an XML description of a keyboard and stores the attributes of the keys. A keyboard consists of rows of keys.
 * @attr ref android.R.styleable#Keyboard_keyWidth
 * @attr ref android.R.styleable#Keyboard_horizontalGap
 */
@Suppress("LongMethod", "NestedBlockDepth", "CyclomaticComplexMethod")
class KeyboardBase {
    /** Horizontal gap default for all rows  */
    private var mDefaultHorizontalGap = 0

    /** Default key width  */
    private var mDefaultWidth = 0

    /** Default key height  */
    private var mDefaultHeight = 0

    /** Is the keyboard in the shifted state  */
    var mShiftState = SHIFT_OFF

    /** Total height of the keyboard, including the padding and keys  */
    var mHeight = 0

    /** Total width of the keyboard, including left side gaps and keys, but not any gaps on the right side. */
    var mMinWidth = 0

    /** List of keys in this keyboard  */
    var mKeys: MutableList<Key?>? = null

    /** Width of the screen available to fit the keyboard  */
    private var mDisplayWidth = 0

    /** What icon should we show at Enter key  */
    var mEnterKeyType = IME_ACTION_NONE

    /** Keyboard rows  */
    private val mRows = ArrayList<Row?>()

    /**
     * Constants for keyboard layouts and the function to retrieve them.
     */
    companion object {
        private const val TAG_KEYBOARD = "Keyboard"
        private const val TAG_ROW = "Row"
        private const val TAG_KEY = "Key"
        private const val EDGE_LEFT = 0x01
        private const val EDGE_RIGHT = 0x02
        private const val WIDTH_DIVIDER = 10
        const val KEYCODE_SHIFT = -1
        const val KEYCODE_MODE_CHANGE = -2
        const val KEYCODE_ENTER = -4
        const val KEYCODE_DELETE = -5
        const val KEYCODE_SPACE = 32
        const val KEYCODE_TAB = -30
        const val KEYCODE_CAPS_LOCK = -50
        const val KEYCODE_LEFT_ARROW = -55
        const val KEYCODE_RIGHT_ARROW = -56
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
        private const val MAX_KEYS_PER_MINI_ROW = 10

        // Sets for grouping key codes to reduce complexity in KeyHandler.
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

        /**
         * Retrieves the dimension or fraction value from the attributes, adjusting the base value if necessary.
         *
         * @param a the TypedArray containing the attributes
         * @param index the index of the desired attribute
         * @param base the base value for the fraction calculation
         * @param defValue the default value to return if no valid dimension is found
         * @return the calculated dimension or fraction value
         */
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

    /**
     * Container for keys in the keyboard.
     * All keys in a row are at the same Y-coordinate.
     * Some of the key size defaults can be overridden per row from
     * what the [KeyboardBase] defines.
     * @attr ref android.R.styleable#Keyboard_keyWidth
     * @attr ref android.R.styleable#Keyboard_horizontalGap
     */
    class Row {
        /** Default width of a key in this row.  */
        var defaultWidth = 0

        /** Default height of a key in this row.  */
        var defaultHeight = 0

        /** Default horizontal gap between keys in this row.  */
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
                if (conjugateMode != "none") {
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

    /**
     * Class for describing the position and characteristics of a single key in the keyboard.
     *
     * @attr ref android.R.styleable#Keyboard_keyWidth
     * @attr ref android.R.styleable#Keyboard_keyHeight
     * @attr ref android.R.styleable#Keyboard_horizontalGap
     * @attr ref android.R.styleable#Keyboard_Key_codes
     * @attr ref android.R.styleable#Keyboard_Key_keyIcon
     * @attr ref android.R.styleable#Keyboard_Key_keyLabel
     * @attr ref android.R.styleable#Keyboard_Key_isRepeatable
     * @attr ref android.R.styleable#Keyboard_Key_popupKeyboard
     * @attr ref android.R.styleable#Keyboard_Key_popupCharacters
     * @attr ref android.R.styleable#Keyboard_Key_keyEdgeFlags
     */
    class Key(
        parent: Row,
    ) {
        /** Key code that this key generates.  */
        var code = 0

        /** Label to display  */
        var label: CharSequence = ""

        /** First row of letters can also be used for inserting numbers by long pressing them, show those numbers  */
        var topSmallNumber: String = ""

        /** Icon to display instead of a label. Icon takes precedence over a label  */
        var icon: Drawable? = null

        /** Width of the key, not including the gap  */
        var width: Int

        /** Height of the key, not including the gap  */
        var height: Int

        /** The horizontal gap before this key  */
        var gap: Int

        /** X coordinate of the key in the keyboard layout  */
        var x = 0

        /** Y coordinate of the key in the keyboard layout  */
        var y = 0

        /** The current pressed state of this key  */
        var pressed = false

        /** Focused state, used after long pressing a key and swiping to alternative keys  */
        var focused = false

        /** Popup characters showing after long pressing the key  */
        var popupCharacters: CharSequence? = null

        /**
         * Flags that specify the anchoring to edges of the keyboard for detecting touch events,
         * that are just out of the boundary of the key.
         * This is a bit mask of [KeyboardBase.EDGE_LEFT], [KeyboardBase.EDGE_RIGHT].
         */
        private var edgeFlags = 0

        /** The keyboard that this key belongs to  */
        private val keyboard = parent.parent

        /** If this key pops up a mini keyboard, this is the resource id for the XML layout for that keyboard.  */
        var popupResId = 0

        /** Whether this key repeats itself when held down  */
        var repeatable = false

        /** Create a key with the given top-left coordinate and extract its attributes from the XML parser.
         * @param res resources associated with the caller's context
         * @param parent the row that this key belongs to. The row must already be attached to a [KeyboardBase].
         * @param x the x coordinate of the top-left
         * @param y the y coordinate of the top-left
         * @param parser the XML parser containing the attributes for this key
         */
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

        /**
         * Detects if a point falls inside this key.
         * @param x the x-coordinate of the point
         * @param y the y-coordinate of the point
         * @return whether or not the point falls inside the key.
         * If the key is attached to an edge, it will assume that all points between the key and
         * the edge are considered to be inside the key.
         */
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

    /**
     * Creates a keyboard from the given xml key layout file.
     * Weeds out rows that have a keyboard mode defined but don't match the specified mode.
     * @param context the application or service context
     * @param xmlLayoutResId the resource file that contains the keyboard layout and keys.
     * @param enterKeyType determines what icon should we show on Enter key
     */
    @JvmOverloads
    constructor(
        context: Context,
        @XmlRes xmlLayoutResId: Int,
        enterKeyType: Int,
    ) {
        mDisplayWidth = context.resources.displayMetrics.widthPixels
        mDefaultHorizontalGap = 0
        mDefaultWidth = mDisplayWidth / WIDTH_DIVIDER
        mDefaultHeight = mDefaultWidth
        mKeys = ArrayList()
        mEnterKeyType = enterKeyType
        loadKeyboard(context, context.resources.getXml(xmlLayoutResId))
    }

    /**
     * Creates a blank keyboard from the given resource file and
     * populates it with the specified characters in left-to-right, top-to-bottom fashion,
     * using the specified number of columns. If the specified number of columns is -1,
     * then the keyboard will fit as many keys as possible in each row.
     * @param context the application or service context
     * @param layoutTemplateResId the layout template file, containing no keys.
     * @param characters the list of characters to display on the keyboard. One key will be created for each character.
     * @param keyWidth the width of the popup key, make sure it is the same as the key itself
     */
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

    /**
     * Sets the keyboard shift state.
     *
     * @param shiftState the new shift state to apply
     * @return true if the shift state was changed; false otherwise
     */
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

    /**
     * Creates a Row object from the XML resource parser.
     *
     * @param res the resources associated with the context
     * @param parser the XML resource parser
     * @return the created Row object
     */
    private fun createRowFromXml(
        res: Resources,
        parser: XmlResourceParser?,
        context: Context,
    ): Row = Row(res, this, parser, context = context)

    /**
     * Creates a Key object from the XML resource parser and the specified coordinates.
     *
     * @param res the resources associated with the context
     * @param parent the parent Row that this key belongs to
     * @param x the x-coordinate of the key
     * @param y the y-coordinate of the key
     * @param parser the XML resource parser
     * @return the created Key object
     */
    private fun createKeyFromXml(
        res: Resources,
        parent: Row,
        x: Int,
        y: Int,
        parser: XmlResourceParser?,
    ): Key = Key(res, parent, x, y, parser)

    /**
     * Loads the keyboard configuration from the provided XML parser, populating the rows and keys.
     * This method also handles edge cases like custom icons for the Enter key based on its type.
     *
     * @param context the application context
     * @param parser the XML resource parser
     */
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
        mHeight = y
    }

    /**
     * Parses the keyboard attributes such as key width, height, and horizontal gap from the XML resource.
     *
     * @param res the resources associated with the context
     * @param parser the XML resource parser
     */
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

    /**
     * Holds custom IME (Input Method Editor) action constants.
     */
    object MyCustomActions {
        const val IME_ACTION_COMMAND = 0x00000008
    }
}
