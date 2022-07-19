package com.MTG.AppLock.ui.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.androidhiddencamera.*
import com.androidhiddencamera.config.CameraFacing
import java.io.File

abstract class IntruderHiddenCameraActivity<VM : ViewModel> : BaseActivity<VM>(), CameraCallbacks {
    private var mCameraPreview: CameraPreview? = null
    private var mCachedCameraConfig: CameraConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCameraPreview = addPreView()
    }

    override fun onDestroy() {
        super.onDestroy()
        //stop preview and release the camera.
        stopCamera()
    }

    /**
     * Start the hidden camera. Make sure that you check for the runtime permissions before you start
     * the camera.
     *
     * @param cameraConfig camera configuration [CameraConfig]
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    protected open fun startCamera(cameraConfig: CameraConfig) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //check if the camera permission is available
            onCameraError(CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE)
        } else if (cameraConfig.facing == CameraFacing.FRONT_FACING_CAMERA && !HiddenCameraUtils.isFrontCameraAvailable(this)) {   //Check if for the front camera
            onCameraError(CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA)
        } else {
            mCachedCameraConfig = cameraConfig
            mCameraPreview?.startCameraInternal(cameraConfig)
        }
    }

    /**
     * Call this method to capture the image using the camera you initialized. Don't forget to
     * initialize the camera using [.startCamera] before using this function.
     */
    protected open fun takePicture(fileSave: File, time: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (mCameraPreview != null) {
            if (mCameraPreview?.isSafeToTakePictureInternal == true) {
                mCameraPreview?.takePictureInternal(fileSave, time)
            }
        } else {
            throw RuntimeException("Background camera not initialized. Call startCamera() to initialize the camera.")
        }
    }

    /**
     * Stop and release the camera forcefully.
     */
    protected open fun stopCamera() {
        mCachedCameraConfig = null //Remove config.
        mCameraPreview?.stopPreviewAndFreeCamera()
    }

    /**
     * Add camera preview to the root of the activity layout.
     *
     * @return [CameraPreview] that was added to the view.
     */
    open fun addPreView(): CameraPreview? {
        //create fake camera view
        val cameraSourceCameraPreview = CameraPreview(this, this)
        cameraSourceCameraPreview.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        when (val view = (window.decorView.rootView as ViewGroup).getChildAt(0)) {
            is LinearLayout -> {
                val params = LinearLayout.LayoutParams(1, 1)
                view.addView(cameraSourceCameraPreview, params)
            }
            is RelativeLayout -> {
                val params = RelativeLayout.LayoutParams(1, 1)
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                view.addView(cameraSourceCameraPreview, params)
            }
            is FrameLayout -> {
                val params = FrameLayout.LayoutParams(1, 1)
                view.addView(cameraSourceCameraPreview, params)
            }
            else -> {
                throw RuntimeException("Root view of the activity/fragment cannot be other than Linear/Relative/Frame layout")
            }
        }
        return cameraSourceCameraPreview
    }

    override fun onResume() {
        super.onResume()
        if (mCachedCameraConfig != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mCachedCameraConfig?.let {
                startCamera(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mCameraPreview?.stopPreviewAndFreeCamera()
    }

    override fun onCameraError(errorCode: Int) {
    }

    override fun onImageCapture(imageFile: File, time: String) {
    }
}