package com.vvechirko.slidepaneltest.motion

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vvechirko.slidepaneltest.R

class MotionRoot(context: Context, attrs: AttributeSet?) : MotionLayout(context, attrs) {

    private var lastInsets: WindowInsetsCompat? = null
    private var drawStatusBarBackground: Boolean = false
    private var statusBarBackground: Drawable? = null

    init {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimaryDark, value, true)
        val colorPrimaryDark = value.data

        statusBarBackground = ColorDrawable(colorPrimaryDark)
        setupForInsets()
    }

    override fun setFitsSystemWindows(fitSystemWindows: Boolean) {
        super.setFitsSystemWindows(fitSystemWindows)
        setupForInsets()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (lastInsets == null && ViewCompat.getFitsSystemWindows(this)) {
            // We're set to fitSystemWindows but we haven't had any insets yet...
            // We should request a new dispatch of window insets
//            ViewCompat.requestApplyInsets(this)
        }
    }

    private fun setWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        if (lastInsets != insets) {
            lastInsets = insets

            val topInset = insets.systemWindowInsetTop
            val appBar = findViewById<MotionLayout>(R.id.appBar)
            if (ViewCompat.getFitsSystemWindows(appBar)) {

                // add [topInset] to end [ConstraintSet] of appBar
                val endAppBar = getConstraintSet(R.id.end).getParameters(R.id.appBar)
                endAppBar.layout.mHeight += topInset

                // add [topInset] to start [ConstraintSet] of appBar
                val startAppBar = getConstraintSet(R.id.start).getParameters(R.id.appBar)
                startAppBar.layout.mHeight += topInset

//                appBar.setPadding(0, topInset, 0, 0)

            } else {
                // add [topInset] to end [ConstraintSet] of appBar
                val endAppBar = getConstraintSet(R.id.end).getParameters(R.id.appBar)
                endAppBar.layout.topMargin += topInset

                // add [topInset] to start [ConstraintSet] of appBar
                val startAppBar = getConstraintSet(R.id.start).getParameters(R.id.appBar)
                startAppBar.layout.topMargin += topInset

                // update [appBar.background] bounds bug
//                if (statusBarBackground == null) {
//                    statusBarBackground = appBar.background
//                }

                drawStatusBarBackground = topInset > 0
                setWillNotDraw(!drawStatusBarBackground && background == null)
            }
        }
        return insets.consumeSystemWindowInsets()
    }

    private fun setupForInsets() {
        if (Build.VERSION.SDK_INT < 21) {
            return
        }

        if (ViewCompat.getFitsSystemWindows(this)) {
            // First apply the insets listener
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                setWindowInsets(insets)
            }

            // Now set the sys ui flags to enable us to lay out in the window insets
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(this, null)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (drawStatusBarBackground && statusBarBackground != null) {
            val inset = lastInsets?.systemWindowInsetTop ?: 0
            if (inset > 0) {
                statusBarBackground?.setBounds(0, 0, width, inset)
                statusBarBackground?.draw(canvas)
            }
        }
    }
}