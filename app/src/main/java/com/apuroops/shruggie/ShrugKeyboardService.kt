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

    override fun onCreateInputView(): View {
        val navBarHeight = navBarHeight()
        val targetHeight = (resources.displayMetrics.heightPixels * 0.19).toInt() + navBarHeight
        Log.d(TAG, "onCreateInputView: navBarHeight=$navBarHeight px, targetHeight=$targetHeight px")

        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardRootLayout
        keyboardView.forcedHeight = targetHeight

        // Push content above the nav bar by adding navBarHeight to the bottom padding
        val p = (8 * resources.displayMetrics.density).toInt()
        keyboardView.setPadding(p, p, p, p + navBarHeight)

        keyboardView.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            val loc = IntArray(2)
            v.getLocationOnScreen(loc)
            val screenHeight = resources.displayMetrics.heightPixels
            val viewBottom = loc[1] + v.height
            Log.d(TAG, "keyboardView: width=${v.width}px, height=${v.height}px, top=${loc[1]}px, bottom=${viewBottom}px, screen=${screenHeight}px, spaceBelowView=${screenHeight - viewBottom}px")
        }

        keyboardView.findViewById<Button>(R.id.shrug_button).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            currentInputConnection?.commitText("¯\\_(ツ)_/¯", 1)
        }
        keyboardView.findViewById<Button>(R.id.return_button).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            currentInputConnection?.commitText("\n", 1)
        }
        keyboardView.findViewById<Button>(R.id.space_button).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            currentInputConnection?.commitText(" ", 1)
        }
        keyboardView.findViewById<Button>(R.id.backspace_button).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            // ¯\_(ツ)_/¯ is exactly 9 characters — delete the whole shruggie at once
            currentInputConnection?.deleteSurroundingText(9, 0)
        }

        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.d(TAG, "onStartInputView: win.attributes.height=${window.window?.attributes?.height}")
    }

    private fun navBarHeight(): Int {
        val height = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = getSystemService(WindowManager::class.java)
            val insets = wm.currentWindowMetrics.windowInsets
            Log.d(TAG, "insets navigationBars.bottom:       ${insets.getInsets(WindowInsets.Type.navigationBars()).bottom}")
            Log.d(TAG, "insets systemBars.bottom:           ${insets.getInsets(WindowInsets.Type.systemBars()).bottom}")
            Log.d(TAG, "insets tappableElement.bottom:      ${insets.getInsets(WindowInsets.Type.tappableElement()).bottom}")
            Log.d(TAG, "insets mandatorySystemGestures.bot: ${insets.getInsets(WindowInsets.Type.mandatorySystemGestures()).bottom}")
            Log.d(TAG, "insets systemGestures.bottom:       ${insets.getInsets(WindowInsets.Type.systemGestures()).bottom}")
            insets.getInsets(WindowInsets.Type.systemGestures()).bottom
        } else {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }
        Log.d(TAG, "navBarHeight returning: $height px")
        return height
    }

    companion object {
        private const val TAG = "ShrugKeyboard"
    }
}
