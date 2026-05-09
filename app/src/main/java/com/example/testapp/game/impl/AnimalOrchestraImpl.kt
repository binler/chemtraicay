package com.example.testapp.game.impl

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.AgeGroup
import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import kotlin.math.sqrt

class AnimalOrchestraImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Dàn Nhạc Rừng Xanh"
    override val category = CategoryType.LEARNING
    override val targetAge = AgeGroup.TODDLER
    override val icon = "🐘"
    override val isAR = true

    private data class Animal(val emoji: String, val name: String, val x: Float, val y: Float, var isPlaying: Boolean = false)
    private val animals = mutableStateListOf<Animal>()
    private var onSpeech: (String) -> Unit = {}
    private var lastInteractionTime = 0L

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        players.addAll(initialPlayers)
        animals.clear()
        val centerY = height * 0.5f
        animals.add(Animal("🐘", "Bạn Voi", width * 0.2f, centerY))
        animals.add(Animal("🦁", "Bạn Sư Tử", width * 0.4f, centerY))
        animals.add(Animal("🐵", "Bạn Khỉ", width * 0.6f, centerY))
        animals.add(Animal("🐥", "Bạn Gà Con", width * 0.8f, centerY))
        onSpeech("Chào mừng con đến với dàn nhạc rừng xanh!")
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInteractionTime < 1500) return

        players.forEach { player ->
            val wrists = listOfNotNull(player.leftWrist, player.rightWrist)
            wrists.forEach { wrist ->
                animals.forEach { animal ->
                    if (dist(wrist.x, wrist.y, animal.x, animal.y) < 100f) {
                        onSpeech(animal.name + " xin chào con!")
                        lastInteractionTime = currentTime
                    }
                }
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            drawRect(Color(0xFFE8F5E9)) // Nền xanh lá nhạt
            animals.forEach { animal ->
                val layout = textMeasurer.measure(animal.emoji, TextStyle(fontSize = 100.sp))
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(animal.x - layout.size.width / 2f, animal.y - layout.size.height / 2f)
                )
            }
            players.forEach { p ->
                p.leftWrist?.let { drawCircle(p.color, 20f, Offset(it.x, it.y)) }
                p.rightWrist?.let { drawCircle(p.color, 20f, Offset(it.x, it.y)) }
            }
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt(((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)).toDouble()).toFloat()
    override fun onTouch(x: Float, y: Float) {}
    override fun release() {}
}
