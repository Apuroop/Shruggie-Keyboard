package com.apuroops.shruggie

import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button

class ShrugKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)
        keyboardView.findViewById<Button>(R.id.shrug_button).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            currentInputConnection?.commitText("¯\\_(ツ)_/¯", 1)
        }
        return keyboardView
    }
}
