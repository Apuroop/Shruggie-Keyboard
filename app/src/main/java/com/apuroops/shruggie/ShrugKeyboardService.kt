package com.apuroops.shruggie

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button

class ShrugKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)

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
        val win = window.window ?: return
        val navBarHeight = navBarHeight()
        val keyboardHeight = (resources.displayMetrics.heightPixels * 0.19).toInt() + navBarHeight
        win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight)
    }

    private fun navBarHeight(): Int {
        // WindowManager.currentWindowMetrics is available from API 30 and queries the
        // WindowManager service directly — reliable regardless of view attachment state.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = getSystemService(WindowManager::class.java)
            wm.currentWindowMetrics.windowInsets
                .getInsets(WindowInsets.Type.navigationBars()).bottom
        } else {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }
    }
}
