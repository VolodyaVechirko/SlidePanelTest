package com.vvechirko.slidepaneltest.motion

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.WindowInsetsCompat
import com.vvechirko.slidepaneltest.R

class MotionAppBar(context: Context, attrs: AttributeSet?) : MotionLayout(context, attrs) {

    init {
        val topGuideline = Guideline(context)
        topGuideline.id = R.id.topInset
        val lp = generateDefaultLayoutParams()
        lp.guideBegin = 0
        lp.orientation = 0
        addView(topGuideline, lp)
    }

    fun applyInsets(insets: WindowInsetsCompat) {
        val topInset = insets.systemWindowInsetTop
        getConstraintSet(R.id.end).getParameters(R.id.topInset).layout.guideBegin = topInset
        getConstraintSet(R.id.start).getParameters(R.id.topInset).layout.guideBegin = topInset
    }
}