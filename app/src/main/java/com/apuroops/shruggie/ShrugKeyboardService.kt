package com.apuroops.shruggie

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout

/**
 * Custom root layout that enforces a specific height regardless of what the IME
 * framework's input frame tries to constrain it to via MeasureSpec.
 */
class KeyboardRootLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    var forcedHeight: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val h = if (forcedHeight > 0) forcedHeight else MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
    }
}

class ShrugKeyboardService : InputMethodService() {

    private var backspaceButton: Button? = null

    override fun onCreateInputView(): View {
        val density = resources.displayMetrics.density
        val navBarHeight = navBarHeight()
        val bottomInset = navBarHeight + (16 * density).toInt()
        val targetHeight = (resources.displayMetrics.heightPixels * 0.19).toInt() + bottomInset
        Log.d(TAG, "onCreateInputView: navBarHeight=$navBarHeight px, bottomInset=$bottomInset px, targetHeight=$targetHeight px")

        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardRootLayout
        keyboardView.forcedHeight = targetHeight

        val p = (8 * density).toInt()
        keyboardView.setPadding(p, p, p, p + bottomInset)

        keyboardView.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            val loc = IntArray(2)
            v.getLocationOnScreen(loc)
            val screenHeight = resources.displayMetrics.heightPixels
            val viewBottom = loc[1] + v.height
            Log.d(TAG, "keyboardView: width=${v.width}px, height=${v.height}px, top=${loc[1]}px, bottom=${viewBottom}px, spaceBelowView=${screenHeight - viewBottom}px")
        }

        keyboardView.findViewById<Button>(R.id.shrug_button).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            currentInputConnection?.commitText(SHRUGGIE, 1)
            updateBackspaceColor()
        }
        keyboardView.findViewById<Button>(R.id.return_button).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            currentInputConnection?.commitText("\n", 1)
            updateBackspaceColor()
        }
        keyboardView.findViewById<Button>(R.id.space_button).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            currentInputConnection?.commitText(" ", 1)
            updateBackspaceColor()
        }

        backspaceButton = keyboardView.findViewById(R.id.backspace_button)
        backspaceButton?.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            smartDelete()
            updateBackspaceColor()
        }

        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.d(TAG, "onStartInputView: win.attributes.height=${window.window?.attributes?.height}")
        updateBackspaceColor()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updateBackspaceColor()
    }

    private fun smartDelete() {
        val ic = currentInputConnection ?: return

        val preceding = ic.getTextBeforeCursor(SHRUGGIE.length, 0)
        when {
            // InputConnection blocked — fall back to deleting one character
            preceding == null -> ic.deleteSurroundingText(1, 0)
            // Full shruggie immediately before cursor — delete all characters
            preceding == SHRUGGIE -> ic.deleteSurroundingText(SHRUGGIE.length, 0)
            // Space or newline — delete just that one character
            preceding.endsWith(" ") || preceding.endsWith("\n") -> ic.deleteSurroundingText(1, 0)
            // Mid-shruggie, foreign text, or anything else — delete one character
            else -> ic.deleteSurroundingText(1, 0)
        }
    }

    private fun updateBackspaceColor() {
        val ic = currentInputConnection
        val accessible = ic != null && ic.getTextBeforeCursor(1, 0) != null
        val color = if (accessible) COLOR_GREEN else COLOR_RED
        backspaceButton?.setTextColor(color)
        Log.d(TAG, "updateBackspaceColor: accessible=$accessible")
    }

    private fun navBarHeight(): Int {
        val height = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = getSystemService(WindowManager::class.java)
            wm.currentWindowMetrics.windowInsets
                .getInsets(WindowInsets.Type.systemGestures()).bottom
        } else {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }
        Log.d(TAG, "navBarHeight: $height px")
        return height
    }

    companion object {
        private const val TAG = "ShrugKeyboard"
        private const val SHRUGGIE = "¯\\_(ツ)_/¯"
        private const val COLOR_GREEN = 0xFF2E7D32.toInt() // dark green — InputConnection accessible
        private const val COLOR_RED   = 0xFFC62828.toInt() // dark red   — InputConnection blocked
    }
}
