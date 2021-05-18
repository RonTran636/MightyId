package com.mightyId.activities.main.ui.message.chatRoom.captureImage

import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.databinding.ActivityCameraBinding
import com.mightyId.utils.imageProxyToBitmap
import com.mightyId.utils.loadImage
import com.mightyId.utils.simulateClick
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var broadcastManager: LocalBroadcastManager

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    companion object {
        const val IMAGE_CAPTURED = "imageCaptured"
        const val KEY_EVENT_EXTRA = "key_event_extra"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewFinder = binding.previewView
        startCamera()

        binding.cameraCaptureButton.setOnClickListener(this)
        binding.cameraSwitchButton.setOnClickListener(this)
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview =
                Preview.Builder().build().also { it.setSurfaceProvider(viewFinder.surfaceProvider) }
            // ImageCapture
            val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            val rotation = viewFinder.display.rotation
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                // We request aspect ratio but no resolution to match preview config, but letting
                // CameraX optimize for whatever specific resolution best fits our use cases
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Timber.tag("CameraActivity").e("startCamera: $e")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val shutter = binding.cameraCaptureButton
                shutter.simulateClick()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private fun updateCameraSwitchButton() {
        try {
            binding.cameraSwitchButton.isEnabled = hasBackCamera() && hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            binding.cameraSwitchButton.isEnabled = false
        }
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.cameraCaptureButton -> {
                // Get a stable reference of the modifiable image capture use case
                val imageCapture = imageCapture ?: return
                // Create time-stamped output file to hold the image
                val photoFile = File(
                    outputDirectory,
                    ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(Instant.now()) + ".jpg"
                )
                // Create output options object which contains file + metadata
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                // Create executor
                val executors = ContextCompat.getMainExecutor(this)
                // Set up image capture listener, which is triggered after photo has been taken
                imageCapture.takePicture(executors,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            image.imageInfo.rotationDegrees
                            val bitmap = image.imageProxyToBitmap()
                            binding.imageCaptured.loadImage(bitmap, null)
                            binding.confirmButton.apply {
                                visibility = View.VISIBLE
                                setOnClickListener {
                                    Timber.tag("CameraActivity")
                                        .d("onCaptureSuccess confirmButton: Called")
                                    imageCapture.takePicture(
                                        outputOptions,
                                        executors,
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onError(exc: ImageCaptureException) {
                                                Timber.tag("CameraActivity").e("onError: $exc")
                                            }

                                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                val savedUri = Uri.fromFile(photoFile)
                                                val intent = Intent()
                                                intent.putExtra(IMAGE_CAPTURED, savedUri)
                                                setResult(RESULT_OK, intent)
                                                finish()
                                                Timber.tag("CameraActivity")
                                                    .d("onImageSaved: photo saved: $savedUri")
                                            }
                                        })
                                }
                            }
                            super.onCaptureSuccess(image)
                        }
                    })
            }
        }
    }


    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit

        @RequiresApi(Build.VERSION_CODES.R)
        override fun onDisplayChanged(displayId: Int) = this@CameraActivity.display?.let { view ->
            if (displayId == this@CameraActivity.displayId) {
                imageCapture?.targetRotation = view.rotation
            }
        } ?: Unit
    }

}