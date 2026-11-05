package com.amazego.autosolver.solver

import com.amazego.autosolver.cv.ArrowDetector.DetectedArrow
import com.amazego.autosolver.cv.ArrowDetector.ArrowDirection

/**
 * Constraint-based Puzzle Solving Engine
 * Builds dependency graph using trajectory raycasts and calculates valid sequence.
 */
class PuzzleSolver {

    data class SolutionResult(
        val sequence: List<DetectedArrow>,
        val freeArrows: List<DetectedArrow>,
        val dependencies: Map<String, List<String>>,
        val isSolvable: Boolean,
        val cycleDetected: Boolean
    )

    fun solve(arrows: List<DetectedArrow>): SolutionResult {
        val depGraph = mutableMapOf<String, MutableList<String>>()
        val inDegree = mutableMapOf<String, Int>()

        for (arrow in arrows) {
            depGraph[arrow.id] = mutableListOf()
            inDegree[arrow.id] = 0
        }

        // Raycast from each arrow along exit trajectory to detect blockers
        for (a in arrows) {
            for (b in arrows) {
                if (a.id == b.id) continue
                if (isBlockedBy(a, b)) {
                    depGraph[a.id]?.add(b.id)
                    inDegree[a.id] = (inDegree[a.id] ?: 0) + 1
                }
            }
        }

        val freeArrows = arrows.filter { inDegree[it.id] == 0 }

        // Topological Sort (Kahn's Algorithm with heuristic priority)
        val sequence = mutableListOf<DetectedArrow>()
        val remaining = arrows.toMutableList()
        val curInDegree = inDegree.toMutableMap()

        while (remaining.isNotEmpty()) {
            val unblocked = remaining.filter { curInDegree[it.id] == 0 }
            if (unblocked.isEmpty()) {
                // Cycle / Deadlock detected! Never guess or tap randomly.
                return SolutionResult(
                    sequence = sequence,
                    freeArrows = freeArrows,
                    dependencies = depGraph,
                    isSolvable = false,
                    cycleDetected = true
                )
            }

            // Pick arrow that frees the most downstream arrows
            val chosen = unblocked.maxByOrNull { arrow ->
                remaining.count { rem -> depGraph[rem.id]?.contains(arrow.id) == true }
            } ?: unblocked.first()

            sequence.add(chosen)
            remaining.remove(chosen)

            for (rem in remaining) {
                if (depGraph[rem.id]?.contains(chosen.id) == true) {
                    curInDegree[rem.id] = maxOf(0, (curInDegree[rem.id] ?: 1) - 1)
                }
            }
        }

        return SolutionResult(
            sequence = sequence,
            freeArrows = freeArrows,
            dependencies = depGraph,
            isSolvable = true,
            cycleDetected = false
        )
    }

    private fun isBlockedBy(a: DetectedArrow, b: DetectedArrow): Boolean {
        val dx = when (a.direction) {
            ArrowDirection.RIGHT -> 1.0
            ArrowDirection.LEFT -> -1.0
            else -> 0.0
        }
        val dy = when (a.direction) {
            ArrowDirection.DOWN -> 1.0
            ArrowDirection.UP -> -1.0
            else -> 0.0
        }

        val vx = b.centerX - a.centerX
        val vy = b.centerY - a.centerY

        // Check if B is in front of A along direction vector
        val dot = vx * dx + vy * dy
        if (dot <= 0) return false // B is behind or perpendicular

        // Perpendicular cross distance
        val cross = kotlin.math.abs(vx * dy - vy * dx)
        val tolerance = maxOf(b.boundingBox.width, b.boundingBox.height) * 0.6

        return cross < tolerance
    }
}
