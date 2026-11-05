package com.amazego.autosolver.capture

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager

/**
 * Screen Capture Manager using MediaProjection & ImageReader
 */
class ScreenCaptureManager(
    context: Context,
    resultCode: Int,
    data: Intent
) {
    private val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private var mediaProjection: MediaProjection? = projectionManager.getMediaProjection(resultCode, data)
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private val width = 1080
    private val height = 2400
    private val density = 420

    init {
        setupVirtualDisplay()
    }

    @SuppressLint("WrongConstant")
    private fun setupVirtualDisplay() {
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "AmazeGoCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
    }

    fun captureScreen(): Bitmap? {
        val image = imageReader?.acquireLatestImage() ?: return null
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        // Crop padding
        return Bitmap.createBitmap(bitmap, 0, 0, width, height)
    }

    fun release() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
    }
}
