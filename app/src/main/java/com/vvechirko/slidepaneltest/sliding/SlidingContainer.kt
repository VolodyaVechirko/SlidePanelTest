package com.vvechirko.slidepaneltest.sliding

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.OverScroller
import com.vvechirko.slidepaneltest.R

class SlidingContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {

    val VIEW_HEADER = -1
    val VIEW_PANEL = -2

    private lateinit var headerView: View
    private lateinit var panelView: View

    private val gestureDetector = GestureDetector(context, this)

    private var collapsedPanelMargin = 0
    private var expandedPanelMargin = -1

    private val scroller = OverScroller(context)

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        val lp = child.layoutParams as LayoutParams
        if (lp.viewType == VIEW_HEADER) {
            headerView = child
            Log.d("SlidingContainer", "headerView found")
        } else if (lp.viewType == VIEW_PANEL) {
            panelView = child
            Log.d("SlidingContainer", "panelView found")
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        Log.d("SlidingContainer", "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (expandedPanelMargin == -1) {
            val lp = panelView.layoutParams as LayoutParams
            collapsedPanelMargin = lp.topMargin
            expandedPanelMargin = collapsedPanelMargin + headerView.measuredHeight
            lp.topMargin = expandedPanelMargin
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        Log.d("SlidingContainer", "onLayout")
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
//        Log.d("onTouchEvent", "onInterceptTouchEvent $event")
//        return (withinView(panelView, event) && detectSwipe(event)) || super.onInterceptTouchEvent(event)
        return super.onInterceptTouchEvent(event)
    }

    private fun withinView(view: View, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        return view.left < x && x < view.right && view.top < y && y < view.bottom
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        Log.d("onTouchEvent", "onTouchEvent $event")
//        return (withinView(panelView, event) && detectSwipe(event)) || super.onTouchEvent(event)
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onDown(event: MotionEvent): Boolean {
        Log.d("SlidingContainer", "onDown")
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d("SlidingContainer", "onLongPress")
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d("SlidingContainer", "onShowPress")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d("SlidingContainer", "onSingleTapUp")
        return false
    }

    override fun onScroll(event1: MotionEvent, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        Log.d("SlidingContainer", "onScroll $distanceX, $distanceY")
        val startTop = (panelView.layoutParams as LayoutParams).topMargin
        val dy = startTop - distanceY.toInt()

        updateMargin(dy)
        return false
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        Log.d("SlidingContainer", "onFling $velocityX, $velocityY")
//        scroller.forceFinished(true)
//        scroller.fling(
//            0, startMargin, velocityX.toInt(), velocityY.toInt(),
//            0, 0, collapsedPanelMargin, expandedPanelMargin
//        )

        scroller.abortAnimation()
        val startLeft = panelView.left
        val startTop = (panelView.layoutParams as LayoutParams).topMargin
        val toY = if (velocityY > 0) expandedPanelMargin else collapsedPanelMargin
        val dy = toY - startTop

        scroller.startScroll(startLeft, startTop, 0, dy, 500)
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
            val currY = scroller.currY
            Log.d("SlidingContainer", "computeScroll $currX, $currY")
            if (currY == scroller.finalY) {
                return
            }

            updateMargin(currY)
        }
    }

    private fun updateMargin(value: Int) {
        var temp = value
        if (temp > expandedPanelMargin) {
            temp = expandedPanelMargin
        } else if (temp < collapsedPanelMargin) {
            temp = collapsedPanelMargin
        }

        val lp = panelView.layoutParams as LayoutParams
        lp.topMargin = temp
        Log.d("SlidingContainer", "topMargin = ${lp.topMargin}")
        panelView.layoutParams = lp
    }



    // SlidingContainer.LayoutParams

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): LayoutParams {
        return LayoutParams(lp)
    }

    class LayoutParams : FrameLayout.LayoutParams {

        var viewType: Int = 0
        var topOffset: Int = 0

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.SlidingContainer_Layout)
            viewType = a.getLayoutDimension(R.styleable.SlidingContainer_Layout_layout_viewType, 0)
            topOffset = a.getDimensionPixelSize(R.styleable.SlidingContainer_Layout_layout_topOffset, 0)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(source: ViewGroup.LayoutParams) : super(source)
        constructor(source: FrameLayout.LayoutParams) : super(source)
        constructor(source: MarginLayoutParams) : super(source)
    }
}