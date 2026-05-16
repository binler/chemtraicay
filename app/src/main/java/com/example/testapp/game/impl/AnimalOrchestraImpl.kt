package com.example.testapp.game.impl

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.*
import com.example.testapp.ui.theme.*
import kotlin.math.sqrt

class AnimalOrchestraImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Dàn Nhạc Rừng Xanh"
    override val category = CategoryType.LEARNING
    override val targetAge = AgeGroup.TODDLER
    override val icon = "🐘"
    override val isAR = false // CẢM ỨNG CHO BÉ 2 TUỔI

    private data class Animal(val emoji: String, val name: String, val x: Float, val y: Float, val color: Color)
    private val animals = mutableStateListOf<Animal>()
    private var onSpeech: (String) -> Unit = {}
    private var lastInteractionTime = 0L

    override fun init(context: android.content.Context, width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        animals.clear()
        
        val centerY = height * 0.5f
        animals.add(Animal("🐘", "Bạn Voi", width * 0.2f, centerY, SkyBlue))
        animals.add(Animal("🦁", "Bạn Sư Tử", width * 0.4f, centerY, SunnyYellow))
        animals.add(Animal("🐵", "Bạn Khỉ", width * 0.6f, centerY, SoftPink))
        animals.add(Animal("🐥", "Bạn Gà Con", width * 0.8f, centerY, MintGreen))
        
        onSpeech("Chào mừng con đến với dàn nhạc rừng xanh! Hãy chạm vào các bạn thú nhé.")
    }

    override fun update(width: Int, height: Int) {}
    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun onTouch(x: Float, y: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInteractionTime < 1000) return

        animals.forEach { animal ->
            val dx = x - animal.x
            val dy = y - animal.y
            if (sqrt((dx * dx + dy * dy).toDouble()) < 120f) {
                onSpeech(animal.name + " kêu như thế nào nhỉ?")
                lastInteractionTime = currentTime
            }
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            // Nền cỏ xanh mượt
            drawRect(Color(0xFFF1F8E9))
            
            animals.forEach { animal ->
                // Vẽ vòng tròn nền cho con vật
                drawCircle(
                    color = animal.color.copy(alpha = 0.3f),
                    radius = 100f,
                    center = Offset(animal.x, animal.y)
                )
                
                val layout = textMeasurer.measure(animal.emoji, TextStyle(fontSize = 100.sp))
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(animal.x - layout.size.width / 2f, animal.y - layout.size.height / 2f)
                )
            }
        }
    }

    override fun release() {}
}
