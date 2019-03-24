package com.vvechirko.slidepaneltest

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder

class SlidingContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {

    private lateinit var headerView: View
    private lateinit var panelView: View

    private val gestureDetector = GestureDetector(context, this)

    private var collapsedPanelMargin = 0
    private var expandedPanelMargin = -1

    private var isAnimating = false

    private val scroller = OverScroller(context)

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        val lp = child.layoutParams as LayoutParams
        if (lp.isViewHeader) {
            headerView = child
            Log.d("SlidingContainer", "headerView found")
        } else if (lp.isViewPanel) {
            panelView = child
            Log.d("SlidingContainer", "panelView found")
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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        Log.d("SlidingContainer", "onLayout")
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.d("onTouchEvent", "onInterceptTouchEvent $event")
//        return (withinView(panelView, event) && detectSwipe(event)) || super.onInterceptTouchEvent(event)
        return super.onInterceptTouchEvent(event)
    }

    private fun withinView(view: View, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        return view.left < x && x < view.right
                && view.top < y && y < view.bottom
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("onTouchEvent", "onTouchEvent $event")
//        return (withinView(panelView, event) && detectSwipe(event)) || super.onTouchEvent(event)
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

//    private val slop: Float = 20.0f
//
//    private var downX: Float = 0.0f
//    private var downY: Float = 0.0f
//    private var isSwiping: Boolean = false
//    private var isOpened: Boolean = false

//    private fun detectSwipe(event: MotionEvent): Boolean {
//        when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                downX = event.x
//                downY = event.y
//                return true
//            }
//            MotionEvent.ACTION_MOVE -> {
//                val deltaX = event.x - downX
//                val deltaY = event.y - downY
//                if (!isSwiping && Math.abs(deltaY) > slop && Math.abs(deltaX) < slop) {
//                    isSwiping = true
//                    if (deltaY > 0) {
//                        Log.d("onTouchEvent", "swipe from top to bottom")
//                        moveView(false)
//                    } else {
//                        Log.d("onTouchEvent", "swipe from bottom to top")
//                        moveView(true)
//                    }
//                    return true
//                } else if (!isSwiping && Math.abs(deltaX) > slop) {
//                    isSwiping = true
//                    Log.d("onTouchEvent", "swipe horizontal")
//                    return true
//                }
//            }
//            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isSwiping = false
//        }
//        return false
//    }

//    private fun moveView(reverse: Boolean) {
//        if (isOpened == reverse) return // already opened/closed
//        isOpened = reverse
//
//        Log.d(
//            "ValueAnimator",
//            "expandedPanelMargin = $expandedPanelMargin, collapsedPanelMargin = $collapsedPanelMargin"
//        )
//        val anim = ValueAnimator.ofFloat(0.0f, 1.0f)
//        anim.addUpdateListener {
//            val progress = it.animatedValue as Float
//
//            val lp = panelView.layoutParams as LayoutParams
//            lp.topMargin = (collapsedPanelMargin + progress * (expandedPanelMargin - collapsedPanelMargin)).toInt()
//            Log.d("ValueAnimator", "topMargin = ${lp.topMargin}")
//            panelView.layoutParams = lp
//        }
//        anim.addListener(object : Animator.AnimatorListener {
//            override fun onAnimationRepeat(animation: Animator) {}
//
//            override fun onAnimationEnd(animation: Animator) {
//                isAnimating = false
//            }
//
//            override fun onAnimationCancel(animation: Animator) {}
//
//            override fun onAnimationStart(animation: Animator) {
//                isAnimating = true
//            }
//        })
//        if (reverse) anim.reverse() else anim.start()
//    }

    private var startMargin = 0

    override fun onDown(event: MotionEvent): Boolean {
        Log.d("SlidingContainer", "onDown")
        startMargin = (panelView.layoutParams as MarginLayoutParams).topMargin
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

        val lp = panelView.layoutParams as LayoutParams
        startMargin -= distanceY.toInt()
        var temp = startMargin
        if (temp > expandedPanelMargin) {
            temp = expandedPanelMargin
        } else if (temp < collapsedPanelMargin) {
            temp = collapsedPanelMargin
        }

        lp.topMargin = temp
        Log.d("SlidingContainer", "topMargin = ${lp.topMargin}")
        panelView.layoutParams = lp
        return false
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        Log.d("SlidingContainer", "onFling $velocityX, $velocityY")
        scroller.forceFinished(true)
        scroller.fling(0, startMargin, velocityX.toInt(), velocityY.toInt(),
            0, 0, collapsedPanelMargin, expandedPanelMargin)
        postInvalidateOnAnimation()

//        val flingY = FlingAnimation(FloatValueHolder(startMargin.toFloat()))
//        flingY.setStartVelocity(velocityY)
//            .setMinValue(collapsedPanelMargin.toFloat())  // minimum translationY property
//            .setMaxValue(expandedPanelMargin.toFloat()) // maximum translationY property
//            .setFriction(1.1f)
//            .start()

        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
            val currY = scroller.currY
            if (currY == scroller.finalY) {
                return
            }
            val lp = panelView.layoutParams as LayoutParams
            lp.topMargin = currY
            Log.d("SlidingContainer", "topMargin = ${lp.topMargin}")
            panelView.layoutParams = lp
            postInvalidateOnAnimation()

            Log.d("SlidingContainer", "computeScroll $currX, $currY")
        }
    }

    // SlidingContainer.LayoutParams

    override fun generateDefaultLayoutParams(): SlidingContainer.LayoutParams {
        return LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): SlidingContainer.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): SlidingContainer.LayoutParams {
        return LayoutParams(lp)
    }

    class LayoutParams : FrameLayout.LayoutParams {

        var isViewPanel: Boolean = false
        var isViewHeader: Boolean = false
        var topOffset: Int = 0

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.SlidingContainer_Layout)
            isViewPanel = a.getBoolean(R.styleable.SlidingContainer_Layout_layout_viewPanel, false)
            isViewHeader = a.getBoolean(R.styleable.SlidingContainer_Layout_layout_viewHeader, false)
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