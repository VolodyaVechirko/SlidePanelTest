package com.vvechirko.slidepaneltest.sliding

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

class SimpleSlideView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var headerView: View
    private lateinit var panelView: View

    private var collapsedPanelMargin = 0
    private var expandedPanelMargin = -1

    private var isAnimating = false

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 1) {
            headerView = getChildAt(0)
            panelView = getChildAt(1)
        }
    }

//    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
//        super.addView(child, index, params)
//        val lp = child.layoutParams as SlidingContainer.LayoutParams
//        if (lp.isViewHeader) {
//            headerView = child
//            Log.d("SlidingContainer", "headerView found")
//        } else if (lp.isViewPanel) {
//            panelView = child
//            Log.d("SlidingContainer", "panelView found")
//        }
//    }

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
        Log.d("onTouchEvent", "onInterceptTouchEvent $event")
        return (withinView(panelView, event) && detectSwipe(event)) || super.onInterceptTouchEvent(event)
    }

    private fun withinView(view: View, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        return view.left < x && x < view.right
                && view.top < y && y < view.bottom
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("onTouchEvent", "onTouchEvent $event")
        return (withinView(panelView, event) && detectSwipe(event)) || super.onTouchEvent(event)
    }

    private val slop: Float = 20.0f

    private var downX: Float = 0.0f
    private var downY: Float = 0.0f
    private var isSwiping: Boolean = false
    private var isOpened: Boolean = false

    private fun detectSwipe(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - downX
                val deltaY = event.y - downY
                if (!isSwiping && Math.abs(deltaY) > slop && Math.abs(deltaX) < slop) {
                    isSwiping = true
                    if (deltaY > 0) {
                        Log.d("onTouchEvent", "swipe from top to bottom")
                        moveView(false)
                    } else {
                        Log.d("onTouchEvent", "swipe from bottom to top")
                        moveView(true)
                    }
                    return true
                } else if (!isSwiping && Math.abs(deltaX) > slop) {
                    isSwiping = true
                    Log.d("onTouchEvent", "swipe horizontal")
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isSwiping = false
        }
        return false
    }

    private fun moveView(reverse: Boolean) {
        if (isOpened == reverse) return // already opened/closed
        isOpened = reverse

        Log.d(
            "ValueAnimator",
            "expandedPanelMargin = $expandedPanelMargin, collapsedPanelMargin = $collapsedPanelMargin"
        )
        val anim = ValueAnimator.ofFloat(0.0f, 1.0f)
        anim.addUpdateListener {
            val progress = it.animatedValue as Float

            val lp = panelView.layoutParams as LayoutParams
            lp.topMargin = (collapsedPanelMargin + progress * (expandedPanelMargin - collapsedPanelMargin)).toInt()
            Log.d("ValueAnimator", "topMargin = ${lp.topMargin}")
            panelView.layoutParams = lp
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                isAnimating = false
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationStart(animation: Animator) {
                isAnimating = true
            }
        })
        if (reverse) anim.reverse() else anim.start()
    }
}