package com.vvechirko.slidepaneltest.slide

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.vvechirko.slidepaneltest.R

class SlidingViewBehaviour<V : View> : CoordinatorLayout.Behavior<V> {

    private var overlapView: Int = View.NO_ID
    private var minOffset: Int = 0
    private var maxOffset: Int = 0
    private var curOffset: Int = 0
    private var skipNestedPreScroll: Boolean = false

    val totalOffset: Int
        get() = maxOffset - minOffset

    constructor() {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SlidingViewBehavior_Params)
        overlapView =
            a.getResourceId(R.styleable.SlidingViewBehavior_Params_behavior_overlapView, View.NO_ID)
        a.recycle()
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        Log.d("SlidingBehavior", "onLayoutChild $child")
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection)
        curOffset = child.top
        minOffset = curOffset
        maxOffset += minOffset
        setTopBottomOffset(child, maxOffset)
        return true
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        Log.d("SlidingBehavior", "layoutDependsOn $child")
        return dependency.id == overlapView
    }

    override fun onMeasureChild(
        parent: CoordinatorLayout, child: V, parentWidthMeasureSpec: Int, widthUsed: Int,
        parentHeightMeasureSpec: Int, heightUsed: Int
    ): Boolean {
        Log.d("SlidingBehavior", "onMeasureChild $child")
        return parent.getDependencies(child).firstOrNull()?.let { view ->
            // find first dependent view
            if (view.fitsSystemWindows) {
                child.fitsSystemWindows = true
            }

            maxOffset = view.measuredHeight
            parent.onMeasureChild(
                child, parentWidthMeasureSpec,
                widthUsed, parentHeightMeasureSpec, heightUsed
            )
            true
        } ?: false
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: V,
        dependency: View
    ): Boolean {
        Log.d("SlidingBehavior", "onMeasureChild $child")
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onStartNestedScroll(
        parent: CoordinatorLayout, child: V,
        directTargetChild: View, target: View, axes: Int, type: Int
    ): Boolean {
        Log.d("Behavior", "onStartNestedScroll $child")
        // Return true if we're nested scrolling vertically and we have scrollable children
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0 // && child.hasScrollableChildren()
    }

    override fun onNestedPreScroll(
        parent: CoordinatorLayout, child: V, target: View,
        dx: Int, dy: Int, consumed: IntArray, type: Int
    ) {
        Log.d("Behavior", "onNestedPreScroll $child")
        if (dy != 0 && !skipNestedPreScroll) {
            consumed[1] = setTopBottomOffset(child, curOffset - dy)
        }
    }

    override fun onNestedScroll(
        parent: CoordinatorLayout, child: V, target: View,
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int
    ) {
        Log.d("Behavior", "onNestedScroll $child")
        if (dyUnconsumed < 0) {
            // If the scrolling view is scrolling down but not consuming, it's probably be at
            // the top of it's content
            setTopBottomOffset(child, curOffset - dyUnconsumed)
            // Set the expanding flag so that onNestedPreScroll doesn't handle any events
            skipNestedPreScroll = true
        } else {
            // As we're no longer handling nested scrolls, reset the skip flag
            skipNestedPreScroll = false
        }
    }

    override fun onStopNestedScroll(parent: CoordinatorLayout, child: V, target: View, type: Int) {
        Log.d("Behavior", "onStopNestedScroll $child")
        // Reset the skip flag
        skipNestedPreScroll = false
    }

    private fun setTopBottomOffset(child: V, newOffset: Int): Int {
        var offset = newOffset
        var consumed = 0
        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            offset = constrain(offset, minOffset, maxOffset)
            if (curOffset != offset) {
                ViewCompat.offsetTopAndBottom(child, offset - child.top)
                // Update how much dy we have consumed
                consumed = curOffset - offset
                // Update the stored sibling offset
                curOffset = offset

                // Dispatch the updates to any listeners
//                dispatchOffsetUpdates(child)
            }
        }
        return consumed
    }

    private fun constrain(amount: Int, low: Int, high: Int): Int {
        return if (amount < low) low else if (amount > high) high else amount
    }
}