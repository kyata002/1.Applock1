package com.MTG.library.customview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import android.widget.ImageView;

public class CustomImageView extends ImageView {
    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        //force a 16:9 aspect ratio             
        int height = Math.round(width * 1280 / 720f);
        setMeasuredDimension(width, height);
    }
}