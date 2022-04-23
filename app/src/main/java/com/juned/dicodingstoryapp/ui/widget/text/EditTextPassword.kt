package com.juned.dicodingstoryapp.ui.widget.text

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.juned.dicodingstoryapp.R

class EditTextPassword : EditTextGeneral, View.OnTouchListener {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private lateinit var showPasswordButtonImage: Drawable

    private fun init() {
        showPasswordButtonImage = ContextCompat.getDrawable(
            context, R.drawable.ic_baseline_remove_red_eye_24
        ) as Drawable

        setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            showPasswordButtonImage,
            null
        )

        setOnTouchListener(this)
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        var isButtonTouched = false

        if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            val buttonEnd = (showPasswordButtonImage.intrinsicWidth + paddingStart)
                .toFloat()

            if (event.x < buttonEnd) {
                isButtonTouched = true
            }
        } else {
            val buttonStart = (width - paddingEnd - showPasswordButtonImage.intrinsicWidth)
                .toFloat()

            if (event.x > buttonStart) {
                isButtonTouched = true
            }
        }

        if (isButtonTouched) {
            val curSelection = selectionEnd

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    transformationMethod = HideReturnsTransformationMethod.getInstance()
                    setSelection(curSelection)
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    transformationMethod = PasswordTransformationMethod.getInstance()
                    setSelection(curSelection)
                    return true
                }
            }
        }

        return false
    }
}