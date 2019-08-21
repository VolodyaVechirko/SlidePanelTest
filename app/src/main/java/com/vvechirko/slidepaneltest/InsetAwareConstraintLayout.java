package com.vvechirko.slidepaneltest;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetAwareConstraintLayout extends ConstraintLayout {

    // accessible from xml [R.id.topInset]
    private Guideline topGuideline;
    // accessible from xml [R.id.bottomInset]
    private Guideline bottomGuideline;
    // accessible from xml [R.id.leftInset]
    private Guideline leftGuideline;
    // accessible from xml [R.id.rightInset]
    private Guideline rightGuideline;

    public InsetAwareConstraintLayout(Context context) {
        this(context, null);
    }

    public InsetAwareConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InsetAwareConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public InsetAwareConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        // create and add topInset [Guideline]
        topGuideline = new Guideline(getContext());
        topGuideline.setId(R.id.topInset);
        LayoutParams lp1 = generateDefaultLayoutParams();
        lp1.guideBegin = 0;
        lp1.orientation = LayoutParams.HORIZONTAL;
        addView(topGuideline, lp1);

        // create and add bottomInset [Guideline]
        bottomGuideline = new Guideline(getContext());
        bottomGuideline.setId(R.id.bottomInset);
        LayoutParams lp2 = generateDefaultLayoutParams();
        lp2.guideEnd = 0;
        lp2.orientation = LayoutParams.HORIZONTAL;
        addView(bottomGuideline, lp2);

        // create and add leftInset [Guideline]
        leftGuideline = new Guideline(getContext());
        leftGuideline.setId(R.id.leftInset);
        LayoutParams lp3 = generateDefaultLayoutParams();
        lp3.guideBegin = 0;
        lp3.orientation = LayoutParams.VERTICAL;
        addView(leftGuideline, lp3);

        // create and add rightInset [Guideline]
        rightGuideline = new Guideline(getContext());
        rightGuideline.setId(R.id.rightInset);
        LayoutParams lp4 = generateDefaultLayoutParams();
        lp4.guideEnd = 0;
        lp4.orientation = LayoutParams.VERTICAL;
        addView(rightGuideline, lp4);

        setupForInsets();
    }

//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        if (ViewCompat.getFitsSystemWindows(this)) {
////             We should request a new dispatch of window insets
//            ViewCompat.requestApplyInsets(this);
//        }
//    }

    @Override
    public void setFitsSystemWindows(boolean fitSystemWindows) {
        super.setFitsSystemWindows(fitSystemWindows);
        setupForInsets();
    }

    private void setupForInsets() {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }

        if (ViewCompat.getFitsSystemWindows(this)) {
            // First apply the insets listener
            ViewCompat.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                    return applyWindowInsets(insets);
                }
            });

            // Now set the sys ui flags to enable us to lay out in the window insets
            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(this, null);
        }
    }

    private WindowInsetsCompat applyWindowInsets(WindowInsetsCompat insets) {
        int topInset = insets.getSystemWindowInsetTop();
        int bottomInset = insets.getSystemWindowInsetBottom();
        int leftInset = insets.getSystemWindowInsetLeft();
        int rightInset = insets.getSystemWindowInsetRight();

        ((LayoutParams) topGuideline.getLayoutParams()).guideBegin = topInset;
        ((LayoutParams) bottomGuideline.getLayoutParams()).guideEnd = bottomInset;
        ((LayoutParams) leftGuideline.getLayoutParams()).guideBegin = leftInset;
        ((LayoutParams) rightGuideline.getLayoutParams()).guideEnd = rightInset;

//        requestLayout();
        return insets.consumeSystemWindowInsets();
    }
}