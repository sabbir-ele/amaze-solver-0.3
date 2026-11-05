package com.amazego.autosolver.verifier

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/**
 * Screenshot-based Verification Engine
 * Compares pre-tap and post-tap frames to confirm arrow removal.
 */
class GameStateVerifier {

    fun verifyArrowDisappeared(pre: Bitmap, post: Bitmap, targetBox: Rect): Boolean {
        val matPre = Mat()
        val matPost = Mat()
        Utils.bitmapToMat(pre, matPre)
        Utils.bitmapToMat(post, matPost)

        // Submat in target bounding box
        val roiPre = Mat(matPre, targetBox)
        val roiPost = Mat(matPost, targetBox)

        val diff = Mat()
        Core.absdiff(roiPre, roiPost, diff)

        val grayDiff = Mat()
        Imgproc.cvtColor(diff, grayDiff, Imgproc.COLOR_RGBA2GRAY)

        val nonZeroCount = Core.countNonZero(grayDiff)
        val totalPixels = targetBox.area()

        // Significant pixel change in bounding box indicates arrow flight
        val changeRatio = nonZeroCount.toDouble() / (totalPixels + 1e-5)
        return changeRatio > 0.18
    }
}
