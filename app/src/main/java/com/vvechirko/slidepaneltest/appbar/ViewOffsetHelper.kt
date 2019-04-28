package com.vvechirko.slidepaneltest.appbar

import android.view.View
import androidx.core.view.ViewCompat

/**
 * Utility helper for moving a [android.view.View] around using
 * [android.view.View.offsetLeftAndRight] and
 * [android.view.View.offsetTopAndBottom].
 *
 *
 * Also the setting of absolute offsets (similar to translationX/Y), rather than additive
 * offsets.
 */
internal class ViewOffsetHelper(private val mView: View) {
    private var mLayoutTop: Int = 0
    private var mLayoutLeft: Int = 0
    private var mOffsetTop: Int = 0
    private var mOffsetLeft: Int = 0

    fun onViewLayout() {
        // Now grab the intended top
        mLayoutTop = mView.top
        mLayoutLeft = mView.left
    }

    /**
     * Set the top and bottom offset for this [ViewOffsetHelper]'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    fun setTopAndBottomOffset(offset: Int): Boolean {
        if (mOffsetTop != offset) {
            mOffsetTop = offset
            ViewCompat.offsetTopAndBottom(mView, mOffsetTop - mView.top - mLayoutTop)
            return true
        }
        return false
    }

    /**
     * Set the left and right offset for this [ViewOffsetHelper]'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    fun setLeftAndRightOffset(offset: Int): Boolean {
        if (mOffsetLeft != offset) {
            mOffsetLeft = offset
            ViewCompat.offsetLeftAndRight(mView, mOffsetLeft - mView.left - mLayoutLeft)
            return true
        }
        return false
    }

    fun getTopAndBottomOffset(): Int {
        return mOffsetTop
    }

    fun getLeftAndRightOffset(): Int {
        return mOffsetLeft
    }
}