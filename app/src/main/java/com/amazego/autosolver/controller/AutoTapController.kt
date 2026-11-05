package com.amazego.autosolver.controller

import android.content.Context
import com.amazego.autosolver.capture.ScreenCaptureManager
import com.amazego.autosolver.cv.ArrowDetector
import com.amazego.autosolver.service.AutoTapAccessibilityService
import com.amazego.autosolver.solver.PuzzleSolver
import com.amazego.autosolver.verifier.GameStateVerifier
import kotlinx.coroutines.*

/**
 * Auto Tap Controller & Execution State Machine
 * Executes: Capture -> Solve -> Tap -> Animation Delay -> Screenshot Verification -> Next Move.
 */
class AutoTapController(
    private val context: Context,
    private val screenCaptureManager: ScreenCaptureManager,
    private val arrowDetector: ArrowDetector,
    private val puzzleSolver: PuzzleSolver
) {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val verifier = GameStateVerifier()
    private var isEmergencyStopped = false

    var tapIntervalMs: Long = 1200L // Default Slow for safety

    fun startAutoSolve(onProgress: (String) -> Unit) {
        isEmergencyStopped = false
        job?.cancel()

        job = scope.launch {
            while (isActive && !isEmergencyStopped) {
                onProgress("Capturing screen...")
                val preBitmap = screenCaptureManager.captureScreen()
                if (preBitmap == null) {
                    onProgress("Error: Screen capture returned null")
                    break
                }

                onProgress("Detecting puzzle objects...")
                val detection = arrowDetector.detectArrows(preBitmap)
                if (detection.arrows.isEmpty()) {
                    onProgress("Puzzle Complete! All arrows cleared.")
                    break
                }

                val solution = puzzleSolver.solve(detection.arrows)
                if (!solution.isSolvable) {
                    onProgress("Safety Halt: Cyclic deadlock or ambiguous board!")
                    break
                }

                val targetArrow = solution.sequence.firstOrNull() ?: break
                onProgress("Tapping Arrow at (${targetArrow.centerX.toInt()}, ${targetArrow.centerY.toInt()})...")

                val tapped = AutoTapAccessibilityService.instance?.performTap(
                    targetArrow.centerX.toFloat(),
                    targetArrow.centerY.toFloat()
                ) ?: false

                if (!tapped) {
                    onProgress("Error: Accessibility tap dispatch failed")
                    break
                }

                // Wait for game exit animation
                delay(tapIntervalMs)

                // State Verification: Confirm arrow has disappeared
                val postBitmap = screenCaptureManager.captureScreen()
                if (postBitmap != null) {
                    val verified = verifier.verifyArrowDisappeared(preBitmap, postBitmap, targetArrow.boundingBox)
                    if (!verified) {
                        onProgress("Verification Failed: Arrow did not disappear. Halting for safety.")
                        break
                    }
                }
            }
        }
    }

    fun emergencyStop() {
        isEmergencyStopped = true
        job?.cancel()
    }
}
