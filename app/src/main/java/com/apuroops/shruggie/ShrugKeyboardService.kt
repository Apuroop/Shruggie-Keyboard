package com.apuroops.shruggie

import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout

class ShrugKeyboardService : InputMethodService() {

    private var keyboardHeight = 0

    override fun onCreateInputView(): View {
        val screenHeight = resources.displayMetrics.heightPixels
        // Half of a standard keyboard height
        keyboardHeight = (screenHeight * 0.19).toInt()
        val vertPadding = (keyboardHeight * 0.10).toInt()
        val horizPadding = (resources.displayMetrics.density * 12).toInt() // 12dp

        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)
        keyboardView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight)
        keyboardView.setPadding(horizPadding, vertPadding, horizPadding, vertPadding)

        val button = keyboardView.findViewById<Button>(R.id.shrug_button)
        // match_parent now works because the parent has an explicit height,
        // and padding on the parent gives the button 80% of the keyboard height
        button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        button.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            currentInputConnection?.commitText("¯\\_(ツ)_/¯", 1)
        }
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        window.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight)
    }
}
