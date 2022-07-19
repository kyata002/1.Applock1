package com.MTG.library.customview.imagezoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;

public class ImageViewTouch extends ImageViewTouchBase {
    static final float SCROLL_DELTA_THRESHOLD = 1.0f;
    protected ScaleGestureDetector mScaleDetector;
    protected GestureDetector mGestureDetector;
    protected int mTouchSlop;
    protected float mScaleFactor;
    protected int mDoubleTapDirection;
    protected OnGestureListener mGestureListener;
    protected OnScaleGestureListener mScaleListener;
    protected boolean mDoubleTapEnabled = true;
    protected boolean mScaleEnabled = true;
    protected boolean mScrollEnabled = true;
    private OnImageViewTouchDoubleTapListener mDoubleTapListener;
    private OnImageViewTouchSingleTapListener mSingleTapListener;
    private OnImageFlingListener mFlingListener;
    private OnScaleImageListener mOnScaleImageListener;
    protected boolean mDisableZoom = false;
    protected boolean mHasMove = true;
    private ColorMatrix mColorMatrix;
    private ColorMatrix mHueMatrix;
    private ColorMatrix mSaturationMatrix;
    private ColorMatrix mLuminanceMatrix;
    private ColorMatrixColorFilter mColorMatrixColorFilter;

