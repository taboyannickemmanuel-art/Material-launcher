package com.example.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object ExpressiveIconShapes {

    val Circle = CircleShape

    val Squircle = RoundedCornerShape(38)

    val Pill = RoundedCornerShape(50)

    val Teardrop = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 28.dp,
        bottomEnd = 6.dp
    )

    val Hexagon = GenericShape { size, _ ->
        val width = size.width
        val height = size.height
        val radius = minOf(width, height) / 2f
        val centerX = width / 2f
        val centerY = height / 2f

        val path = Path()
        for (i in 0 until 6) {
            val angle = i * (PI / 3) - PI / 2
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        addPath(path)
    }

    // Material 3 Expressive 8-lobed Star / Flower Shape
    val ExpressiveStar = GenericShape { size, _ ->
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val outerRadius = minOf(width, height) / 2f
        val innerRadius = outerRadius * 0.78f

        val path = Path()
        val numPoints = 8
        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * (PI / numPoints) - PI / 2
            val x = centerX + r * cos(angle).toFloat()
            val y = centerY + r * sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        addPath(path)
    }

    fun getShape(shapeName: String): Shape {
        return when (shapeName) {
            "Cercle", "Circle" -> Circle
            "Squircle" -> Squircle
            "Étoile Expressive", "Expressive Star" -> ExpressiveStar
            "Pilule", "Pill" -> Pill
            "Goutte", "Teardrop" -> Teardrop
            "Hexagone", "Hexagon" -> Hexagon
            "System", "Système" -> Squircle
            else -> Squircle
        }
    }

    val ALL_SHAPES = listOf(
        "Squircle",
        "Circle",
        "Expressive Star",
        "Pill",
        "Teardrop",
        "Hexagon"
    )
}
