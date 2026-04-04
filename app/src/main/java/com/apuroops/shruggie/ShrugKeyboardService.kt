package com.apuroops.shruggie

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button

class ShrugKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)

        // Set an explicit height on the view itself — the IME framework sizes the
        // window to fit the content, so this is the correct way to control height.
        // No nav bar compensation needed: the system positions the IME window above
        // the nav bar automatically.
        val targetHeight = (resources.displayMetrics.heightPixels * 0.19).toInt()
        keyboardView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, targetHeight)
        Log.d(TAG, "onCreateInputView: targetHeight=$targetHeight px (screen=${resources.displayMetrics.heightPixels})")

        keyboardView.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            Log.d(TAG, "keyboardView layout changed: width=${v.width}px, height=${v.height}px")
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

    companion object {
        private const val TAG = "ShrugKeyboard"
    }
}
