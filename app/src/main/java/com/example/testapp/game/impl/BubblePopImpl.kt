package com.example.testapp.game.impl

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import com.example.testapp.services.PoseDetectorService
import kotlin.random.Random

class BubblePopImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Bong Bóng Cầu Vồng"
    
    private val bubbles = mutableStateListOf<Bubble>()
    private var lastSpawnTime = 0L

    data class Bubble(
        var x: Float, var y: Float, val radius: Float, val color: Color, 
        val isCoop: Boolean, var alpha: Float = 0.7f, val speed: Float
    )

    override fun init(width: Int, height: Int, initialPlayers: List<Player>) {
        players.clear()
        players.addAll(initialPlayers)
        players.forEach { it.score = 0 }
        bubbles.clear()
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime > 1500) {
            val isCoop = Random.nextFloat() < 0.3f
            bubbles.add(Bubble(
                x = (0.1f + Random.nextFloat() * 0.8f) * width,
                y = height.toFloat() + 50f,
                radius = if (isCoop) 100f else 60f,
                color = if (isCoop) Color.Cyan else listOf(Color.Magenta, Color.Yellow, Color.Green).random(),
                isCoop = isCoop,
                speed = 2f + Random.nextFloat() * 4f
            ))
            lastSpawnTime = currentTime
        }

        bubbles.removeAll { b ->
            b.y -= b.speed
            b.y < -100f
        }
        checkCollisions()
    }

    private fun checkCollisions() {
        val iterator = bubbles.iterator()
        while (iterator.hasNext()) {
            val b = iterator.next()
            if (b.isCoop) {
                // Bong bóng Co-op cần cả 2 người chạm vào
                val p1Hit = players.getOrNull(0)?.let { p -> 
                    listOfNotNull(p.leftWrist, p.rightWrist).any { dist(it.x, it.y, b.x, b.y) < b.radius + 30f }
                } ?: false
                val p2Hit = players.getOrNull(1)?.let { p -> 
                    listOfNotNull(p.leftWrist, p.rightWrist).any { dist(it.x, it.y, b.x, b.y) < b.radius + 30f }
                } ?: false
                
                if (p1Hit && p2Hit) {
                    players.forEach { it.score += 50 }
                    iterator.remove()
                }
            } else {
                // Bong bóng thường, ai chạm thì điểm cho người đó
                players.forEach { player ->
                    val hit = listOfNotNull(player.leftWrist, player.rightWrist).any { 
                        dist(it.x, it.y, b.x, b.y) < b.radius + 30f 
                    }
                    if (hit) {
                        player.score += 10
                        iterator.remove()
                        return@forEach
                    }
                }
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            bubbles.forEach { b ->
                if (b.isCoop) {
                    drawCircle(b.color, b.radius, Offset(b.x, b.y), style = Stroke(width = 8f))
                    drawCircle(b.color.copy(0.3f), b.radius - 10f, Offset(b.x, b.y))
                    val layout = textMeasurer.measure("CÙNG NHAU!", TextStyle(fontSize = 16.sp, color = Color.White))
                    drawText(textLayoutResult = layout, topLeft = Offset(b.x - layout.size.width/2, b.y - layout.size.height/2))
                } else {
                    drawCircle(b.color.copy(b.alpha), b.radius, Offset(b.x, b.y))
                    drawCircle(Color.White.copy(0.4f), b.radius * 0.3f, Offset(b.x - b.radius * 0.3f, b.y - b.radius * 0.3f))
                }
            }
            // Vẽ tay
            players.forEach { p ->
                p.leftWrist?.let { drawCircle(p.color, 20f, Offset(it.x, it.y)) }
                p.rightWrist?.let { drawCircle(p.color, 20f, Offset(it.x, it.y)) }
            }
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = kotlin.math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2))
    override fun release() {}
}
