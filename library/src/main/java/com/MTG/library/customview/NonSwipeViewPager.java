package com.MTG.library.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class NonSwipeViewPager extends ViewPager {
    private boolean mSwipe = true;

    public NonSwipeViewPager(@NonNull Context context) {
        super(context);
    }

    public NonSwipeViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mSwipe) {
            return super.onInterceptTouchEvent(ev);
        }
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mSwipe) {
            return super.onTouchEvent(ev);
        }
        // Never allow swiping to switch between pages
        return false;
    }

    public void setSwipe(boolean swipe) {
        mSwipe = swipe;
    }
}
