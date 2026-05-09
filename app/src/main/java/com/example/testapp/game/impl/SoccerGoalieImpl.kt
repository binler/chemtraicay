package com.example.testapp.game.impl

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import kotlin.random.Random

import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.AgeGroup

class SoccerGoalieImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Thủ Môn Nhí"
    override val category = CategoryType.EXERCISE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "⚽"
    override val isAR = true
    
    private val balls = mutableStateListOf<Ball>()
    private var lastSpawnTime = 0L

    data class Ball(var x: Float, var y: Float, var vx: Float, var vy: Float, val radius: Float = 50f)

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        players.clear()
        players.addAll(initialPlayers)
        players.forEach { it.score = 0 }
        balls.clear()
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime > 1200) {
            balls.add(Ball(
                x = width / 2f,
                y = height / 2f,
                vx = (Random.nextFloat() - 0.5f) * 20f,
                vy = (Random.nextFloat() - 0.5f) * 20f
            ))
            lastSpawnTime = currentTime
        }

        balls.removeAll { ball ->
            ball.x += ball.vx; ball.y += ball.vy
            ball.x < 0 || ball.x > width || ball.y < 0 || ball.y > height
        }
        
        checkSaves()
    }

    private fun checkSaves() {
        val iterator = balls.iterator()
        while (iterator.hasNext()) {
            val ball = iterator.next()
            players.forEach { player ->
                val bodyParts = listOfNotNull(player.leftWrist, player.rightWrist, player.leftShoulder, player.rightShoulder)
                val hit = bodyParts.any { part ->
                    dist(part.x, part.y, ball.x, ball.y) < ball.radius + 40f
                }
                if (hit) {
                    player.score += 10
                    iterator.remove()
                    return@forEach
                }
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun onTouch(x: Float, y: Float) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            // Vẽ bóng đá bằng emoji
            balls.forEach { ball ->
                val layout = textMeasurer.measure("⚽", TextStyle(fontSize = 50.sp))
                drawText(textLayoutResult = layout, topLeft = Offset(ball.x - layout.size.width/2, ball.y - layout.size.height/2))
            }

            // Vẽ "găng tay" thủ môn cho mỗi người chơi
            players.forEach { player ->
                player.leftWrist?.let { drawCircle(player.color, 30f, Offset(it.x, it.y)) }
                player.rightWrist?.let { drawCircle(player.color, 30f, Offset(it.x, it.y)) }
            }
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = kotlin.math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2))
    override fun release() {}
}
