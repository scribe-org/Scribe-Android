/**
 * The base keyboard view for Scribe language keyboards application.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package be.scri.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.PopupWindow
import android.widget.TextView
import be.scri.R
import be.scri.databinding.KeyboardPopupKeyboardBinding
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.databinding.KeyboardViewKeyboardBinding
import be.scri.extensions.adjustAlpha
import be.scri.extensions.applyColorFilter
import be.scri.extensions.beGoneIf
import be.scri.extensions.config
import be.scri.extensions.darkenColor
import be.scri.extensions.getContrastColor
import be.scri.extensions.getProperBackgroundColor
import be.scri.extensions.getProperKeyColor
import be.scri.extensions.getProperPrimaryColor
import be.scri.extensions.getProperTextColor
import be.scri.extensions.getStrokeColor
import be.scri.extensions.performHapticFeedback
import be.scri.helpers.KeyboardBase
import be.scri.helpers.KeyboardBase.Companion.KEYCODE_DELETE
import be.scri.helpers.KeyboardBase.Companion.KEYCODE_ENTER
import be.scri.helpers.KeyboardBase.Companion.KEYCODE_MODE_CHANGE
import be.scri.helpers.KeyboardBase.Companion.KEYCODE_SHIFT
import be.scri.helpers.KeyboardBase.Companion.KEYCODE_SPACE
import be.scri.helpers.MAX_KEYS_PER_MINI_ROW
import be.scri.helpers.SHIFT_OFF
import be.scri.helpers.SHIFT_ON_ONE_CHAR
import be.scri.helpers.SHIFT_ON_PERMANENT
import java.util.Arrays
import java.util.Locale

@SuppressLint("UseCompatLoadingForDrawables")
@Suppress("LargeClass", "LongMethod", "TooManyFunctions", "NestedBlockDepth", "CyclomaticComplexMethod")
class KeyboardView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleRes: Int = 0,
    ) : View(context, attrs, defStyleRes) {
        interface OnKeyboardActionListener {
            /**
             * Called when the user presses a key. This is sent before the [.onKey] is called.
             * For keys that repeat, this is only called once.
             * @param primaryCode the unicode of the key being pressed.
             * If the touch is not on a valid key, the value will be zero.
             */
            fun onPress(primaryCode: Int)

            /**
             * Send a key press to the listener.
             * @param code this is the key that was pressed
             */
            fun onKey(code: Int)

            /**
             * Called when the finger has been lifted after pressing a key
             */
            fun onActionUp()

            /**
             * Called when the user long presses Space and moves to the left
             */
            fun moveCursorLeft()

            /**
             * Called when the user long presses Space and moves to the right
             */
            fun moveCursorRight()

            /**
             * Sends a sequence of characters to the listener.
             * @param text the string to be displayed.
             */
            fun onText(text: String)

            fun hasTextBeforeCursor(): Boolean

            fun commitPeriodAfterSpace()
        }

        private var mKeyboard: KeyboardBase? = null
        private var mCurrentKeyIndex: Int = NOT_A_KEY

        private var mLabelTextSize = 0
        private var mKeyTextSize = 0

        private var mTextColor = 0
        private var mBackgroundColor = 0
        private var mPrimaryColor = 0
        private var mKeyColor = 0

        private var mPreviewText: TextView? = null
        private val mPreviewPopup: PopupWindow
        private var mPreviewTextSizeLarge = 0
        private var mPreviewHeight = 0

        private val mCoordinates = IntArray(2)
        private val mPopupKeyboard: PopupWindow
        private var mMiniKeyboardContainer: View? = null
        private var mMiniKeyboard: KeyboardView? = null
        private var mMiniKeyboardOnScreen = false
        private var mPopupParent: View
        private var mMiniKeyboardOffsetX = 0
        private var mMiniKeyboardOffsetY = 0
        private val mMiniKeyboardCache: MutableMap<KeyboardBase.Key, View?>
        private var mKeys = ArrayList<KeyboardBase.Key>()
        private var mMiniKeyboardSelectedKeyIndex = -1

        var mOnKeyboardActionListener: OnKeyboardActionListener? = null
        private var mVerticalCorrection = 0
        private var mProximityThreshold = 0
        private var mPopupPreviewX = 0
        private var mPopupPreviewY = 0
        private var mLastX = 0
        private var mLastY = 0

        private val mPaint: Paint
        private var mDownTime = 0L
        private var mLastMoveTime = 0L
        private var mLastKey = 0
        private var mLastCodeX = 0
        private var mLastCodeY = 0
        private var mCurrentKey: Int = NOT_A_KEY
        private var mLastKeyTime = 0L
        private var mCurrentKeyTime = 0L
        private val mKeyIndices = IntArray(NUMBER_OF_KEYS)
        private var mPopupX = 0
        private var mPopupY = 0
        private var mRepeatKeyIndex = NOT_A_KEY
        private var mPopupLayout = 0
        private var mAbortKey = false
        private var mIsLongPressingSpace = false
        private var mLastSpaceMoveX = 0
        private var mPopupMaxMoveDistance = 0f
        private var mTopSmallNumberSize = 0f
        private var mTopSmallNumberMarginWidth = 0f
        private var mTopSmallNumberMarginHeight = 0f
        private val mSpaceMoveThreshold: Int
        private var ignoreTouches = false

        private var mEnterKeyColor: Int = 0

        private var mSpecialKeyColor: Int? = null

        private var mKeyBackground: Drawable? = null

        private var mToolbarHolder: View? = null

        // For multi-tap.
        private var mLastTapTime = 0L

        /** Whether the keyboard bitmap needs to be redrawn before it's blitted.  */
        private var mDrawPending = false

        /** The dirty region in the keyboard bitmap  */
        private val mDirtyRect = Rect()

        /** The keyboard bitmap for faster updates  */
        private var mBuffer: Bitmap? = null

        /** Notes if the keyboard just changed, so that we could possibly reallocate the mBuffer.  */
        private var mKeyboardChanged = false

        /** The canvas for the above mutable keyboard bitmap  */
        private var mCanvas: Canvas? = null

        /** The accessibility manager for accessibility support  */
        private val mAccessibilityManager: AccessibilityManager

        private var mHandler: Handler? = null

        private var lastSpaceBarTapTime = 0L

        private var mKeyboardBackgroundColor = 0

        private val alpha = FULL_ALPHA
        private val redDark = (DARK_COLOR_FACTOR * FULL_ALPHA).toInt()
        private val greenDark = (DARK_COLOR_FACTOR * FULL_ALPHA).toInt()
        private val blueDark = (DARK_COLOR_FACTOR * FULL_ALPHA).toInt()
        private val darkSpecialKey = Color.argb(FULL_ALPHA, redDark, greenDark, blueDark)

        private val red = (LIGHT_COLOR_RED_FACTOR * FULL_ALPHA).toInt()
        private val green = (LIGHT_COLOR_GREEN_FACTOR * FULL_ALPHA).toInt()
        private val blue = (LIGHT_COLOR_BLUE_FACTOR * FULL_ALPHA).toInt()
        private val lightSpecialKey = Color.argb(alpha, red, green, blue)

        companion object {
            private val LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout()
            private val LONG_PRESSABLE_STATE_SET = intArrayOf(R.attr.state_long_pressable)
            private const val NOT_A_KEY = -1
            private const val MSG_REMOVE_PREVIEW = 1
            private const val MSG_REPEAT = 2
            private const val MSG_LONGPRESS = 3
            private const val DELAY_AFTER_PREVIEW = 100
            private const val DEBOUNCE_TIME = 70
            private const val REPEAT_INTERVAL = 50 // ~20 keys per second
            private const val REPEAT_START_DELAY = 400
            private const val DOUBLE_TAP_DELAY = 300L
            private const val NUMBER_OF_KEYS = 12
            private const val FULL_ALPHA = 255
            private const val DARK_COLOR_FACTOR = 0.180
            private const val LIGHT_COLOR_RED_FACTOR = 0.682
            private const val LIGHT_COLOR_GREEN_FACTOR = 0.702
            private const val LIGHT_COLOR_BLUE_FACTOR = 0.745
            private const val DEFAULT_KEY_TEXT_SIZE = 18
            private const val MARGIN_ADJUSTMENT = 10
            private const val PROXIMITY_SCALING_FACTOR = 1.4f
            private const val KEY_MARGIN = 8
            private const val SHADOW_OFFSET = 3
            private const val ALPHA_ADJUSTMENT_FACTOR = 0.8f
            private const val SHADOW_ALPHA = 100
            private const val KEY_PADDING = 5
            private const val RECT_RADIUS = 15f
            private const val SHADOW_OFFSET_Y = 9f
            private const val POPUP_OFFSET_MULTIPLIER = 2.5
            private const val EXTRA_DELAY = 200L
        }

        private var _keyboardCommandBinding: KeyboardViewCommandOptionsBinding? = null
        val keyboardCommandBinding: KeyboardViewCommandOptionsBinding
            get() {
                if (_keyboardCommandBinding == null) {
                    _keyboardCommandBinding = KeyboardViewCommandOptionsBinding.inflate(LayoutInflater.from(context))
                }
                return _keyboardCommandBinding!!
            }
        private var _popupBinding: KeyboardPopupKeyboardBinding? = null
        val popupBinding: KeyboardPopupKeyboardBinding
            get() {
                if (_popupBinding == null) {
                    _popupBinding = KeyboardPopupKeyboardBinding.inflate(LayoutInflater.from(context))
                }
                return _popupBinding!!
            }

        fun setEnterKeyColor(
            color: Int? = null,
            isDarkMode: Boolean? = null,
        ) {
            if (color != null) {
                mEnterKeyColor = color
                invalidateAllKeys()
            } else {
                when (isDarkMode) {
                    true -> {
                        mEnterKeyColor = darkSpecialKey
                        invalidateAllKeys()
                    }
                    else -> {
                        mEnterKeyColor = lightSpecialKey
                        invalidateAllKeys()
                    }
                }
            }
        }

        private var _keyboardBinding: KeyboardViewKeyboardBinding? = null
        val keyboardBinding: KeyboardViewKeyboardBinding
            get() {
                if (_keyboardBinding == null) {
                    _keyboardBinding = KeyboardViewKeyboardBinding.inflate(LayoutInflater.from(context))
                }
                return _keyboardBinding!!
            }

        init {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView, 0, defStyleRes)
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val keyTextSize = 0
            val indexCnt = attributes.indexCount

            try {
                for (i in 0 until indexCnt) {
                    when (val attr = attributes.getIndex(i)) {
                        R.styleable.KeyboardView_keyTextSize -> {
                            mKeyTextSize = attributes.getDimensionPixelSize(attr, DEFAULT_KEY_TEXT_SIZE)
                        }
                    }
                }
            } finally {
                attributes.recycle()
            }

            mPopupLayout = R.layout.keyboard_popup_keyboard
            mKeyBackground = resources.getDrawable(R.drawable.keyboard_key_selector, context.theme)
            mVerticalCorrection = resources.getDimension(R.dimen.vertical_correction).toInt()
            mLabelTextSize = resources.getDimension(R.dimen.label_text_size).toInt()
            mPreviewHeight = resources.getDimension(R.dimen.key_height).toInt()
            mSpaceMoveThreshold = resources.getDimension(R.dimen.medium_margin).toInt()
            mTextColor = context.getProperTextColor()
            mBackgroundColor = context.getProperBackgroundColor()
            mPrimaryColor = context.getProperPrimaryColor()
            mKeyColor = context.getProperKeyColor()

            mPreviewPopup = PopupWindow(context)
            mPreviewText = inflater.inflate(resources.getLayout(R.layout.keyboard_key_preview), null) as TextView
            mPreviewTextSizeLarge = context.resources.getDimension(R.dimen.preview_text_size).toInt()
            mPreviewPopup.contentView = mPreviewText
            mPreviewPopup.setBackgroundDrawable(null)

            mPreviewPopup.isTouchable = false
            mPopupKeyboard = PopupWindow(context)
            mPopupKeyboard.setBackgroundDrawable(null)
            mPopupParent = this
            mPaint = Paint()
            mPaint.isAntiAlias = true
            mPaint.textSize = keyTextSize.toFloat()
            mPaint.textAlign = Align.CENTER
            mPaint.alpha = FULL_ALPHA
            mMiniKeyboardCache = HashMap()
            mAccessibilityManager = (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager)
            mPopupMaxMoveDistance = resources.getDimension(R.dimen.popup_max_move_distance)
            mTopSmallNumberSize = resources.getDimension(R.dimen.small_text_size)
            mTopSmallNumberMarginWidth = resources.getDimension(R.dimen.top_small_number_margin_width)
            mTopSmallNumberMarginHeight = resources.getDimension(R.dimen.top_small_number_margin_height)
        }

        @SuppressLint("HandlerLeak")
        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (mHandler == null) {
                mHandler =
                    object : Handler() {
                        override fun handleMessage(msg: Message) {
                            when (msg.what) {
                                MSG_REMOVE_PREVIEW -> mPreviewText!!.visibility = INVISIBLE
                                MSG_REPEAT ->
                                    if (repeatKey(false)) {
                                        val repeat = Message.obtain(this, MSG_REPEAT)
                                        sendMessageDelayed(repeat, REPEAT_INTERVAL.toLong())
                                    }
                                MSG_LONGPRESS -> openPopupIfRequired(msg.obj as MotionEvent)
                            }
                        }
                    }
            }
        }

        override fun onVisibilityChanged(
            changedView: View,
            visibility: Int,
        ) {
            super.onVisibilityChanged(changedView, visibility)

            if (visibility == VISIBLE) {
                mTextColor = context.getProperTextColor()
                mBackgroundColor = context.getProperBackgroundColor()
                mPrimaryColor = context.getProperPrimaryColor()
                val strokeColor = context.getStrokeColor()

                val toolbarColor =
                    if (context.config.isUsingSystemTheme) {
                        resources.getColor(R.color.you_keyboard_toolbar_color, context.theme)
                    } else {
                        resources.getColor(R.color.you_keyboard_toolbar_color, context.theme)
                    }

                val darkerColor =
                    if (context.config.isUsingSystemTheme) {
                        resources.getColor(R.color.you_keyboard_background_color, context.theme)
                    } else {
                        mBackgroundColor
                    }

                val miniKeyboardBackgroundColor =
                    if (context.config.isUsingSystemTheme) {
                        resources.getColor(R.color.you_keyboard_toolbar_color, context.theme)
                    } else {
                        mBackgroundColor
                    }

                if (changedView == popupBinding.miniKeyboardView) {
                    val previewBackground = background as LayerDrawable

                    previewBackground
                        .findDrawableByLayerId(R.id.button_background_shape)
                        .applyColorFilter(miniKeyboardBackgroundColor)

                    previewBackground
                        .findDrawableByLayerId(R.id.button_background_stroke)
                        .applyColorFilter(strokeColor)

                    background = previewBackground
                } else {
                    background.applyColorFilter(darkerColor)
                }

                val wasDarkened = mBackgroundColor != mBackgroundColor.darkenColor()
                mToolbarHolder?.apply {
                    keyboardBinding.apply {
                        topKeyboardDivider.beGoneIf(wasDarkened)
                        topKeyboardDivider.background = ColorDrawable(strokeColor)

                        background = ColorDrawable(toolbarColor)
                    }
                }
            }
        }

        /**
         * Attaches a keyboard to this view.
         * The keyboard can be switched at any time and the view will re-layout itself to accommodate the keyboard.
         * @param keyboard the keyboard to display in this view
         */
        fun setKeyboard(keyboard: KeyboardBase) {
            if (mKeyboard != null) {
                showPreview(NOT_A_KEY)
            }

            removeMessages()
            mKeyboard = keyboard
            val keys = mKeyboard!!.mKeys
            mKeys = keys!!.toMutableList() as ArrayList<KeyboardBase.Key>
            requestLayout()
            mKeyboardChanged = true
            invalidateAllKeys()
            computeProximityThreshold(keyboard)
            mMiniKeyboardCache.clear()
            // Not really necessary to do every time, but will free up views.
            // Switching to a different keyboard should abort any pending keys so that the key up
            // doesn't get delivered to the old or new keyboard.
            mAbortKey = true // until the next ACTION_DOWN
        }

        /** Sets the top row above the keyboard containing Scribe command buttons **/
        fun setKeyboardHolder() {
            mToolbarHolder = keyboardBinding.commandField

            mToolbarHolder?.let { toolbarHolder ->
                keyboardBinding.let { binding ->
                }
            }
        }

        fun vibrateIfNeeded() {
            if (context.config.vibrateOnKeypress) {
                performHapticFeedback()
            }
        }

        /**
         * Sets the state of the shift key of the keyboard, if any.
         * @param shifted whether or not to enable the state of the shift key
         * @return true if the shift key state changed, false if there was no change
         */
        private fun setShifted(shiftState: Int) {
            if (mKeyboard?.setShifted(shiftState) == true) {
                invalidateAllKeys()
            }
        }

        /**
         * Returns the state of the shift key of the keyboard, if any.
         * @return true if the shift is in a pressed state, false otherwise
         */
        private fun isShifted(): Boolean = mKeyboard?.mShiftState ?: SHIFT_OFF > SHIFT_OFF

        private fun setPopupOffset(
            x: Int,
            y: Int,
        ) {
            mMiniKeyboardOffsetX = x
            mMiniKeyboardOffsetY = y
            if (mPreviewPopup.isShowing) {
                mPreviewPopup.dismiss()
            }
        }

        private fun adjustCase(label: CharSequence?): CharSequence? =
            label?.takeIf { it.length in 1..2 }?.let {
                if (mKeyboard?.mShiftState?.let { state -> state > SHIFT_OFF } == true) {
                    label.toString().uppercase(Locale.getDefault())
                } else {
                    label
                }
            } ?: label

        public override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            if (mKeyboard == null) {
                setMeasuredDimension(0, 0)
            } else {
                var width = mKeyboard!!.mMinWidth
                if (MeasureSpec.getSize(widthMeasureSpec) < width + MARGIN_ADJUSTMENT) {
                    width = MeasureSpec.getSize(widthMeasureSpec)
                }
                setMeasuredDimension(width, mKeyboard!!.mHeight)
            }
        }

        /**
         * Compute the average distance between adjacent keys (horizontally and vertically)
         * and square it to get the proximity threshold.
         * We use a square here and in computing the touch distance from a key's center to avoid taking a square root.
         * @param keyboard
         */
        private fun computeProximityThreshold(keyboard: KeyboardBase?) {
            if (keyboard == null) {
                return
            }

            val keys = mKeys
            val length = keys.size
            var dimensionSum = 0
            for (i in 0 until length) {
                val key = keys[i]
                dimensionSum += Math.min(key.width, key.height) + key.gap
            }

            if (dimensionSum < 0 || length == 0) {
                return
            }

            mProximityThreshold = (dimensionSum * PROXIMITY_SCALING_FACTOR / length).toInt()
            mProximityThreshold *= mProximityThreshold // square it
        }

        public override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (mDrawPending || mBuffer == null || mKeyboardChanged) {
                onBufferDraw()
            }
            canvas.drawBitmap(mBuffer!!, 0f, 0f, null)
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun onBufferDraw() {
            val keyMargin = KEY_MARGIN
            val shadowOffset = SHADOW_OFFSET
            if (mBuffer == null || mKeyboardChanged) {
                if (mBuffer?.let { buffer -> buffer.width != width || buffer.height != height } != false) {
                    // Make sure our bitmap is at least 1x1.
                    val width = 1.coerceAtLeast(width)
                    val height = 1.coerceAtLeast(height)
                    mBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    mCanvas = Canvas(mBuffer!!)
                }
                invalidateAllKeys()
                mKeyboardChanged = false
            }

            if (mKeyboard == null) {
                return
            }

            mCanvas!!.save()
            val canvas = mCanvas
            canvas!!.clipRect(mDirtyRect)
            val paint = mPaint
            val keys = mKeys
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
            val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
            val keyBackgroundColor =
                if (isUserDarkMode) {
                    Color.DKGRAY
                } else {
                    Color.WHITE
                }
            mBackgroundColor =
                if (isUserDarkMode) {
                    Color.DKGRAY
                } else {
                    Color.WHITE
                }
            mTextColor =
                if (keyBackgroundColor == Color.WHITE) {
                    Color.BLACK
                } else {
                    Color.WHITE
                }
            mSpecialKeyColor =
                if (isUserDarkMode) {
                    R.color.special_key_dark
                } else {
                    R.color.special_key_light
                }
            paint.color = mTextColor
            val keyBackgroundPaint =
                Paint().apply {
                    color = keyBackgroundColor
                    style = Paint.Style.FILL
                }
            val smallLetterPaint =
                Paint().apply {
                    set(paint)
                    color = mTextColor.adjustAlpha(ALPHA_ADJUSTMENT_FACTOR)
                    textSize = mTopSmallNumberSize
                    typeface = Typeface.DEFAULT
                }
            val shadowPaint =
                Paint().apply {
                    color = Color.GRAY
                    alpha = SHADOW_ALPHA
                    style = Paint.Style.FILL
                }
            mKeyboardBackgroundColor =
                if (isUserDarkMode) {
                    Color.BLACK
                } else {
                    Color.LTGRAY
                }
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawColor(mKeyboardBackgroundColor)

            val keyCount = keys.size
            for (i in 0 until keyCount) {
                val key = keys[i]
                val code = key.code

                val padding = KEY_PADDING
                val rectRadius = RECT_RADIUS
                val shadowOffsetY = SHADOW_OFFSET_Y

                val shadowRect =
                    RectF(
                        (key.x + keyMargin + padding).toFloat(),
                        (key.y + keyMargin + padding + shadowOffsetY).toFloat(),
                        (key.x + key.width - keyMargin - padding).toFloat(),
                        (key.y + key.height - keyMargin - padding + shadowOffsetY).toFloat(),
                    )

                val keyRect =
                    RectF(
                        (key.x + keyMargin - shadowOffset + padding).toFloat(),
                        (key.y + keyMargin - shadowOffset + padding).toFloat(),
                        (key.x + key.width - keyMargin + shadowOffset - padding).toFloat(),
                        (key.y + key.height - keyMargin + shadowOffset - padding).toFloat(),
                    )
                canvas.drawRoundRect(shadowRect, rectRadius, rectRadius, shadowPaint)
                canvas.drawRoundRect(keyRect, rectRadius, rectRadius, keyBackgroundPaint)

                // Switch the character to uppercase if shift is pressed.
                val label = adjustCase(key.label)?.toString()

                if (key.focused || code == KEYCODE_ENTER) {
                    keyBackgroundPaint.color = Color.DKGRAY
                    canvas.drawRoundRect(keyRect, rectRadius, rectRadius, keyBackgroundPaint)
                    keyBackgroundPaint.color = mEnterKeyColor
                    canvas.drawRoundRect(keyRect, rectRadius, rectRadius, keyBackgroundPaint)
                    keyBackgroundPaint.color = keyBackgroundColor
                }

                if ((code == KEYCODE_DELETE || code == KEYCODE_SHIFT || code == KEYCODE_MODE_CHANGE)) {
                    keyBackgroundPaint.color = resources.getColor(mSpecialKeyColor!!)
                    canvas.drawRoundRect(keyRect, rectRadius, rectRadius, keyBackgroundPaint)
                    keyBackgroundPaint.color = keyBackgroundColor
                }

                canvas.translate(key.x.toFloat(), key.y.toFloat())
                if (label?.isNotEmpty() == true) {
                    // For characters, use large font. For labels like "Done", use small font.
                    if (label.length > 1) {
                        paint.textSize = mLabelTextSize.toFloat()
                        paint.typeface = Typeface.DEFAULT_BOLD
                    } else {
                        paint.textSize = mKeyTextSize.toFloat()
                        paint.typeface = Typeface.DEFAULT
                    }

                    paint.color =
                        if (key.focused) {
                            mPrimaryColor.getContrastColor()
                        } else {
                            mTextColor
                        }

                    canvas.drawText(
                        label,
                        (key.width / 2).toFloat(),
                        key.height / 2 + (paint.textSize - paint.descent()) / 2,
                        paint,
                    )

                    if (key.topSmallNumber.isNotEmpty()) {
                        canvas.drawText(
                            key.topSmallNumber,
                            key.width - mTopSmallNumberMarginWidth,
                            mTopSmallNumberMarginHeight,
                            smallLetterPaint,
                        )
                    }

                    // Turn off drop shadow.
                    paint.setShadowLayer(0f, 0f, 0f, 0)
                } else if (key.icon != null && mKeyboard != null) {
                    if (code == KEYCODE_SHIFT) {
                        val drawableId =
                            when (mKeyboard!!.mShiftState) {
                                SHIFT_OFF -> R.drawable.ic_caps_outline_vector
                                SHIFT_ON_ONE_CHAR -> R.drawable.ic_caps_vector
                                else -> R.drawable.ic_caps_underlined_vector
                            }
                        key.icon = resources.getDrawable(drawableId)
                    }

                    if (code == KEYCODE_ENTER) {
                        key.icon!!.applyColorFilter(mTextColor)
                    } else if (code == KEYCODE_DELETE || code == KEYCODE_SHIFT) {
                        key.icon!!.applyColorFilter(mTextColor)
                    }

                    // Controls where icons are located on their keys.
                    val drawableX = (key.width - key.icon!!.intrinsicWidth) / 2
                    val drawableY = (key.height - key.icon!!.intrinsicHeight) / 2
                    canvas.translate(drawableX.toFloat(), drawableY.toFloat())
                    key.icon!!.setBounds(0, 0, key.icon!!.intrinsicWidth, key.icon!!.intrinsicHeight)
                    key.icon!!.draw(canvas)
                    canvas.translate(-drawableX.toFloat(), -drawableY.toFloat())
                }
                canvas.translate(-key.x.toFloat(), -key.y.toFloat())
            }

            mCanvas!!.restore()
            mDrawPending = false
            mDirtyRect.setEmpty()
        }

        private fun getPressedKeyIndex(
            x: Int,
            y: Int,
        ): Int =
            mKeys.indexOfFirst {
                it.isInside(x, y)
            }

        private fun detectAndSendKey(
            index: Int,
            x: Int,
            y: Int,
            eventTime: Long,
        ) {
            if (index != NOT_A_KEY && index < mKeys.size) {
                val key = mKeys[index]
                getPressedKeyIndex(x, y)
                mOnKeyboardActionListener!!.onKey(key.code)
                mLastTapTime = eventTime
            }
        }

        private fun showPreview(keyIndex: Int) {
            if (!context.config.showPopupOnKeypress) {
                return
            }

            val oldKeyIndex = mCurrentKeyIndex
            val previewPopup = mPreviewPopup
            mCurrentKeyIndex = keyIndex
            // Release the old key and press the new key.
            val keys = mKeys
            if (oldKeyIndex != mCurrentKeyIndex) {
                if (oldKeyIndex != NOT_A_KEY && keys.size > oldKeyIndex) {
                    val oldKey = keys[oldKeyIndex]
                    oldKey.pressed = false
                    invalidateKey(oldKeyIndex)
                    val keyCode = oldKey.code
                    sendAccessibilityEventForUnicodeCharacter(
                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED,
                        keyCode,
                    )
                }

                if (mCurrentKeyIndex != NOT_A_KEY && keys.size > mCurrentKeyIndex) {
                    val newKey = keys[mCurrentKeyIndex]
                    val code = newKey.code
                    val pressedKeys =
                        listOf(
                            KEYCODE_SHIFT,
                            KEYCODE_MODE_CHANGE,
                            KEYCODE_DELETE,
                            KEYCODE_ENTER,
                            KEYCODE_SPACE,
                        )

                    newKey.pressed = code in pressedKeys

                    invalidateKey(mCurrentKeyIndex)
                    sendAccessibilityEventForUnicodeCharacter(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED, code)
                }
            }

            // If key changed and preview is on.
            if (oldKeyIndex != mCurrentKeyIndex) {
                if (previewPopup.isShowing) {
                    if (keyIndex == NOT_A_KEY) {
                        mHandler!!.sendMessageDelayed(
                            mHandler!!.obtainMessage(MSG_REMOVE_PREVIEW),
                            DELAY_AFTER_PREVIEW.toLong(),
                        )
                    }
                }

                if (keyIndex != NOT_A_KEY) {
                    showKey(keyIndex)
                }
            }
        }

        private fun showKey(keyIndex: Int) {
            val previewPopup = mPreviewPopup
            val keys = mKeys
            if (keyIndex < 0 || keyIndex >= mKeys.size) {
                return
            }

            val key = keys[keyIndex]
            if (key.icon != null) {
                mPreviewText!!.setCompoundDrawables(null, null, null, key.icon)
            } else {
                if (key.label.length > 1) {
                    mPreviewText!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, mKeyTextSize.toFloat())
                    mPreviewText!!.typeface = Typeface.DEFAULT_BOLD
                } else {
                    mPreviewText!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, mPreviewTextSizeLarge.toFloat())
                    mPreviewText!!.typeface = Typeface.DEFAULT
                }

                mPreviewText!!.setCompoundDrawables(null, null, null, null)
                try {
                    mPreviewText!!.text = adjustCase(key.label)
                } catch (ignored: Exception) {
                }
            }

            val previewBackgroundColor =
                if (context.config.isUsingSystemTheme) {
                    resources.getColor(R.color.you_keyboard_toolbar_color, context.theme)
                } else {
                    mBackgroundColor
                }

            val previewBackground = mPreviewText!!.background as LayerDrawable
            previewBackground
                .findDrawableByLayerId(R.id.button_background_shape)
                .applyColorFilter(previewBackgroundColor)

            previewBackground
                .findDrawableByLayerId(R.id.button_background_stroke)
                .applyColorFilter(context.getStrokeColor())

            mPreviewText!!.background = previewBackground

            mPreviewText!!.setTextColor(mTextColor)
            mPreviewText!!.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            )
            val popupWidth = Math.max(mPreviewText!!.measuredWidth, key.width)
            val popupHeight = mPreviewHeight
            val lp = mPreviewText!!.layoutParams
            lp?.width = popupWidth
            lp?.height = popupHeight

            mPopupPreviewX = key.x
            mPopupPreviewY = key.y - popupHeight

            mHandler!!.removeMessages(MSG_REMOVE_PREVIEW)
            getLocationInWindow(mCoordinates)
            mCoordinates[0] += mMiniKeyboardOffsetX // offset may be zero
            mCoordinates[1] += mMiniKeyboardOffsetY // offset may be zero

            // Set the preview background state.
            mPreviewText!!.background.state =
                if (key.popupResId != 0) {
                    LONG_PRESSABLE_STATE_SET
                } else {
                    EMPTY_STATE_SET
                }

            mPopupPreviewX += mCoordinates[0]
            mPopupPreviewY += mCoordinates[1]

            // If the popup cannot be shown above the key, put it on the side.
            getLocationOnScreen(mCoordinates)
            if (mPopupPreviewY + mCoordinates[1] < 0) {
                // If the key you're pressing is on the left side of the keyboard, show the popup on
                // the right, offset by enough to see at least one key to the left/right.
                if (key.x + key.width <= width / 2) {
                    mPopupPreviewX += (key.width * POPUP_OFFSET_MULTIPLIER).toInt()
                } else {
                    mPopupPreviewX -= (key.width * POPUP_OFFSET_MULTIPLIER).toInt()
                }
                mPopupPreviewY += popupHeight
            }

            previewPopup.dismiss()

            if (key.label.isNotEmpty() && key.code != KEYCODE_MODE_CHANGE && key.code != KEYCODE_SHIFT) {
                previewPopup.width = popupWidth
                previewPopup.height = popupHeight
                previewPopup.showAtLocation(mPopupParent, Gravity.NO_GRAVITY, mPopupPreviewX, mPopupPreviewY)
                mPreviewText!!.visibility = VISIBLE
            }
        }

        private fun sendAccessibilityEventForUnicodeCharacter(
            eventType: Int,
            code: Int,
        ) {
            if (mAccessibilityManager.isEnabled) {
                val event = AccessibilityEvent.obtain(eventType)
                onInitializeAccessibilityEvent(event)
                val text: String =
                    when (code) {
                        KEYCODE_DELETE -> context.getString(R.string.keycode_delete)
                        KEYCODE_ENTER -> context.getString(R.string.keycode_enter)
                        KEYCODE_MODE_CHANGE -> context.getString(R.string.keycode_mode_change)
                        KEYCODE_SHIFT -> context.getString(R.string.keycode_shift)
                        else -> code.toChar().toString()
                    }
                event.text.add(text)
                mAccessibilityManager.sendAccessibilityEvent(event)
            }
        }

        /**
         * Requests a redraw of the entire keyboard.
         * Calling [.invalidate] is not sufficient because the keyboard renders the keys to an off-screen buffer and
         * an invalidate() only draws the cached buffer.
         */
        fun invalidateAllKeys() {
            mDirtyRect.union(0, 0, width, height)
            mDrawPending = true
            invalidate()
        }

        /**
         * Invalidates a key so that it will be redrawn on the next repaint.
         * Use this method if only one key is changing it's content. Any changes that
         * affect the position or size of the key may not be honored.
         * @param keyIndex the index of the key in the attached [KeyboardBase].
         */
        private fun invalidateKey(keyIndex: Int) {
            if (keyIndex < 0 || keyIndex >= mKeys.size) {
                return
            }

            val key = mKeys[keyIndex]
            mDirtyRect.union(
                key.x,
                key.y,
                key.x + key.width,
                key.y + key.height,
            )
            onBufferDraw()
            invalidate(
                key.x,
                key.y,
                key.x + key.width,
                key.y + key.height,
            )
        }

        private fun openPopupIfRequired(me: MotionEvent): Boolean {
            if (mPopupLayout == 0 || mCurrentKey !in mKeys.indices) {
                return false
            }

            val popupKey = mKeys[mCurrentKey]
            val result = onLongPress(popupKey, me)
            if (result) {
                mAbortKey = true
                showPreview(NOT_A_KEY)
            }

            return result
        }

        /**
         * Called when a key is long pressed.
         * By default this will open any popup keyboard associated with this key through the attributes
         * popupLayout and popupCharacters.
         * @param popupKey the key that was long pressed
         * @return true if the long press is handled, false otherwise.
         * Subclasses should call the method on the base class if the subclass doesn't wish to
         * handle the call.
         */
        private fun onLongPress(
            popupKey: KeyboardBase.Key,
            me: MotionEvent,
        ): Boolean {
            val popupKeyboardId = popupKey.popupResId
            if (popupKeyboardId != 0) {
                mMiniKeyboardContainer = mMiniKeyboardCache[popupKey]
                if (mMiniKeyboardContainer == null) {
                    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    mMiniKeyboardContainer = inflater.inflate(mPopupLayout, null)
                    mMiniKeyboard =
                        mMiniKeyboardContainer!!
                            .findViewById<View>(R.id.mini_keyboard_view)
                            as KeyboardView

                    mMiniKeyboard!!.mOnKeyboardActionListener =
                        object : OnKeyboardActionListener {
                            override fun onKey(code: Int) {
                                mOnKeyboardActionListener!!.onKey(code)
                                dismissPopupKeyboard()
                            }

                            override fun onPress(primaryCode: Int) {
                                mOnKeyboardActionListener!!.onPress(primaryCode)
                            }

                            override fun onActionUp() {
                                mOnKeyboardActionListener!!.onActionUp()
                            }

                            override fun moveCursorLeft() {
                                mOnKeyboardActionListener!!.moveCursorLeft()
                            }

                            override fun moveCursorRight() {
                                mOnKeyboardActionListener!!.moveCursorRight()
                            }

                            override fun onText(text: String) {
                                mOnKeyboardActionListener!!.onText(text)
                            }

                            override fun hasTextBeforeCursor(): Boolean =
                                mOnKeyboardActionListener!!
                                    .hasTextBeforeCursor()

                            override fun commitPeriodAfterSpace() {
                                mOnKeyboardActionListener!!.commitPeriodAfterSpace()
                            }
                        }

                    val keyboard =
                        if (popupKey.popupCharacters != null) {
                            KeyboardBase(context, popupKeyboardId, popupKey.popupCharacters!!, popupKey.width)
                        } else {
                            KeyboardBase(context, popupKeyboardId, 0)
                        }

                    mMiniKeyboard!!.setKeyboard(keyboard)
                    mPopupParent = this
                    mMiniKeyboardContainer!!.measure(
                        MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST),
                    )
                    mMiniKeyboardCache[popupKey] = mMiniKeyboardContainer
                } else {
                    mMiniKeyboard =
                        mMiniKeyboardContainer!!
                            .findViewById<View>(R.id.mini_keyboard_view) as KeyboardView
                }

                getLocationInWindow(mCoordinates)
                mPopupX = popupKey.x
                mPopupY = popupKey.y

                val widthToUse =
                    mMiniKeyboardContainer!!.measuredWidth -
                        (popupKey.popupCharacters!!.length / 2) *
                        popupKey.width
                mPopupX = mPopupX + popupKey.width - widthToUse
                mPopupY -= mMiniKeyboardContainer!!.measuredHeight
                val x = mPopupX + mCoordinates[0]
                val y = mPopupY + mCoordinates[1]
                val xOffset = Math.max(0, x)
                mMiniKeyboard!!.setPopupOffset(xOffset, y)

                // Make sure we highlight the proper key right after long pressing it,
                // before any ACTION_MOVE event occurs.
                val miniKeyboardX =
                    if (xOffset + mMiniKeyboard!!.measuredWidth <= measuredWidth) {
                        xOffset
                    } else {
                        measuredWidth - mMiniKeyboard!!.measuredWidth
                    }

                val keysCnt = mMiniKeyboard!!.mKeys.size
                var selectedKeyIndex = Math.floor((me.x - miniKeyboardX) / popupKey.width.toDouble()).toInt()
                if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                    selectedKeyIndex += MAX_KEYS_PER_MINI_ROW
                }
                selectedKeyIndex = Math.max(0, Math.min(selectedKeyIndex, keysCnt - 1))

                for (i in 0 until keysCnt) {
                    mMiniKeyboard!!.mKeys[i].focused = i == selectedKeyIndex
                }

                mMiniKeyboardSelectedKeyIndex = selectedKeyIndex
                mMiniKeyboard!!.invalidateAllKeys()

                val miniShiftStatus = if (isShifted()) SHIFT_ON_PERMANENT else SHIFT_OFF
                mMiniKeyboard!!.setShifted(miniShiftStatus)
                mPopupKeyboard.contentView = mMiniKeyboardContainer
                mPopupKeyboard.width = mMiniKeyboardContainer!!.measuredWidth
                mPopupKeyboard.height = mMiniKeyboardContainer!!.measuredHeight
                mPopupKeyboard.showAtLocation(this, Gravity.NO_GRAVITY, x, y)
                mMiniKeyboardOnScreen = true
                invalidateAllKeys()
                return true
            }
            return false
        }

        override fun onTouchEvent(me: MotionEvent): Boolean {
            val action = me.action

            if (ignoreTouches) {
                if (action == MotionEvent.ACTION_UP) {
                    ignoreTouches = false

                    // Fix a glitch with long pressing backspace, then clicking some letter.
                    if (mRepeatKeyIndex != NOT_A_KEY) {
                        val key = mKeys[mRepeatKeyIndex]
                        if (key.code == KEYCODE_DELETE) {
                            mHandler?.removeMessages(MSG_REPEAT)
                            mRepeatKeyIndex = NOT_A_KEY
                        }
                    }
                }
                return true
            }

            // Handle moving between alternative popup characters by swiping.
            if (mPopupKeyboard.isShowing) {
                when (action) {
                    MotionEvent.ACTION_MOVE -> {
                        if (mMiniKeyboard != null) {
                            val coords = intArrayOf(0, 0)
                            mMiniKeyboard!!.getLocationOnScreen(coords)
                            val keysCnt = mMiniKeyboard!!.mKeys.size
                            val lastRowKeyCount =
                                if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                                    Math.max(keysCnt % MAX_KEYS_PER_MINI_ROW, 1)
                                } else {
                                    keysCnt
                                }

                            val widthPerKey =
                                if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                                    mMiniKeyboard!!.width / MAX_KEYS_PER_MINI_ROW
                                } else {
                                    mMiniKeyboard!!.width / lastRowKeyCount
                                }

                            var selectedKeyIndex = Math.floor((me.x - coords[0]) / widthPerKey.toDouble()).toInt()
                            if (keysCnt > MAX_KEYS_PER_MINI_ROW) {
                                selectedKeyIndex = Math.max(0, selectedKeyIndex)
                                selectedKeyIndex += MAX_KEYS_PER_MINI_ROW
                            }

                            selectedKeyIndex = Math.max(0, Math.min(selectedKeyIndex, keysCnt - 1))
                            if (selectedKeyIndex != mMiniKeyboardSelectedKeyIndex) {
                                for (i in 0 until keysCnt) {
                                    mMiniKeyboard!!.mKeys[i].focused = i == selectedKeyIndex
                                }
                                mMiniKeyboardSelectedKeyIndex = selectedKeyIndex
                                mMiniKeyboard!!.invalidateAllKeys()
                            }

                            if (coords[0] > 0 || coords[1] > 0) {
                                if (coords[0] - me.x > mPopupMaxMoveDistance ||
                                    // left
                                    me.x - (coords[0] + mMiniKeyboard!!.measuredWidth) > mPopupMaxMoveDistance // right
                                ) {
                                    dismissPopupKeyboard()
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        mMiniKeyboard?.mKeys?.firstOrNull { it.focused }?.apply {
                            mOnKeyboardActionListener!!.onKey(code)
                        }
                        mMiniKeyboardSelectedKeyIndex = -1
                        dismissPopupKeyboard()
                    }
                }
            }

            return onModifiedTouchEvent(me)
        }

        private fun onModifiedTouchEvent(me: MotionEvent): Boolean {
            var touchX = me.x.toInt()
            var touchY = me.y.toInt()
            if (touchY >= -mVerticalCorrection) {
                touchY += mVerticalCorrection
            }

            var handled = false
            val action = me.actionMasked
            val eventTime = me.eventTime
            val keyIndex = getPressedKeyIndex(touchX, touchY)

            // Ignore all motion events until a DOWN.
            if (mAbortKey && action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_CANCEL) {
                handled = true
            }

            // Needs to be called after the gesture detector gets a turn, as it may have displayed the mini keyboard.
            if (mMiniKeyboardOnScreen && action != MotionEvent.ACTION_CANCEL) {
                return true
            }

            if (!handled) {
                when (action) {
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        // If the user presses a key while still holding down the previous,
                        // type in both chars and ignore the later gestures.
                        // Can happen at fast typing, easier to reproduce by increasing LONGPRESS_TIMEOUT.
                        ignoreTouches = true
                        mHandler!!.removeMessages(MSG_LONGPRESS)
                        dismissPopupKeyboard()
                        detectAndSendKey(keyIndex, touchX, touchY, eventTime)

                        val newPointerX = me.getX(1).toInt()
                        val newPointerY = me.getY(1).toInt()
                        val secondKeyIndex = getPressedKeyIndex(newPointerX, newPointerY)
                        showPreview(secondKeyIndex)
                        detectAndSendKey(secondKeyIndex, newPointerX, newPointerY, eventTime)

                        val secondKeyCode = mKeys.getOrNull(secondKeyIndex)?.code
                        secondKeyCode?.let { mOnKeyboardActionListener!!.onPress(it) }

                        showPreview(NOT_A_KEY)
                        invalidateKey(mCurrentKey)
                        handled = true
                    }
                    MotionEvent.ACTION_DOWN -> {
                        mAbortKey = false
                        mLastCodeX = touchX
                        mLastCodeY = touchY
                        mLastKeyTime = 0
                        mCurrentKeyTime = 0
                        mLastKey = NOT_A_KEY
                        mCurrentKey = keyIndex
                        mDownTime = eventTime
                        mLastMoveTime = eventTime

                        val onPressKey = if (keyIndex != NOT_A_KEY) mKeys[keyIndex].code else 0
                        mOnKeyboardActionListener!!.onPress(onPressKey)

                        if (mCurrentKey >= 0 && mKeys[mCurrentKey].repeatable) {
                            mRepeatKeyIndex = mCurrentKey
                            val msg = mHandler!!.obtainMessage(MSG_REPEAT)
                            mHandler!!.sendMessageDelayed(msg, REPEAT_START_DELAY.toLong())
                            // If the user long presses Space, move the cursor after swipine left/right.
                            if (mKeys[mCurrentKey].code == KEYCODE_SPACE) {
                                mLastSpaceMoveX = -1
                            } else {
                                repeatKey(true)
                            }

                            // Delivering the key could have caused an abort.
                            if (mAbortKey) {
                                mRepeatKeyIndex = NOT_A_KEY
                                handled = true
                            }
                        }

                        if (!handled && mCurrentKey != NOT_A_KEY) {
                            val msg = mHandler!!.obtainMessage(MSG_LONGPRESS, me)
                            mHandler!!.sendMessageDelayed(msg, LONGPRESS_TIMEOUT.toLong())
                        }

                        if (mPopupParent.id != R.id.mini_keyboard_view) {
                            showPreview(keyIndex)
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        var continueLongPress = false
                        if (keyIndex != NOT_A_KEY) {
                            if (mCurrentKey == NOT_A_KEY) {
                                mCurrentKey = keyIndex
                                mCurrentKeyTime = eventTime - mDownTime
                            } else {
                                if (keyIndex == mCurrentKey) {
                                    mCurrentKeyTime += eventTime - mLastMoveTime
                                    continueLongPress = true
                                } else if (mRepeatKeyIndex == NOT_A_KEY) {
                                    mLastKey = mCurrentKey
                                    mLastCodeX = mLastX
                                    mLastCodeY = mLastY
                                    mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                                    mCurrentKey = keyIndex
                                    mCurrentKeyTime = 0
                                }
                            }
                        }

                        if (mIsLongPressingSpace) {
                            if (mLastSpaceMoveX == -1) {
                                mLastSpaceMoveX = mLastX
                            }

                            val diff = mLastX - mLastSpaceMoveX
                            if (diff < -mSpaceMoveThreshold) {
                                for (i in diff / mSpaceMoveThreshold until 0) {
                                    mOnKeyboardActionListener?.moveCursorLeft()
                                }
                                mLastSpaceMoveX = mLastX
                            } else if (diff > mSpaceMoveThreshold) {
                                for (i in 0 until diff / mSpaceMoveThreshold) {
                                    mOnKeyboardActionListener?.moveCursorRight()
                                }
                                mLastSpaceMoveX = mLastX
                            }
                        } else if (!continueLongPress) {
                            // Cancel old longpress.
                            mHandler!!.removeMessages(MSG_LONGPRESS)
                            // Start new longpress if key has changed.
                            if (keyIndex != NOT_A_KEY) {
                                val msg = mHandler!!.obtainMessage(MSG_LONGPRESS, me)
                                mHandler!!.sendMessageDelayed(msg, LONGPRESS_TIMEOUT.toLong())
                            }

                            if (mPopupParent.id != R.id.mini_keyboard_view) {
                                showPreview(mCurrentKey)
                            }
                            mLastMoveTime = eventTime
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        mLastSpaceMoveX = 0
                        removeMessages()
                        if (keyIndex == mCurrentKey) {
                            mCurrentKeyTime += eventTime - mLastMoveTime
                        } else {
                            mLastKey = mCurrentKey
                            mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                            mCurrentKey = keyIndex
                            mCurrentKeyTime = 0
                        }

                        if (mCurrentKeyTime < mLastKeyTime &&
                            mCurrentKeyTime <
                            DEBOUNCE_TIME &&
                            mLastKey != NOT_A_KEY
                        ) {
                            mCurrentKey = mLastKey
                            touchX = mLastCodeX
                            touchY = mLastCodeY
                        }

                        showPreview(NOT_A_KEY)
                        Arrays.fill(mKeyIndices, NOT_A_KEY)
                        // If we're not on a repeating key (which sends on a DOWN event).
                        if (mRepeatKeyIndex == NOT_A_KEY && !mMiniKeyboardOnScreen && !mAbortKey) {
                            detectAndSendKey(mCurrentKey, touchX, touchY, eventTime)
                        }

                        if (mKeys.getOrNull(mCurrentKey)?.code == KEYCODE_SPACE && !mIsLongPressingSpace) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastSpaceBarTapTime < DOUBLE_TAP_DELAY + EXTRA_DELAY &&
                                context.config.periodOnDoubleTap &&
                                mOnKeyboardActionListener!!.hasTextBeforeCursor()
                            ) {
                                mOnKeyboardActionListener!!.commitPeriodAfterSpace()
                            } else {
                                detectAndSendKey(mCurrentKey, touchX, touchY, eventTime)
                            }
                            lastSpaceBarTapTime = currentTime
                        }

                        invalidateKey(keyIndex)
                        mRepeatKeyIndex = NOT_A_KEY
                        mOnKeyboardActionListener!!.onActionUp()
                        mIsLongPressingSpace = false
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        mIsLongPressingSpace = false
                        mLastSpaceMoveX = 0
                        removeMessages()
                        dismissPopupKeyboard()
                        mAbortKey = true
                        showPreview(NOT_A_KEY)
                        invalidateKey(mCurrentKey)
                    }
                }
            }

            mLastX = touchX
            mLastY = touchY

            return handled || true
        }

        private fun repeatKey(initialCall: Boolean): Boolean {
            val key = mKeys[mRepeatKeyIndex]
            if (!initialCall && key.code == KEYCODE_SPACE) {
                if (!mIsLongPressingSpace) {
                    vibrateIfNeeded()
                }

                mIsLongPressingSpace = true
            } else {
                detectAndSendKey(mCurrentKey, key.x, key.y, mLastTapTime)
            }
            return true
        }

        private fun closing() {
            if (mPreviewPopup.isShowing) {
                mPreviewPopup.dismiss()
            }
            removeMessages()
            dismissPopupKeyboard()
            mBuffer = null
            mCanvas = null
            mMiniKeyboardCache.clear()
        }

        private fun removeMessages() {
            mHandler?.apply {
                removeMessages(MSG_REPEAT)
                removeMessages(MSG_LONGPRESS)
            }
        }

        public override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            closing()
        }

        private fun dismissPopupKeyboard() {
            if (mPopupKeyboard.isShowing) {
                mPopupKeyboard.dismiss()
                mMiniKeyboardOnScreen = false
                invalidateAllKeys()
            }
        }
    }
