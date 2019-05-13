package com.vvechirko.slidepaneltest.motion

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.ViewCompat

class CollapsingToolbar(context: Context, attrs: AttributeSet?) : MotionLayout(context, attrs) {

    init {
//        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
//            Log.d("CollapsingToolbar", "OnApplyWindowInsetsListener $insets")
//            val params = v.layoutParams as ViewGroup.MarginLayoutParams
//            params.topMargin = insets.systemWindowInsetTop
//            setPaddingRelative(paddingStart, insets.systemWindowInsetTop, paddingEnd, paddingBottom)
//            insets.consumeSystemWindowInsets()
//        }
    }
}