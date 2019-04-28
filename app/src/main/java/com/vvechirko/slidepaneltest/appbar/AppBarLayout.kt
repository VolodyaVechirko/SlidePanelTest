package com.vvechirko.slidepaneltest.appbar

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vvechirko.slidepaneltest.R

/**
 * AppBarLayout is a vertical {@link LinearLayout} which implements many of the features of
 * material design's app bar concept, namely scrolling gestures.
 * <p>
 * Children should provide their desired scrolling behavior through
 * {@link LayoutParams#setScrollFlags(int)} and the associated layout xml attribute:
 * {@code app:layout_scrollFlags}.
 *
 * <p>
 * This view depends heavily on being used as a direct child within a {@link CoordinatorLayout}.
 * If you use AppBarLayout within a different {@link ViewGroup}, most of it's functionality will
 * not work.
 * <p>
 * AppBarLayout also requires a separate scrolling sibling in order to. The binding is done through
 * the {@link ScrollingViewBehavior} beahior class, meaning that you should set your scrolling
 * view's behavior to be an instance of {@link ScrollingViewBehavior}. A string resource containing
 * the full class name is available.
 */
@CoordinatorLayout.DefaultBehavior(AppBarLayout.Behavior::class)
class AppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    /**
     * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
     * offset changes.
     */
    interface OnOffsetChangedListener {
        /**
         * Called when the {@link AppBarLayout}'s layout offset has been changed. This allows
         * child views to implement custom behavior based on the offset (for instance pinning a
         * view at a certain y value).
         *
         * @param appBarLayout   the {@link AppBarLayout} which offset has changed
         * @param verticalOffset the vertical offset for the parent {@link AppBarLayout}, in px
         */
        fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int)
    }

    private val INVALID_SCROLL_RANGE = -1
    private var totalScrollRange = INVALID_SCROLL_RANGE
    private var downPreScrollRange = INVALID_SCROLL_RANGE
    private var downScrollRange = INVALID_SCROLL_RANGE

    private var targetElevation: Float = 0.0f
    private var lastInsets: WindowInsetsCompat? = null
    private val listeners = mutableListOf<OnOffsetChangedListener>()

    init {
        orientation = VERTICAL
        val a = context.obtainStyledAttributes(attrs, R.styleable.AppBarLayout, 0, R.style.Widget_Design_AppBarLayout)
        targetElevation = a.getDimensionPixelSize(R.styleable.AppBarLayout_elevation, 0).toFloat()
        background = a.getDrawable(R.styleable.AppBarLayout_android_background)
        a.recycle()

        // Use the bounds view outline provider so that we cast a shadow, even without a background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider = ViewOutlineProvider.BOUNDS
        }
        ViewCompat.setElevation(this, targetElevation)
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
            setWindowInsets(insets)
            insets.consumeSystemWindowInsets()
        }
    }

    private fun setWindowInsets(insets: WindowInsetsCompat) {
        var insets = insets
        // Invalidate the total scroll range...
        totalScrollRange = INVALID_SCROLL_RANGE
        lastInsets = insets
        // Now dispatch them to our children
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            insets = ViewCompat.dispatchApplyWindowInsets(child, insets)
            if (insets.isConsumed) {
                break
            }
        }
    }

    /**
     * Add a listener that will be called when the offset of this [AppBarLayout] changes.
     *
     * @param listener The listener that will be called when the offset changes.]
     * @see .removeOnOffsetChangedListener
     */
    fun addOnOffsetChangedListener(listener: OnOffsetChangedListener) {
        listeners.forEach {
            if (it === listener) {
                // Listener already added
                return
            }
        }
        listeners.add(listener)
    }

    /**
     * Remove the previously added [OnOffsetChangedListener].
     *
     * @param listener the listener to remove.
     */
    fun removeOnOffsetChangedListener(listener: OnOffsetChangedListener) {
        listeners.removeAll {
            it === listener
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        // Invalidate the scroll ranges
        totalScrollRange = INVALID_SCROLL_RANGE
        downPreScrollRange = INVALID_SCROLL_RANGE
        downScrollRange = INVALID_SCROLL_RANGE

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
        }
    }

    /**
     * Returns the scroll range of all children.
     *
     * @return the scroll range in px
     */
    fun getTotalScrollRange(): Int {
        if (totalScrollRange != INVALID_SCROLL_RANGE) {
            return totalScrollRange
        }

        var range = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            val childHeight = if (child.isLaidOut) child.height else child.measuredHeight

            val flags = lp.scrollFlags
            if (flags and LayoutParams.SCROLL_FLAG_SCROLL != 0) {
                // We're set to scroll so add the child's height
                range += childHeight
                if (flags and LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED != 0) {
                    // For a collapsing scroll, we to take the collapsed height into account.
                    // We also break straight away since later views can't scroll beneath
                    // us
                    range -= child.minimumHeight
                    break
                }
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break
            }
        }
        val top = lastInsets?.systemWindowInsetTop ?: 0
        totalScrollRange = range - top
        return totalScrollRange
    }

    internal fun hasScrollableChildren(): Boolean {
        return getTotalScrollRange() != 0
    }

    /**
     * Return the scroll range when scrolling up from a nested pre-scroll.
     */
    internal fun getUpNestedPreScrollRange(): Int {
        return getTotalScrollRange()
    }

    /**
     * Return the scroll range when scrolling down from a nested pre-scroll.
     */
    internal fun getDownNestedPreScrollRange(): Int {
        if (downPreScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return downPreScrollRange
        }

        var range = 0
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            val childHeight = if (child.isLaidOut) child.height else child.measuredHeight

            val flags = lp.scrollFlags
            if (flags and LayoutParams.FLAG_QUICK_RETURN == LayoutParams.FLAG_QUICK_RETURN) {
                // The view has the quick return flag combination...
                if (flags and LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED != 0) {
                    // If they're set to enter collapsed, use the minimum height
                    range += child.minimumHeight
                } else {
                    // Else use the full height
                    range += childHeight
                }
            } else if (range > 0) {
                // If we've hit an non-quick return scrollable view, and we've already hit a
                // quick return view, return now
                break
            }
        }
        downPreScrollRange = range
        return downPreScrollRange
    }

    /**
     * Return the scroll range when scrolling down from a nested scroll.
     */
    internal fun getDownNestedScrollRange(): Int {
        if (downScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return downScrollRange
        }
        var range = 0
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            val childHeight = if (child.isLaidOut) child.height else child.measuredHeight

            val flags = lp.scrollFlags
            if (flags and LayoutParams.SCROLL_FLAG_SCROLL != 0) {
                // We're set to scroll so add the child's height
                range += childHeight
                return range - child.minimumHeight
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break
            }
        }
        downScrollRange = range
        return downScrollRange
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        if (p is LinearLayout.LayoutParams) {
            return LayoutParams(p)
        } else if (p is MarginLayoutParams) {
            return LayoutParams(p)
        }
        return LayoutParams(p)
    }

    class LayoutParams : LinearLayout.LayoutParams {

        var scrollFlags = SCROLL_FLAG_SCROLL

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.AppBarLayout_LayoutParams)
            scrollFlags = a.getInt(R.styleable.AppBarLayout_LayoutParams_layout_scrollFlags, 0)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height) {}
        constructor(width: Int, height: Int, weight: Float) : super(width, height, weight) {}
        constructor(p: ViewGroup.LayoutParams) : super(p) {}
        constructor(source: MarginLayoutParams) : super(source) {}
        constructor(source: LinearLayout.LayoutParams) : super(source) {}
        constructor(source: LayoutParams) : super(source) {
            scrollFlags = source.scrollFlags
        }

        companion object {

            /**
             * The view will be scroll in direct relation to scroll events. This flag needs to be
             * set for any of the other flags to take effect. If any sibling views
             * before this one do not have this flag, then this value has no effect.
             */
            val SCROLL_FLAG_SCROLL = 0x1
            /**
             * When exiting (scrolling off screen) the view will be scrolled until it is
             * 'collapsed'. The collapsed height is defined by the view's minimum height.
             *
             * @see View.getMinimumHeight
             * @see View.setMinimumHeight
             */
            val SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 0x2
            /**
             * When entering (scrolling on screen) the view will scroll on any downwards
             * scroll event, regardless of whether the scrolling view is also scrolling. This
             * is commonly referred to as the 'quick return' pattern.
             */
            val SCROLL_FLAG_ENTER_ALWAYS = 0x4
            /**
             * An additional flag for 'enterAlways' which modifies the returning view to
             * only initially scroll back to it's collapsed height. Once the scrolling view has
             * reached the end of it's scroll range, the remainder of this view will be scrolled
             * into view. The collapsed height is defined by the view's minimum height.
             *
             * @see View.getMinimumHeight
             * @see View.setMinimumHeight
             */
            val SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 0x8
            /**
             * Internal flag which allows quick checking of 'quick return'
             */
            internal val FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
        }
    }


    /**
     * The default {@link Behavior} for {@link AppBarLayout}. Implements the necessary nested
     * scroll handling with offsetting.
     */
    class Behavior : CoordinatorLayout.Behavior<AppBarLayout> {

        var siblingOffsetTop: Int = 0
        private var skipNestedPreScroll: Boolean = false
        private var flingRunnable: Runnable? = null
        private lateinit var scroller: OverScroller
        private val animator: ValueAnimator = ValueAnimator()

        private var viewOffsetHelper: ViewOffsetHelper? = null

        constructor() {}
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

        override fun onLayoutChild(parent: CoordinatorLayout, child: AppBarLayout, layoutDirection: Int): Boolean {
            Log.d("Behavior", "onLayoutChild $child")
            // First let the parent lay it out
            parent.onLayoutChild(child, layoutDirection)
            if (viewOffsetHelper == null) {
                viewOffsetHelper = ViewOffsetHelper(child)
            }
            viewOffsetHelper?.onViewLayout()

            scroller = OverScroller(child.context)
            // Make sure we update the elevation
            dispatchOffsetUpdates(child)
            return true
        }

        override fun onStartNestedScroll(
            parent: CoordinatorLayout, child: AppBarLayout,
            directTargetChild: View, target: View, axes: Int, type: Int
        ): Boolean {
            Log.d("Behavior", "onStartNestedScroll $child")
            // Return true if we're nested scrolling vertically and we have scrollable children
            val started = axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0 && child.hasScrollableChildren()
            if (started) {
                // Cancel any offset animation
                animator.cancel()
            }
            return started
        }

        override fun onNestedPreScroll(
            parent: CoordinatorLayout, child: AppBarLayout, target: View,
            dx: Int, dy: Int, consumed: IntArray, type: Int
        ) {
            Log.d("Behavior", "onNestedPreScroll $child")
            if (dy != 0 && !skipNestedPreScroll) {
                val min: Int
                val max: Int
                if (dy < 0) {
                    // We're scrolling down
                    min = -child.getTotalScrollRange()
                    max = min + child.getDownNestedPreScrollRange()
                } else {
                    // We're scrolling up
                    min = -child.getTotalScrollRange()
                    max = 0
                }
                consumed[1] = scroll(parent, child, dy, min, max)
            }
        }

        override fun onNestedScroll(
            parent: CoordinatorLayout, child: AppBarLayout, target: View,
            dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int
        ) {
            Log.d("Behavior", "onNestedScroll $child")
            if (dyUnconsumed < 0) {
                // If the scrolling view is scrolling down but not consuming, it's probably be at
                // the top of it's content
                scroll(parent, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0)
                // Set the expanding flag so that onNestedPreScroll doesn't handle any events
                skipNestedPreScroll = true
            } else {
                // As we're no longer handling nested scrolls, reset the skip flag
                skipNestedPreScroll = false
            }
        }

        override fun onStopNestedScroll(parent: CoordinatorLayout, child: AppBarLayout, target: View, type: Int) {
            Log.d("Behavior", "onStopNestedScroll $child")
            // Reset the skip flag
            skipNestedPreScroll = false
        }

        override fun onNestedFling(
            parent: CoordinatorLayout, child: AppBarLayout, target: View,
            velocityX: Float, velocityY: Float, consumed: Boolean
        ): Boolean {
            Log.d("Behavior", "onNestedFling $child")
            if (!consumed) {
                // It has been consumed so let's fling ourselves
                return fling(parent, child, -child.getTotalScrollRange(), 0, -velocityY)
            } else {
                // If we're scrolling up and the child also consumed the fling. We'll fake scroll
                // upto our 'collapsed' offset
                val targetScroll: Int
                if (velocityY < 0) {
                    // We're scrolling down
                    targetScroll = -child.getTotalScrollRange() + child.getDownNestedPreScrollRange()
                    if (siblingOffsetTop > targetScroll) {
                        // If we're currently expanded more than the target scroll, we'll return false
                        // now. This is so that we don't 'scroll' the wrong way.
                        return false
                    }
                } else {
                    // We're scrolling up
                    targetScroll = -child.getUpNestedPreScrollRange()
                    if (siblingOffsetTop < targetScroll) {
                        // If we're currently expanded less than the target scroll, we'll return
                        // false now. This is so that we don't 'scroll' the wrong way.
                        return false
                    }
                }

                if (siblingOffsetTop != targetScroll) {
                    animateOffsetTo(parent, child, targetScroll)
                    return true
                }
            }
            return false
        }

        private fun scroll(
            parent: CoordinatorLayout, child: AppBarLayout,
            dy: Int, minOffset: Int, maxOffset: Int
        ): Int {
            return setAppBarTopBottomOffset(parent, child, siblingOffsetTop - dy, minOffset, maxOffset)
        }

        fun setAppBarTopBottomOffset(
            parent: CoordinatorLayout, child: AppBarLayout, newOffset: Int,
            minOffset: Int = Int.MIN_VALUE, maxOffset: Int = Int.MAX_VALUE
        ): Int {
            var offset = newOffset
            val curOffset = siblingOffsetTop
            var consumed = 0
            if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
                // If we have some scrolling range, and we're currently within the min and max
                // offsets, calculate a new offset
                offset = Util.constrain(offset, minOffset, maxOffset)
                if (curOffset != offset) {
                    val offsetChanged = viewOffsetHelper?.setTopAndBottomOffset(offset) ?: false
                    // Update how much dy we have consumed
                    consumed = curOffset - offset
                    // Update the stored sibling offset
                    siblingOffsetTop = offset

                    // Dispatch the updates to any listeners
                    dispatchOffsetUpdates(child)
                }
            }
            return consumed
        }

        private fun animateOffsetTo(parent: CoordinatorLayout, child: AppBarLayout, offset: Int) {
            animator.cancel()
            animator.removeAllUpdateListeners()
            animator.addUpdateListener { animator ->
                setAppBarTopBottomOffset(parent, child, animator.animatedValue as Int)
            }
            animator.setIntValues(siblingOffsetTop, offset)
            animator.start()
        }

        private fun fling(
            parent: CoordinatorLayout, child: AppBarLayout, minOffset: Int,
            maxOffset: Int, velocityY: Float
        ): Boolean {
            if (flingRunnable != null) {
                child.removeCallbacks(flingRunnable)
            }

            scroller.fling(
                0, siblingOffsetTop, // curr
                0, Math.round(velocityY), // velocity.
                0, 0, // x
                minOffset, maxOffset // y
            )

            if (scroller.computeScrollOffset()) {
                flingRunnable = FlingRunnable(parent, child)
                child.postOnAnimation(flingRunnable)
                return true
            } else {
                flingRunnable = null
                return false
            }
        }

        private inner class FlingRunnable internal constructor(
            private val parent: CoordinatorLayout,
            private val child: AppBarLayout
        ) : Runnable {
            override fun run() {
                if (scroller.computeScrollOffset()) {
                    setAppBarTopBottomOffset(parent, child, scroller.currY)
                    // Post ourselves so that we run on the next animation
                    child.postOnAnimation(this)
                }
            }
        }

        private fun dispatchOffsetUpdates(child: AppBarLayout) {
            child.listeners.forEach {
                it.onOffsetChanged(child, getTopAndBottomOffset())
            }
        }

        internal fun getTopAndBottomOffset(): Int {
            return viewOffsetHelper?.getTopAndBottomOffset() ?: 0
        }
    }

    /**
     * Behavior which should be used by [View]s which can scroll vertically and support
     * nested scrolling to automatically scroll any [AppBarLayout] siblings.
     */
    class ScrollingViewBehavior<V : View> : CoordinatorLayout.Behavior<V> {
        var overlayTop: Int = 0

        private var viewOffsetHelper: ViewOffsetHelper? = null

        constructor() {}
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ScrollingViewBehavior_Params)
            overlayTop = a.getDimensionPixelSize(R.styleable.ScrollingViewBehavior_Params_behavior_overlapTop, 0)
            a.recycle()
        }

        override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
            Log.d("ScrollingBehavior", "onLayoutChild $child")
            // First let the parent lay it out
            parent.onLayoutChild(child, layoutDirection)
            if (viewOffsetHelper == null) {
                viewOffsetHelper = ViewOffsetHelper(child)
            }
            viewOffsetHelper?.onViewLayout()
            return true
        }

        override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
            Log.d("ScrollingBehavior", "layoutDependsOn $child")
            // We depend on any AppBarLayouts
            return dependency is AppBarLayout
        }

        override fun onMeasureChild(
            parent: CoordinatorLayout, child: V, parentWidthMeasureSpec: Int, widthUsed: Int,
            parentHeightMeasureSpec: Int, heightUsed: Int
        ): Boolean {
            Log.d("ScrollingBehavior", "onMeasureChild $child")
            if (child.layoutParams.height == LinearLayout.LayoutParams.MATCH_PARENT) {
                // If the child's height is set to match_parent then it with it's maximum visible
                // visible height
                val dependencies = parent.getDependencies(child)
                if (dependencies.isEmpty()) {
                    // If we don't have any dependencies, return false
                    return false
                }

                val appBar = dependencies.firstOrNull() as? AppBarLayout
                if (appBar != null) {
                    if (appBar.fitsSystemWindows) {
                        // If the AppBarLayout is fitting system windows then we need to also,
                        // otherwise we'll get CoL's compatible layout functionality
                        child.fitsSystemWindows = true
                    }
                    val scrollRange = appBar.getTotalScrollRange()
                    val height = parent.measuredHeight - appBar.measuredHeight + scrollRange
                    val heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
                    // Now measure the scrolling child with the correct height
                    parent.onMeasureChild(
                        child, parentWidthMeasureSpec,
                        widthUsed, heightMeasureSpec, heightUsed
                    )
                    return true
                }
            }
            return false
        }

        override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
            Log.d("ScrollingBehavior", "onDependentViewChanged $child")
            val behavior = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior
            if (behavior is Behavior) {
                // Offset the child so that it is below the app-bar (with any overlap)
                val appBarOffset = behavior.siblingOffsetTop
                val expandedMax = dependency.height - overlayTop
                val collapsedMin = parent.height - child.height
                if (overlayTop != 0 && dependency is AppBarLayout) {
                    // If we have an overlap top, and the dependency is an AppBarLayout, we control
                    // the offset ourselves based on the appbar's scroll progress. This is so that
                    // the scroll happens sequentially rather than linearly
                    val scrollRange = dependency.totalScrollRange
                    val offset = Util.lerp(
                        expandedMax, collapsedMin, Math.abs(appBarOffset) / scrollRange.toFloat()
                    )
                    viewOffsetHelper?.setTopAndBottomOffset(offset)
                } else {
                    val amount = dependency.height - overlayTop + appBarOffset
                    val offset = Util.constrain(amount, collapsedMin, expandedMax)
                    viewOffsetHelper?.setTopAndBottomOffset(offset)
                }
            }
            return false
        }
    }

    internal object Util {

        fun constrain(amount: Int, low: Int, high: Int): Int {
            return if (amount < low) low else if (amount > high) high else amount
        }

        fun lerp(startValue: Int, endValue: Int, fraction: Float): Int {
            return startValue + Math.round(fraction * (endValue - startValue))
        }
    }
}