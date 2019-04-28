package com.vvechirko.slidepaneltest.sliding

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.customview.widget.ViewDragHelper

class DragContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var headerView: View
    private lateinit var panelView: View

    private var collapsedPanelMargin = 0
    private var expandedPanelMargin = -1

    private var isAnimating = false

    val velocityThreshold = 1500f * resources.displayMetrics.density
    val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    val dragCallback = object : ViewDragHelper.Callback() {

        var initTop: Int = 0

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            Log.d("ViewDragHelper", "tryCaptureView $child")
            initTop = child.top
            return child == panelView
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            Log.d("ViewDragHelper", "clampViewPositionHorizontal $child top $top dy $dy")
            if (dy > 0) {
//                return clampMoveBottom(child, top)
            } else {
//                return clampMoveTop(child, top)
            }
            return top
        }

        override fun getViewVerticalDragRange(child: View): Int {
            Log.d("ViewDragHelper", "getViewHorizontalDragRange $child")
//            return height
            return height - child.height
        }

        override fun onViewReleased(child: View, xvel: Float, yvel: Float) {
            Log.d("ViewDragHelper", "onViewReleased $child xvel $xvel yvel $yvel")
            if (yvel > 0) {
                dragHelper.settleCapturedViewAt(child.left, height - child.height)
            } else {
                dragHelper.settleCapturedViewAt(child.left, 0)
            }
            invalidate()
        }

        override fun onViewPositionChanged(child: View, left: Int, top: Int, dx: Int, dy: Int) {
            Log.d("ViewDragHelper", "onViewPositionChanged $child left $left top $top dx $dx dy $dy")

        }
    }

    val dragHelper: ViewDragHelper = ViewDragHelper.create(this, 1f, dragCallback)

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 1) {
            headerView = getChildAt(0)
            panelView = getChildAt(1)
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        Log.d("ViewDragHelper", "computeScroll")
        if (dragHelper.continueSettling(true)) {
            postInvalidateOnAnimation()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        Log.d("SlidingContainer", "onMeasure isAnimating $isAnimating")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (expandedPanelMargin == -1) {
            val lp = panelView.layoutParams as LayoutParams
            collapsedPanelMargin = lp.topMargin
            expandedPanelMargin = collapsedPanelMargin + headerView.measuredHeight
            lp.topMargin = expandedPanelMargin
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return dragHelper.shouldInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper.processTouchEvent(event)
        return true
    }
}