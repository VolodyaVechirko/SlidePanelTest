package com.vvechirko.slidepaneltest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FitWindowFrameLayout extends FrameLayout {

    private WindowInsetsCompat mLastInsets;
    private boolean mDrawStatusBarBackground;
    private Drawable mStatusBarBackground;

    private androidx.core.view.OnApplyWindowInsetsListener mApplyWindowInsetsListener;

    public FitWindowFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public FitWindowFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitWindowFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        mStatusBarBackground = new ColorDrawable(Color.MAGENTA);
        setupForInsets();
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        Log.d("WindowInsets", "onApplyWindowInsets " + insets.toString());
        return super.onApplyWindowInsets(insets);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mLastInsets == null && ViewCompat.getFitsSystemWindows(this)) {
            // We're set to fitSystemWindows but we haven't had any insets yet...
            // We should request a new dispatch of window insets
            ViewCompat.requestApplyInsets(this);
        }
    }

//    @Override
//    public void addView(View child, int index, ViewGroup.LayoutParams params) {
//        super.addView(child, index, params);
//        if (mStatusBarBackground == null) {
//            mStatusBarBackground = child.getBackground();
//        }
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//        for (int i = 0; i < getChildCount(); i++) {
//            final View child = getChildAt(i);
//            if (child.getVisibility() != GONE) {
//                layoutChild(child, ViewCompat.getLayoutDirection(this));
//            }
//        }
        final int count = getChildCount();

        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = Gravity.TOP | Gravity.START;
                }

                final int layoutDirection = getLayoutDirection();
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = parentRight - width - lp.rightMargin;
                        break;
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }

                if (mLastInsets != null && ViewCompat.getFitsSystemWindows(this)
                        && !ViewCompat.getFitsSystemWindows(child)) {
                    // If we're set to handle insets but this child isn't, then it has been measured as
                    // if there are no insets. We need to lay it out to match.
                    childLeft += mLastInsets.getSystemWindowInsetLeft();
                    childTop += mLastInsets.getSystemWindowInsetTop();
                }

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    private void layoutChild(View child, int layoutDirection) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final Rect parent = new Rect();
        parent.set(getPaddingLeft() + lp.leftMargin,
                getPaddingTop() + lp.topMargin,
                getWidth() - getPaddingRight() - lp.rightMargin,
                getHeight() - getPaddingBottom() - lp.bottomMargin);

        if (mLastInsets != null && ViewCompat.getFitsSystemWindows(this)
                && !ViewCompat.getFitsSystemWindows(child)) {
            // If we're set to handle insets but this child isn't, then it has been measured as
            // if there are no insets. We need to lay it out to match.
            parent.left += mLastInsets.getSystemWindowInsetLeft();
            parent.top += mLastInsets.getSystemWindowInsetTop();
            parent.right -= mLastInsets.getSystemWindowInsetRight();
            parent.bottom -= mLastInsets.getSystemWindowInsetBottom();
        }

        final Rect out = new Rect();
        GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(),
                child.getMeasuredHeight(), parent, out, layoutDirection);
        child.layout(out.left, out.top, out.right, out.bottom);
    }

    private static int resolveGravity(int gravity) {
        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.NO_GRAVITY) {
            gravity |= GravityCompat.START;
        }
        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.NO_GRAVITY) {
            gravity |= Gravity.TOP;
        }
        return gravity;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawStatusBarBackground && mStatusBarBackground != null) {
            final int inset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            if (inset > 0) {
                mStatusBarBackground.setBounds(0, 0, getWidth(), inset);
                mStatusBarBackground.draw(canvas);
            }
        }
    }

    private void setupForInsets() {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }

        if (ViewCompat.getFitsSystemWindows(this)) {
            if (mApplyWindowInsetsListener == null) {
                mApplyWindowInsetsListener = new androidx.core.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        return setWindowInsets(insets);
                    }
                };
            }
            // First apply the insets listener
            ViewCompat.setOnApplyWindowInsetsListener(this, mApplyWindowInsetsListener);

            // Now set the sys ui flags to enable us to lay out in the window insets
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(this, null);
        }
    }

    final WindowInsetsCompat setWindowInsets(WindowInsetsCompat insets) {
        Log.d("WindowInsetsCompat", "onApplyWindowInsets " + insets.toString());
        if (!ObjectsCompat.equals(mLastInsets, insets)) {
            mLastInsets = insets;
            mDrawStatusBarBackground = insets != null && insets.getSystemWindowInsetTop() > 0;
            setWillNotDraw(!mDrawStatusBarBackground && getBackground() == null);

            // Now dispatch to the Behaviors
            insets = dispatchApplyWindowInsetsToChildren(insets);
            requestLayout();
        }
        return insets;
    }

    private WindowInsetsCompat dispatchApplyWindowInsetsToChildren(WindowInsetsCompat insets) {
        if (insets.isConsumed()) {
            return insets;
        }

        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            if (ViewCompat.getFitsSystemWindows(child)) {
                insets = ViewCompat.onApplyWindowInsets(child, insets);
                if (insets.isConsumed()) {
                    // If it consumed the insets, break
                    break;
                }
            }
        }

        return insets;
    }
}
