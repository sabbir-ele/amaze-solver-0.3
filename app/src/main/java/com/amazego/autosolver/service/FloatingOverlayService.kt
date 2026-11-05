package com.amazego.autosolver.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import com.amazego.autosolver.R
import com.amazego.autosolver.capture.ScreenCaptureManager
import com.amazego.autosolver.controller.AutoTapController
import com.amazego.autosolver.cv.ArrowDetector
import com.amazego.autosolver.solver.PuzzleSolver
import com.amazego.autosolver.ui.DebugVisualizer
import kotlinx.coroutines.*

/**
 * Floating Overlay Service (TYPE_APPLICATION_OVERLAY)
 * Provides a draggable, collapsible floating control panel above Amaze GO!
 */
class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var visualizerOverlay: DebugVisualizer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var screenCaptureManager: ScreenCaptureManager? = null
    private var arrowDetector = ArrowDetector()
    private var puzzleSolver = PuzzleSolver()
    private var autoTapController: AutoTapController? = null

    companion object {
        var projectionResultCode: Int = 0
        var projectionData: Intent? = null

        fun setMediaProjectionIntent(resultCode: Int, data: Intent) {
            projectionResultCode = resultCode
            projectionData = data
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForegroundServiceNotification()
        initScreenCapture()
        createFloatingOverlay()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "amazego_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Amaze GO Floating Assistant",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Amaze GO Auto Solver Active")
            .setContentText("Floating controls are visible above the screen.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1001, notification)
    }

    private fun initScreenCapture() {
        if (projectionData != null) {
            screenCaptureManager = ScreenCaptureManager(this, projectionResultCode, projectionData!!)
            autoTapController = AutoTapController(
                context = this,
                screenCaptureManager = screenCaptureManager!!,
                arrowDetector = arrowDetector,
                puzzleSolver = puzzleSolver
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        overlayView = LayoutInflater.from(this).inflate(R.layout.layout_floating_panel, null)

        // Setup Dragging
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        val dragHandle = overlayView!!.findViewById<View>(R.id.drag_handle)
        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                else -> false
            }
        }

        // Button Listeners
        val btnScan = overlayView!!.findViewById<Button>(R.id.btn_scan)
        val btnManual = overlayView!!.findViewById<Button>(R.id.btn_manual)
        val btnAuto = overlayView!!.findViewById<Button>(R.id.btn_auto)
        val btnStop = overlayView!!.findViewById<Button>(R.id.btn_stop)
        val statusText = overlayView!!.findViewById<TextView>(R.id.txt_status)

        btnScan.setOnClickListener {
            serviceScope.launch {
                statusText.text = "Scanning..."
                val bitmap = screenCaptureManager?.captureScreen()
                if (bitmap != null) {
                    val detection = arrowDetector.detectArrows(bitmap)
                    val solution = puzzleSolver.solve(detection.arrows)
                    statusText.text = "Detected: ${detection.arrows.size} | Unblocked: ${solution.freeArrows.size}"
                } else {
                    statusText.text = "Capture Failed"
                }
            }
        }

        btnAuto.setOnClickListener {
            statusText.text = "Auto Solving..."
            autoTapController?.startAutoSolve { status ->
                statusText.text = status
            }
        }

        btnStop.setOnClickListener {
            autoTapController?.emergencyStop()
            statusText.text = "Stopped"
        }

        windowManager.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        screenCaptureManager?.release()
        overlayView?.let { windowManager.removeView(it) }
    }
}
