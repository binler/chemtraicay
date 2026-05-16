package com.example.testapp.game.core

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

object OrbitCalculator {
    /**
     * Calculates the (x, y) position on an elliptical orbit.
     * x = cx + a * cos(theta)
     * y = cy + b * sin(theta)
     */
    fun getPosition(
        cx: Float,
        cy: Float,
        a: Float,
        b: Float,
        theta: Float
    ): Offset {
        val x = cx + a * cos(theta)
        val y = cy + b * sin(theta)
        return Offset(x, y)
    }
}
