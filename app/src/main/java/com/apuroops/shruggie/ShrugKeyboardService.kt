package com.apuroops.shruggie

import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
        val navBarHeight = ViewCompat.getRootWindowInsets(win.decorView)
            ?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        val keyboardHeight = (resources.displayMetrics.heightPixels * 0.19).toInt() + navBarHeight
        win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight)
    }
}
