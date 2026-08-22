// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService.BACK_DISPOSITION_ADJUST_NOTHING
import android.inputmethodservice.InputMethodService.BACK_DISPOSITION_DEFAULT
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import be.scri.R
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot
import be.scri.services.GeneralKeyboardIME

/**
 * Manages floating keyboard state, resize handles, touch listeners, and layout transitions
 * for [GeneralKeyboardIME].
 *
 * @property ime The [GeneralKeyboardIME] instance this handler is associated with.
 */
class FloatingKeyboardHandler(
    private val ime: GeneralKeyboardIME,
) {
    var isFloatingMode: Boolean = false
        private set

    private var lastAppliedFloatingMode: Boolean? = null

    private var initialX = 0f
    private var initialY = 0f
    private var initialTranslationX = 0f
    private var initialTranslationY = 0f
    private var maxTranslationX = 0f
    private var minTranslationX = 0f
    private var minTranslationY = 0f
    private var maxTranslationY = 0f

    private val cornerHideHandler = Handler(Looper.getMainLooper())
    private val hideCornersRunnable = Runnable { animateHideCorners() }

    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialScaleX = 1.0f
    private var initialScaleY = 1.0f
    private var dragFactorX = 1f
    private var dragFactorY = 1f
    private var keyboardCenterX = 0f
    private var keyboardCenterY = 0f
    private var initialDistance = 0f
    private var isResizing = false

    fun initFloatingMode() {
        isFloatingMode = PreferencesHelper.getIsFloatingModeEnabled(ime, ime.language)
        lastAppliedFloatingMode = null
        applyFloatingModeState()
    }

    fun toggleFloatingMode() {
        isFloatingMode = !isFloatingMode
        PreferencesHelper.setIsFloatingModeEnabled(ime, ime.language, isFloatingMode)
        lastAppliedFloatingMode = null
        applyFloatingModeState()
        ime.window
            ?.window
            ?.decorView
            ?.requestLayout()
    }

    fun disableFloatingMode() {
        if (isFloatingMode) {
            isFloatingMode = false
            PreferencesHelper.setIsFloatingModeEnabled(ime, ime.language, isFloatingMode)
            lastAppliedFloatingMode = null
            applyFloatingModeState()
            ime.window
                ?.window
                ?.decorView
                ?.requestLayout()
        }
    }

    fun applyFloatingModeState() {
        if (!ime.isUiManagerInitialized) return
        val card = ime.binding.keyboardCard
        val dragBar = ime.binding.floatingDragBar
        val density = ime.resources.displayMetrics.density
        val root = ime.binding.root
        val win = ime.window?.window

        val modeChanged = lastAppliedFloatingMode != isFloatingMode
        lastAppliedFloatingMode = isFloatingMode

        val rootWidth = ViewGroup.LayoutParams.MATCH_PARENT
        val rootHeight = if (isFloatingMode) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        val rootParams = root.layoutParams ?: ViewGroup.LayoutParams(rootWidth, rootHeight)
        rootParams.width = rootWidth
        rootParams.height = rootHeight
        root.layoutParams = rootParams
        root.minimumHeight = 0

        val parentViewGroup = root.parent as? ViewGroup
        if (parentViewGroup != null) {
            val pParams = parentViewGroup.layoutParams
            if (pParams != null) {
                pParams.width = rootWidth
                pParams.height = rootHeight
                parentViewGroup.layoutParams = pParams
            }
        }

        if (isFloatingMode) {
            ime.setBackDisposition(BACK_DISPOSITION_ADJUST_NOTHING)
            win?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            win?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        } else {
            ime.setBackDisposition(BACK_DISPOSITION_DEFAULT)
            win?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            win?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        if (isFloatingMode) {
            val params = card.layoutParams
            if (params != null) {
                params.width = ime.getKeyboardWidth()
                card.layoutParams = params
            }

            val scaleFactorX = PreferencesHelper.getFloatingScaleX(ime, ime.language)
            val scaleFactorY = PreferencesHelper.getFloatingScaleY(ime, ime.language)
            card.scaleX = scaleFactorX
            card.scaleY = scaleFactorY
            card.alpha = 1.0f

            ime.binding.resizeHandleTopLeft.setOnTouchListener(resizeTouchListener)
            ime.binding.resizeHandleTopRight.setOnTouchListener(resizeTouchListener)
            ime.binding.resizeHandleBottomLeft.setOnTouchListener(resizeTouchListener)
            ime.binding.resizeHandleBottomRight.setOnTouchListener(resizeTouchListener)

            if (modeChanged) {
                ime.binding.resizeHandleTopLeft.visibility = View.GONE
                ime.binding.resizeHandleTopRight.visibility = View.GONE
                ime.binding.resizeHandleBottomLeft.visibility = View.GONE
                ime.binding.resizeHandleBottomRight.visibility = View.GONE
            }

            val isDarkMode = getIsDarkModeOrNot(ime)
            val kbBgColorRes = if (isDarkMode) R.color.dark_keyboard_bg_color else R.color.light_keyboard_bg_color
            val kbBgColor = ContextCompat.getColor(ime, kbBgColorRes)

            val floatingBg =
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f * density
                    setColor(kbBgColor)
                    setStroke((1f * density).toInt(), 0x40888888.toInt())
                }
            card.background = floatingBg
            card.elevation = 8f * density
            card.clipToOutline = true

            dragBar.setBackgroundColor(kbBgColor)
            val pillColor = if (isDarkMode) 0x4DFFFFFF.toInt() else 0x40000000.toInt()
            ime.binding.floatingDragHandle.setColorFilter(pillColor)

            dragBar.visibility = View.VISIBLE

            if (modeChanged) ime.recreateKeyboardPublic()

            card.post {
                disableParentClipping(root)
                var storedX = PreferencesHelper.getFloatingX(ime, ime.language)
                var storedY = PreferencesHelper.getFloatingY(ime, ime.language)
                val currentScaleX = PreferencesHelper.getFloatingScaleX(ime, ime.language)
                val currentScaleY = PreferencesHelper.getFloatingScaleY(ime, ime.language)

                if (storedY == 0f) storedY = 100f * density

                val screenWidth = ime.resources.displayMetrics.widthPixels
                val screenHeight = ime.resources.displayMetrics.heightPixels
                val cardWidth = card.width.toFloat()
                val cardHeight = card.height.toFloat()

                if (cardWidth > 0f && cardHeight > 0f) {
                    val maxTranslationX = (screenWidth - cardWidth * currentScaleX) / 2f
                    val minTranslationX = -maxTranslationX

                    val minTranslationY = 0f
                    val maxTranslationY = screenHeight.toFloat() - cardHeight * currentScaleY

                    val targetX = storedX.coerceInSafe(minTranslationX, maxTranslationX)
                    val targetY = storedY.coerceInSafe(minTranslationY, maxTranslationY)

                    updateFloatingViewsPosition(targetX, targetY, currentScaleX, currentScaleY)

                    val attr = win?.attributes
                    if (attr != null) {
                        attr.gravity = Gravity.TOP or Gravity.START
                        attr.x = 0
                        attr.y = 0
                        attr.width = ViewGroup.LayoutParams.MATCH_PARENT
                        attr.height = ViewGroup.LayoutParams.MATCH_PARENT
                        win.attributes = attr
                    }
                    root.requestLayout()
                }
            }
        } else {
            val params = card.layoutParams
            if (params != null) {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                card.layoutParams = params
            }

            card.scaleX = 1.0f
            card.scaleY = 1.0f
            card.alpha = 1.0f

            val isDarkMode = getIsDarkModeOrNot(ime)
            val kbBgColorRes = if (isDarkMode) R.color.dark_keyboard_bg_color else R.color.light_keyboard_bg_color
            card.background = ColorDrawable(ContextCompat.getColor(ime, kbBgColorRes))
            card.elevation = 0f
            card.clipToOutline = false

            dragBar.visibility = View.GONE

            if (modeChanged) ime.recreateKeyboardPublic()

            card.translationX = 0f
            card.translationY = 0f

            ime.binding.resizeHandleTopLeft.translationX = 0f
            ime.binding.resizeHandleTopLeft.translationY = 0f
            ime.binding.resizeHandleTopRight.translationX = 0f
            ime.binding.resizeHandleTopRight.translationY = 0f
            ime.binding.resizeHandleBottomLeft.translationX = 0f
            ime.binding.resizeHandleBottomLeft.translationY = 0f
            ime.binding.resizeHandleBottomRight.translationX = 0f
            ime.binding.resizeHandleBottomRight.translationY = 0f

            ime.binding.resizeHandleTopLeft.visibility = View.GONE
            ime.binding.resizeHandleTopRight.visibility = View.GONE
            ime.binding.resizeHandleBottomLeft.visibility = View.GONE
            ime.binding.resizeHandleBottomRight.visibility = View.GONE

            val attr = win?.attributes
            if (attr != null) {
                attr.gravity = Gravity.BOTTOM
                attr.x = 0
                attr.y = 0
                attr.width = ViewGroup.LayoutParams.MATCH_PARENT
                attr.height = ViewGroup.LayoutParams.WRAP_CONTENT
                win.attributes = attr
            }

            card.post {
                card.translationX = 0f
                card.translationY = 0f
                root.requestLayout()
            }
        }
        ime.applyNavBarColor()
    }

    private fun updateFloatingViewsPosition(
        targetX: Float,
        targetY: Float,
        scaleX: Float,
        scaleY: Float,
    ) {
        val card = ime.binding.keyboardCard
        val screenHeight =
            ime.resources.displayMetrics.heightPixels
                .toFloat()

        val cardWidth = card.width.toFloat()
        val cardHeight = card.height.toFloat()
        if (cardWidth == 0f || cardHeight == 0f) return

        card.scaleX = scaleX
        card.scaleY = scaleY

        val transX = targetX
        val transY = (screenHeight - cardHeight * scaleY) / 2f - targetY

        card.translationX = transX
        card.translationY = transY

        val scaleOffsetX = scaleX - 1.0f
        val scaleOffsetY = scaleY - 1.0f
        val halfW = cardWidth / 2f
        val halfH = cardHeight / 2f

        ime.binding.resizeHandleTopLeft.translationX = transX - halfW * scaleOffsetX
        ime.binding.resizeHandleTopLeft.translationY = transY - halfH * scaleOffsetY

        ime.binding.resizeHandleTopRight.translationX = transX + halfW * scaleOffsetX
        ime.binding.resizeHandleTopRight.translationY = transY - halfH * scaleOffsetY

        ime.binding.resizeHandleBottomLeft.translationX = transX - halfW * scaleOffsetX
        ime.binding.resizeHandleBottomLeft.translationY = transY + halfH * scaleOffsetY

        ime.binding.resizeHandleBottomRight.translationX = transX + halfW * scaleOffsetX
        ime.binding.resizeHandleBottomRight.translationY = transY + halfH * scaleOffsetY
    }

    fun disableParentClipping(view: View) {
        var p = view.parent
        while (p is ViewGroup) {
            p.clipChildren = false
            p.clipToPadding = false
            p = p.parent
        }
    }

    private fun showCorners() {
        cornerHideHandler.removeCallbacks(hideCornersRunnable)

        val corners =
            listOf(
                ime.binding.resizeHandleTopLeft,
                ime.binding.resizeHandleTopRight,
                ime.binding.resizeHandleBottomLeft,
                ime.binding.resizeHandleBottomRight,
            )

        for (corner in corners) {
            corner.animate().cancel()
            corner.alpha = 1f
            corner.visibility = View.VISIBLE
        }
    }

    private fun startHideCornersTimer() {
        cornerHideHandler.removeCallbacks(hideCornersRunnable)
        cornerHideHandler.postDelayed(hideCornersRunnable, 3000)
    }

    private fun animateHideCorners() {
        val corners =
            listOf(
                ime.binding.resizeHandleTopLeft,
                ime.binding.resizeHandleTopRight,
                ime.binding.resizeHandleBottomLeft,
                ime.binding.resizeHandleBottomRight,
            )

        for (corner in corners) {
            corner
                .animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    corner.visibility = View.GONE
                }.start()
        }
    }

    private fun applyScaleAndPosition(
        scaleX: Float,
        scaleY: Float,
    ) {
        val card = ime.binding.keyboardCard
        val screenHeight =
            ime.resources.displayMetrics.heightPixels
                .toFloat()
        val cardHeight = card.height.toFloat()

        val liveX = card.translationX
        val liveTransY = card.translationY
        val prevScaleY = card.scaleY
        val liveY = (screenHeight - cardHeight * prevScaleY) / 2f - liveTransY

        updateFloatingViewsPosition(liveX, liveY, scaleX, scaleY)
    }

    private val resizeTouchListener =
        View.OnTouchListener { view, event ->
            if (!isFloatingMode) return@OnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isResizing = true
                    showCorners()

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    initialScaleX = PreferencesHelper.getFloatingScaleX(ime, ime.language)
                    initialScaleY = PreferencesHelper.getFloatingScaleY(ime, ime.language)

                    val viewId = view.id
                    dragFactorX =
                        when (viewId) {
                            R.id.resize_handle_top_left -> -1f
                            R.id.resize_handle_bottom_left -> -1f
                            R.id.resize_handle_top_right -> 1f
                            R.id.resize_handle_bottom_right -> 1f
                            else -> 1f
                        }
                    dragFactorY =
                        when (viewId) {
                            R.id.resize_handle_top_left -> -1f
                            R.id.resize_handle_top_right -> -1f
                            R.id.resize_handle_bottom_left -> 1f
                            R.id.resize_handle_bottom_right -> 1f
                            else -> 1f
                        }

                    val card = ime.binding.keyboardCard
                    card.animate().cancel()
                    card.alpha = 0.7f

                    val density = ime.resources.displayMetrics.density
                    val activeColor = ContextCompat.getColor(ime, R.color.theme_scribe_blue)
                    (card.background as? GradientDrawable)?.setStroke((2.5f * density).toInt(), activeColor)

                    val location = IntArray(2)
                    card.getLocationOnScreen(location)

                    keyboardCenterX = location[0] + card.width / 2f
                    keyboardCenterY = location[1] + card.height / 2f

                    initialDistance =
                        Math
                            .hypot(
                                (event.rawX - keyboardCenterX).toDouble(),
                                (event.rawY - keyboardCenterY).toDouble(),
                            ).toFloat()

                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isResizing) return@OnTouchListener false

                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY

                    val card = ime.binding.keyboardCard
                    val cardWidth = card.width.toFloat()
                    val cardHeight = card.height.toFloat()

                    if (cardWidth > 0 && cardHeight > 0) {
                        var targetScaleX = initialScaleX + (dragFactorX * dx) / cardWidth
                        var targetScaleY = initialScaleY + (dragFactorY * dy) / cardHeight

                        targetScaleX = targetScaleX.coerceIn(0.6f, 1.5f)
                        targetScaleY = targetScaleY.coerceIn(0.6f, 1.5f)

                        applyScaleAndPosition(targetScaleX, targetScaleY)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isResizing = false
                    startHideCornersTimer()

                    val card = ime.binding.keyboardCard
                    val finalScaleX = card.scaleX
                    val finalScaleY = card.scaleY
                    PreferencesHelper.setFloatingScaleX(ime, ime.language, finalScaleX)
                    PreferencesHelper.setFloatingScaleY(ime, ime.language, finalScaleY)

                    val screenHeight =
                        ime.resources.displayMetrics.heightPixels
                            .toFloat()
                    val cardHeight = card.height.toFloat()
                    val liveY = (screenHeight - cardHeight * finalScaleY) / 2f - card.translationY
                    PreferencesHelper.setFloatingX(ime, ime.language, card.translationX)
                    PreferencesHelper.setFloatingY(ime, ime.language, liveY)

                    applyFloatingModeState()
                    card
                        .animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .start()
                    true
                }
                else -> false
            }
        }

    @SuppressLint("ClickableViewAccessibility")
    fun setupFloatingDragListener() {
        if (!ime.isUiManagerInitialized) return

        ime.binding.floatingDragHandle.setOnTouchListener { _, event ->
            if (!isFloatingMode) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    initialY = event.rawY

                    val card = ime.binding.keyboardCard
                    card.animate().cancel()
                    card.alpha = 0.7f

                    val density = ime.resources.displayMetrics.density
                    val activeColor = ContextCompat.getColor(ime, R.color.theme_scribe_blue)
                    (card.background as? GradientDrawable)?.setStroke((2.5f * density).toInt(), activeColor)

                    val displayMetrics = ime.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val screenHeight = displayMetrics.heightPixels
                    val cardWidth = card.width.toFloat()
                    val cardHeight = card.height.toFloat()
                    val scaleFactorX = PreferencesHelper.getFloatingScaleX(ime, ime.language)
                    val scaleFactorY = PreferencesHelper.getFloatingScaleY(ime, ime.language)

                    maxTranslationX = (screenWidth - cardWidth * scaleFactorX) / 2f
                    minTranslationX = -maxTranslationX

                    minTranslationY = 0f
                    maxTranslationY = screenHeight.toFloat() - cardHeight * scaleFactorY

                    initialTranslationX = PreferencesHelper.getFloatingX(ime, ime.language).coerceInSafe(minTranslationX, maxTranslationX)
                    initialTranslationY = PreferencesHelper.getFloatingY(ime, ime.language).coerceInSafe(minTranslationY, maxTranslationY)

                    showCorners()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialX
                    val deltaY = event.rawY - initialY

                    var targetX = initialTranslationX + deltaX
                    var targetY = initialTranslationY - deltaY

                    targetX = targetX.coerceInSafe(minTranslationX, maxTranslationX)
                    targetY = targetY.coerceInSafe(minTranslationY, maxTranslationY)

                    val scaleFactorX = PreferencesHelper.getFloatingScaleX(ime, ime.language)
                    val scaleFactorY = PreferencesHelper.getFloatingScaleY(ime, ime.language)
                    updateFloatingViewsPosition(targetX, targetY, scaleFactorX, scaleFactorY)

                    val card = ime.binding.keyboardCard
                    val density = ime.resources.displayMetrics.density
                    val isNearBottom = targetY < 60f * density
                    if (isNearBottom) {
                        val dockColor = ContextCompat.getColor(ime, R.color.theme_scribe_blue)
                        (card.background as? GradientDrawable)?.setStroke((4.0f * density).toInt(), dockColor)
                        card.alpha = 0.85f
                    } else {
                        val activeColor = ContextCompat.getColor(ime, R.color.theme_scribe_blue)
                        (card.background as? GradientDrawable)?.setStroke((2.5f * density).toInt(), activeColor)
                        card.alpha = 0.7f
                    }

                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val deltaX = event.rawX - initialX
                    val deltaY = event.rawY - initialY
                    var finalTargetX = initialTranslationX + deltaX
                    var finalTargetY = initialTranslationY - deltaY
                    finalTargetX = finalTargetX.coerceInSafe(minTranslationX, maxTranslationX)
                    finalTargetY = finalTargetY.coerceInSafe(minTranslationY, maxTranslationY)

                    val scaleFactorX = PreferencesHelper.getFloatingScaleX(ime, ime.language)
                    val scaleFactorY = PreferencesHelper.getFloatingScaleY(ime, ime.language)
                    val density = ime.resources.displayMetrics.density
                    val isNearBottom = finalTargetY < 60f * density

                    val card = ime.binding.keyboardCard

                    if (isNearBottom) {
                        disableFloatingMode()
                    } else {
                        updateFloatingViewsPosition(finalTargetX, finalTargetY, scaleFactorX, scaleFactorY)
                        PreferencesHelper.setFloatingX(ime, ime.language, finalTargetX)
                        PreferencesHelper.setFloatingY(ime, ime.language, finalTargetY)
                        applyFloatingModeState()
                    }

                    card
                        .animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .start()
                    ime.binding.root.requestLayout()
                    startHideCornersTimer()
                    true
                }
                else -> false
            }
        }
    }
}

private fun Float.coerceInSafe(
    minimumValue: Float,
    maximumValue: Float,
): Float =
    if (minimumValue > maximumValue) {
        minimumValue
    } else {
        this.coerceIn(minimumValue, maximumValue)
    }
