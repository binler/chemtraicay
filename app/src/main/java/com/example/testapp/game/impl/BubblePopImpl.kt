package com.example.testapp.game.impl

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.*
import kotlin.random.Random

class BubblePopImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Bong Bóng Màu Sắc"
    override val category = CategoryType.LEARNING
    override val targetAge = AgeGroup.TODDLER
    override val icon = "🫧"
    override val isAR = false

    private val bubbles = mutableStateListOf<Bubble>()
    private var lastSpawnTime = 0L

    data class Bubble(var x: Float, var y: Float, val radius: Float, val color: Color, val speed: Float)

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        players.clear()
        players.addAll(initialPlayers)
        bubbles.clear()
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime > 1500) {
            bubbles.add(Bubble((0.1f + Random.nextFloat() * 0.8f) * width, height.toFloat() + 50f, 60f, listOf(Color.Cyan, Color.Magenta, Color.Yellow).random(), 3f + Random.nextFloat() * 4f))
            lastSpawnTime = currentTime
        }
        bubbles.forEach { it.y -= it.speed }
        bubbles.removeAll { it.y < -100f }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun onTouch(x: Float, y: Float) {
        val iterator = bubbles.iterator()
        while (iterator.hasNext()) {
            val b = iterator.next()
            if (kotlin.math.sqrt(((x - b.x) * (x - b.x) + (y - b.y) * (y - b.y)).toDouble()) < b.radius + 30f) {
                players.getOrNull(0)?.score = (players.getOrNull(0)?.score ?: 0) + 10
                iterator.remove()
                return
            }
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            drawRect(Color(0xFFE3F2FD)) // Pastel Blue background
            bubbles.forEach { b ->
                drawCircle(b.color.copy(alpha = 0.6f), b.radius, Offset(b.x, b.y))
                drawCircle(Color.White.copy(alpha = 0.4f), b.radius * 0.3f, Offset(b.x - b.radius * 0.3f, b.y - b.radius * 0.3f))
            }
        }
    }

    override fun release() {}
}
