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
import com.example.testapp.services.PoseDetectorService
import kotlin.random.Random

import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.AgeGroup

class CatchStarsImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Hái Sao Cho Bé"
    override val category = CategoryType.EXERCISE
    override val targetAge = AgeGroup.TODDLER
    override val icon = "⭐"
    override val isAR = true
    
    private val stars = mutableStateListOf<Star>()
    private var lastSpawnTime = 0L

    data class Star(var x: Float, var y: Float, val emoji: String, val id: Long, val type: StarType)
    enum class StarType { HIGH, LOW, SPECIAL }

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        players.clear()
        players.addAll(initialPlayers)
        players.forEach { it.score = 0 }
        stars.clear()
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime > 2000) {
            val type = when(Random.nextFloat()) {
                in 0f..0.4f -> StarType.LOW
                in 0.4f..0.8f -> StarType.HIGH
                else -> StarType.SPECIAL
            }
            stars.add(Star(
                x = (0.2f + Random.nextFloat() * 0.6f) * width,
                y = when(type) {
                    StarType.LOW -> (0.6f + Random.nextFloat() * 0.3f) * height
                    StarType.HIGH -> (0.1f + Random.nextFloat() * 0.3f) * height
                    StarType.SPECIAL -> (0.4f + Random.nextFloat() * 0.2f) * height
                },
                emoji = if (type == StarType.SPECIAL) "💎" else "⭐",
                id = System.currentTimeMillis(),
                type = type
            ))
            lastSpawnTime = currentTime
        }
        checkHits()
    }

    private fun checkHits() {
        val iterator = stars.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            players.forEach { player ->
                val hit = listOfNotNull(player.leftWrist, player.rightWrist).any { 
                    dist(it.x, it.y, s.x, s.y) < 80f
                }
                if (hit) {
                    player.score += if (s.type == StarType.SPECIAL) 50 else 20
                    iterator.remove()
                    return@forEach
                }
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            stars.forEach { s ->
                val layout = textMeasurer.measure(s.emoji, TextStyle(fontSize = 60.sp))
                drawText(textLayoutResult = layout, topLeft = Offset(s.x - layout.size.width/2, s.y - layout.size.height/2))
            }
            players.forEach { p ->
                p.leftWrist?.let { drawCircle(p.color, 20f, Offset(it.x, it.y)) }
                p.rightWrist?.let { drawCircle(p.color, 20f, Offset(it.x, it.y)) }
            }
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = kotlin.math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2))
    override fun onTouch(x: Float, y: Float) {}
    override fun release() {}
}
