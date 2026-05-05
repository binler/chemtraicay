package com.example.testapp.game.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import com.example.testapp.services.PoseDetectorService
import kotlin.random.Random

class PunchPointsImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Punch Points Multiplayer"
    
    private val targets = mutableStateListOf<TargetPoint>()
    private var lastSpawnTime = 0L

    data class TargetPoint(val x: Float, val y: Float, val radius: Float, val color: Color, val id: Long)

    override fun init(width: Int, height: Int, initialPlayers: List<Player>) {
        players.clear()
        players.addAll(initialPlayers)
        targets.clear()
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime > 2000 || targets.isEmpty()) {
            spawnTarget(width, height)
            lastSpawnTime = currentTime
        }
        checkHits()
    }

    private fun spawnTarget(width: Int, height: Int) {
        if (targets.size < 5) {
            targets.add(TargetPoint(
                x = (0.2f + Random.nextFloat() * 0.6f) * width,
                y = (0.2f + Random.nextFloat() * 0.6f) * height,
                radius = 80f,
                color = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow).random(),
                id = System.currentTimeMillis()
            ))
        }
    }

    private fun checkHits() {
        players.forEach { player ->
            val wrists = listOfNotNull(player.leftWrist, player.rightWrist)
            val iterator = targets.iterator()
            while (iterator.hasNext()) {
                val target = iterator.next()
                val hit = wrists.any { wrist ->
                    val dx = wrist.x - target.x
                    val dy = wrist.y - target.y
                    kotlin.math.sqrt(dx * dx + dy * dy) < target.radius + 30f
                }
                if (hit) {
                    player.score += 20
                    iterator.remove()
                }
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {
        updatedPlayers.forEach { updated ->
            players.find { it.id == updated.id }?.let { 
                it.leftWrist = updated.leftWrist
                it.rightWrist = updated.rightWrist
            }
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            targets.forEach { target ->
                drawCircle(target.color.copy(alpha = 0.5f), target.radius + 10f, Offset(target.x, target.y))
                drawCircle(target.color, target.radius, Offset(target.x, target.y))
                
                val layout = textMeasurer.measure("PUNCH!", TextStyle(fontSize = 20.sp, color = Color.White))
                drawText(textLayoutResult = layout, topLeft = Offset(target.x - layout.size.width/2, target.y - layout.size.height/2))
            }

            // Vẽ tay của tất cả người chơi
            players.forEach { player ->
                player.leftWrist?.let { drawCircle(player.color, 25f, Offset(it.x, it.y)) }
                player.rightWrist?.let { drawCircle(player.color, 25f, Offset(it.x, it.y)) }
            }
        }
    }

    override fun release() {}
}
