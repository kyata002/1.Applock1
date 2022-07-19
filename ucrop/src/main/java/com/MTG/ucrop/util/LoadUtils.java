package com.MTG.ucrop.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

public class LoadUtils {
    public static void getBitmapFromPathForSticker(Context context, String path, OnLoadBitmapListener onLoadBitmapListener) {
        int width = (int) (2 / 3f * context.getResources().getDisplayMetrics().widthPixels);
        getBitmapFromPath(context, path, width, onLoadBitmapListener);
    }

    public static void getBitmapFromPathForSticker(Activity activity, String path, OnLoadBitmapListener onLoadBitmapListener) {
        int width = (int) (2 / 3f * activity.getResources().getDisplayMetrics().widthPixels);
        getBitmapFromPath(activity, path, width, onLoadBitmapListener);
    }

    public static void getBitmapFromPathForCollage(Activity activity, String path, OnLoadBitmapListener onLoadBitmapListener) {
        int width = activity.getResources().getDisplayMetrics().widthPixels;
        if (width > 1080) {
            width = 1080;
        }
        getBitmapFromPath(activity, path, width, onLoadBitmapListener);
    }

    public static void getBitmapFromPathForScrapbook(Activity activity, String path, int index, OnLoadBitmapListenerV2 onLoadBitmapListenerV2) {
        int width = activity.getResources().getDisplayMetrics().widthPixels;
        getBitmapFromPathForScrapbook(activity, path, width, index, onLoadBitmapListenerV2);
    }

    public static void getBitmapBlurFromPath(Context context, String path, OnLoadBitmapListener onLoadBitmapListener) {
        int width = (int) (2 / 3f * context.getResources().getDisplayMetrics().widthPixels);
        getBitmapFromPath(context, path, width, onLoadBitmapListener);
    }

    public static void getBitmapFromPath(Context context, String path, OnLoadBitmapListener onLoadBitmapListener) {
        float delta;
        if (checkRamSize(context)) {
            delta = 1.3f;
        } else {
            delta = 1.0f;
        }
        int width = (int) (delta * context.getResources().getDisplayMetrics().widthPixels);
        getBitmapFromPath(context, path, width, onLoadBitmapListener);
    }

    public static void getBitmapFromPathForView(Context context, String path, OnLoadBitmapListener onLoadBitmapListener) {
        float delta;
        if (checkRamSize(context)) {
            delta = 1.3f;
        } else {
            delta = 1.0f;
        }
        int width = (int) (delta * context.getResources().getDisplayMetrics().widthPixels);
        getBitmapFromPathForView(context, path, width, onLoadBitmapListener);
    }

    public static void getBitmapFromPath(Activity activity, String path, OnLoadBitmapListener onLoadBitmapListener) {
        float delta;
        if (checkRamSize(activity)) {
            delta = 1.3f;
        } else {
            delta = 1.0f;
        }
        int width = (int) (delta * activity.getResources().getDisplayMetrics().widthPixels);
        getBitmapFromPath(activity, path, width, onLoadBitmapListener);
    }

    public static void getBitmapFromPath(Context context, String path, int threshold, OnLoadBitmapListener onLoadBitmapListener) {
        if (context == null) return;
        Glide.with(context)
                .asBitmap()
                .load(path)
                .apply(new RequestOptions()
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(threshold))
                .addListener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (context instanceof Activity) {
                            if (((Activity) context).isFinishing()) return false;
                        }
                        onLoadBitmapListener.onLoadFailed();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (context instanceof Activity) {
                            if (((Activity) context).isFinishing()) return false;
                        }
//                        Log.d("TAG:" + context.getClass().getSimpleName(), "width = " + resource.getWidth() + ", height = " + resource.getHeight() + ", threshold = " + threshold);
                        onLoadBitmapListener.onLoadSuccess(resource);
                        return false;
                    }
                }).submit();
    }


    public static void getBitmapFromPathForView(Context context, String path, int threshold, OnLoadBitmapListener onLoadBitmapListener) {
        if (context == null) return;
        Glide.with(context)
                .asBitmap()
                .load(path)
                .apply(new RequestOptions()
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(threshold))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        if (context instanceof Activity) {
                            if (((Activity) context).isFinishing()) return;
                        }
//                        Log.d("TAG:V2" + context.getClass().getSimpleName(), "width = " + resource.getWidth() + ", height = " + resource.getHeight() + ", threshold = " + threshold);
                        onLoadBitmapListener.onLoadSuccess(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        if (context instanceof Activity) {
                            if (((Activity) context).isFinishing()) return;
                        }
                        onLoadBitmapListener.onLoadFailed();
                    }
                });
    }

    public static void getBitmapFromPath(Activity activity, String path, int threshold, OnLoadBitmapListener onLoadBitmapListener) {
        if (activity == null) return;
        Glide.with(activity)
                .asBitmap()
                .load(path)
                .apply(new RequestOptions()
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(threshold))
                .addListener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (activity.isFinishing()) return false;
                        onLoadBitmapListener.onLoadFailed();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (activity.isFinishing()) return false;
//                        Log.d("TAG:V3:" + activity.getClass().getSimpleName(), "width = " + resource.getWidth() + ", height = " + resource.getHeight() + ", threshold = " + threshold);
                        onLoadBitmapListener.onLoadSuccess(resource);
                        return false;
                    }
                }).submit();
    }

    public static void getBitmapFromPathForScrapbook(Activity activity, String path, int threshold, final int index, OnLoadBitmapListenerV2 onLoadBitmapListenerV2) {
        if (activity == null) return;
        Glide.with(activity)
                .asBitmap()
                .load(path)
                .apply(new RequestOptions()
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(threshold)
                        .centerCrop())
                .addListener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (activity.isFinishing()) return false;
                        onLoadBitmapListenerV2.onLoadFailed();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (activity.isFinishing()) return false;
//                        Log.d("TAG:V4:" + activity.getClass().getSimpleName(), "width = " + resource.getWidth() + ", height = " + resource.getHeight() + ", threshold = " + threshold);
                        onLoadBitmapListenerV2.onLoadSuccess(resource, index);
                        return false;
                    }
                }).submit();
    }

    private static boolean checkRamSize(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        float totalMemory = memInfo.totalMem / 1073741824.0f;
        return totalMemory > 2.0;
    }

    public interface OnLoadBitmapListener {
        void onLoadSuccess(Bitmap bitmap);

        void onLoadFailed();
    }

    public interface OnLoadBitmapListenerV2 {
        void onLoadSuccess(Bitmap bitmap, int indexV2);

        void onLoadFailed();
    }
}