    public ImageViewTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mGestureListener = getGestureListener();
        mScaleListener = getScaleListener();
        mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
        mGestureDetector = new GestureDetector(getContext(), mGestureListener, null, true);
        mDoubleTapDirection = 1;
        //
        mColorMatrix = new ColorMatrix();
        mHueMatrix = new ColorMatrix();
        mSaturationMatrix = new ColorMatrix();
        mLuminanceMatrix = new ColorMatrix();
        mHueMatrix.setRotate(0, 0);
        mHueMatrix.setRotate(1, 0);
        mHueMatrix.setRotate(2, 0);
        mSaturationMatrix.setSaturation(1);
        mLuminanceMatrix.setScale(1, 1, 1, 1);
        mColorMatrix = new ColorMatrix();
        mColorMatrix.postConcat(mHueMatrix);
        mColorMatrix.postConcat(mSaturationMatrix);
        mColorMatrix.postConcat(mLuminanceMatrix);
        mColorMatrixColorFilter = new ColorMatrixColorFilter(mColorMatrix);
        setColorFilter(mColorMatrixColorFilter);
    }

    public void setSaturation(float sat) {
        mSaturationMatrix.setSaturation(sat);
        mColorMatrix = new ColorMatrix();
        mColorMatrix.postConcat(mHueMatrix);
        mColorMatrix.postConcat(mSaturationMatrix);
        mColorMatrix.postConcat(mLuminanceMatrix);
        mColorMatrixColorFilter = new ColorMatrixColorFilter(mColorMatrix);
        setColorFilter(mColorMatrixColorFilter);
        postInvalidate();
    }

    public void setHue(float hue) {
        mHueMatrix.setRotate(0, hue);
        mHueMatrix.setRotate(1, hue);
        mHueMatrix.setRotate(2, hue);
        mColorMatrix = new ColorMatrix();
        mColorMatrix.postConcat(mHueMatrix);
        mColorMatrix.postConcat(mSaturationMatrix);
        mColorMatrix.postConcat(mLuminanceMatrix);
        mColorMatrixColorFilter = new ColorMatrixColorFilter(mColorMatrix);
        setColorFilter(mColorMatrixColorFilter);
        postInvalidate();
    }

    public void setLuminance(float lum) {
        mLuminanceMatrix.setScale(lum, lum, lum, 1);
        mColorMatrix = new ColorMatrix();
        mColorMatrix.postConcat(mHueMatrix);
        mColorMatrix.postConcat(mSaturationMatrix);
        mColorMatrix.postConcat(mLuminanceMatrix);
        mColorMatrixColorFilter = new ColorMatrixColorFilter(mColorMatrix);
        setColorFilter(mColorMatrixColorFilter);
        postInvalidate();
    }

    public void setDoubleTapListener(OnImageViewTouchDoubleTapListener listener) {
        mDoubleTapListener = listener;
    }

    public void setSingleTapListener(OnImageViewTouchSingleTapListener listener) {
        mSingleTapListener = listener;
    }

    public void setFlingListener(OnImageFlingListener listener) {
        mFlingListener = listener;
    }

    public void setOnScaleImageListener(OnScaleImageListener onScaleImageListener) {
        mOnScaleImageListener = onScaleImageListener;
    }

    public void setScaleEnabled(boolean value) {
        mScaleEnabled = value;
        setDoubleTapEnabled(value);
    }

    public void setScrollEnabled(boolean value) {
        mScrollEnabled = value;
    }

    public boolean getDoubleTapEnabled() {
        return mDoubleTapEnabled;
    }

    public void setDoubleTapEnabled(boolean value) {
        mDoubleTapEnabled = value;
    }

    protected OnGestureListener getGestureListener() {
        return new GestureListener();
    }

    protected OnScaleGestureListener getScaleListener() {
        return new ScaleListener();
    }

    public boolean isDisableZoom() {
        return mDisableZoom;
    }

    public void setDisableZoom(boolean disableZoom) {
        mDisableZoom = disableZoom;
    }

    @Override
    protected void _setImageDrawable(final Drawable drawable,
                                     final Matrix initial_matrix, float min_zoom, float max_zoom) {
        super._setImageDrawable(drawable, initial_matrix, min_zoom, max_zoom);
        mScaleFactor = getMaxScale() / 3;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDisableZoom) return true;
        mScaleDetector.onTouchEvent(event);
        if (!mScaleDetector.isInProgress()) {
            mGestureDetector.onTouchEvent(event);
        }
        int action = event.getActionMasked();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (mBitmapSticker != null) {
                    if (getScale() < 1f) {
                        zoomTo(1f, 500);
                    }
                } else {
                    if (getScale() < getMinScale()) {
                        zoomTo(getMinScale(), 500);
                    }
                }
                break;
        }
        return true;
    }

    public void setBitmapSticker(Bitmap bitmapSticker) {
        mBitmapSticker = bitmapSticker;
        invalidate();
    }

    @Override
    protected void onZoomAnimationCompleted(float scale) {
        if (LOG_ENABLED) {
            Log.d(LOG_TAG,
                  "onZoomAnimationCompleted. scale: " + scale + ", minZoom: " + getMinScale());
        }
        if (mOnScaleImageListener != null) {
            mOnScaleImageListener.onScaleImage(getScale());
        }
        if (scale < getMinScale()) {
            zoomTo(getMinScale(), 50);
        }
    }

    // sửa ngày 27/12/2019 giờ double tap sẽ luôn luôn trở về kích thước mặc định
    protected float onDoubleTapPost(float scale, float maxZoom) {
//        if (mDoubleTapDirection == 1) {
//            if ((scale + (mScaleFactor * 2)) <= maxZoom) {
//                return scale + mScaleFactor;
//            } else {
//                mDoubleTapDirection = -1;
//                return maxZoom;
//            }
//        } else {
//            mDoubleTapDirection = 1;
//            return 1f;
//        }
        return 1f;
    }

    public void setHasMove(boolean hasMove) {
        mHasMove = hasMove;
        invalidate();
    }

    public boolean hasMove() {
        return mHasMove;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mHasMove) {
            // thực hiện có cho phép di chuyển ảnh đc ko ?
            onScroll(distanceX, distanceY);
            if (mFlingListener != null) {
                mFlingListener.onFling(e1, e2, distanceX, distanceY);
            }
            return true;
        }
        if (!mScrollEnabled) {
            return false;
        }
        if (e1 == null || e2 == null) {
            return false;
        }
        if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
            return false;
        }
        if (mScaleDetector.isInProgress()) {
            return false;
        }
        if (getScale() == 1f) {
            return false;
        }
        mUserScaled = true;
        scrollBy(-distanceX, -distanceY);
        invalidate();
        return true;
    }

    public boolean onScroll(float distanceX, float distanceY) {
        mSuppMatrix.postTranslate(-distanceX, -distanceY);
        setImageMatrix(getImageViewMatrix());
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        if (!mScrollEnabled) {
            return false;
        }
        if (mFlingListener != null) {
            mFlingListener.onFling(e1, e2, velocityX, velocityY);
        }
        if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
            return false;
        }
        if (mScaleDetector.isInProgress()) {
            return false;
        }
        if (getScale() == 1f) {
            return false;
        }
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();
        if (Math.abs(velocityX) > 800 || Math.abs(velocityY) > 800) {
            mUserScaled = true;
            scrollBy(diffX / 2, diffY / 2, 300);
            invalidate();
            return true;
        }
        return false;
    }

    /**
     * Determines whether this ImageViewTouch can be scrolled.
     *
     * @param direction - positive direction value means scroll from right to left,
     *                  negative value means scroll from left to right
     *
     * @return true if there is some more place to scroll, false - otherwise.
     */
    public boolean canScroll(int direction) {
        RectF bitmapRect = getBitmapRect();
        updateRect(bitmapRect, mScrollRect);
        Rect imageViewRect = new Rect();
        getGlobalVisibleRect(imageViewRect);
        if (null == bitmapRect) {
            return false;
        }
        if (bitmapRect.right >= imageViewRect.right) {
            if (direction < 0) {
                return Math.abs(bitmapRect.right - imageViewRect.right) > SCROLL_DELTA_THRESHOLD;
            }
        }
        double bitmapScrollRectDelta = Math.abs(bitmapRect.left - mScrollRect.left);
        return bitmapScrollRectDelta > SCROLL_DELTA_THRESHOLD;
    }

    public void resetImage() {
        float scale = getScale();
        float targetScale = scale;
        targetScale = Math.min(getMaxScale(), Math.max(targetScale, getMinScale()));
        zoomTo(targetScale, 0, 0, DEFAULT_ANIMATION_DURATION);
        invalidate();
    }

    public interface OnImageViewTouchDoubleTapListener {
        void onDoubleTap();
    }

    public interface OnImageViewTouchSingleTapListener {
        void onSingleTapConfirmed();
    }

    public interface OnScaleImageListener {
        void onScaleImage(float scale);
    }

    public interface OnImageFlingListener {
        void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (null != mSingleTapListener) {
                mSingleTapListener.onSingleTapConfirmed();
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i(LOG_TAG, "onDoubleTap. double tap enabled? " + mDoubleTapEnabled);
            if (mDoubleTapEnabled) {
                mUserScaled = true;
                float scale = getScale();
                float targetScale = scale;
                targetScale = onDoubleTapPost(scale, getMaxScale());
                targetScale = Math.min(getMaxScale(), Math.max(targetScale, getMinScale()));
                zoomTo(targetScale, e.getX(), e.getY(), DEFAULT_ANIMATION_DURATION);
                invalidate();
            }
            if (null != mDoubleTapListener) {
                mDoubleTapListener.onDoubleTap();
            }
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (isLongClickable()) {
                if (!mScaleDetector.isInProgress()) {
                    setPressed(true);
                    performLongClick();
                }
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1 != null && e2 != null && e1.getPointerCount() <= 1 &&
                    e2.getPointerCount() <= 1) {
                ImageViewTouch.this.onScroll(e1, e2, distanceX, distanceY);
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return ImageViewTouch.this.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        protected boolean mScaled = false;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float span = detector.getCurrentSpan() - detector.getPreviousSpan();
            float targetScale = getScale() * detector.getScaleFactor();
            if (mScaleEnabled) {
                if (mScaled && span != 0) {
                    mUserScaled = true;
                    targetScale =
                            Math.min(getMaxScale(), Math.max(targetScale, getMinScale() - 0.1f));
                    zoomTo(targetScale, detector.getFocusX(), detector.getFocusY());
                    mDoubleTapDirection = 1;
                    onZoomAnimationCompleted(getScale());
                    invalidate();
                    return true;
                }
                if (!mScaled) {
                    mScaled = true;
                }
            }
            return true;
        }
    }
}
