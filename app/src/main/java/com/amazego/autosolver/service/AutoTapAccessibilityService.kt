package com.amazego.autosolver.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

/**
 * Accessibility Service for automated tap dispatching
 */
class AutoTapAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoTapAccessibilityService? = null
        var isServiceRunning = false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        instance = null
        isServiceRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isServiceRunning = false
    }

    fun performTap(x: Float, y: Float, duration: Long = 80L): Boolean {
        val clickPath = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(clickPath, 0, duration)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }
}
