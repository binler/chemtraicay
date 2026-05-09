package com.example.testapp.game.impl

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.AgeGroup
import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import kotlin.random.Random

class EarthDiscoveryImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Bí Mật Đại Dương"
    override val category = CategoryType.SCIENCE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🌊"
    override val isAR = false // PURE TOUCH

    private var cloudAlpha by mutableStateOf(1f)
    private var isOceanRevealed by mutableStateOf(false)
    private val fishList = mutableStateListOf<Fish>()
    private var message by mutableStateOf("Dùng tay xua tan mây để thấy đại dương nhé!")
    private var onSpeech: (String) -> Unit = {}

    data class Fish(var x: Float, var y: Float, val color: Color, val speed: Float, val size: Float, val emoji: String)

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        cloudAlpha = 1f
        isOceanRevealed = false
        fishList.clear()
        message = "Dùng tay xua tan mây để thấy đại dương nhé!"
        
        repeat(5) {
            fishList.add(Fish(
                Random.nextFloat() * width,
                height * 0.6f + Random.nextFloat() * height * 0.3f,
                listOf(Color.Yellow, Color.Red, Color.Magenta, Color.Cyan).random(),
                2f + Random.nextFloat() * 4f,
                40f + Random.nextFloat() * 40f,
                listOf("🐠", "🐟", "🐡", "🐙").random()
            ))
        }
        onSpeech(message)
    }

    override fun update(width: Int, height: Int) {
        if (isOceanRevealed) {
            fishList.forEach { fish ->
                fish.x += fish.speed
                if (fish.x > width + 100) fish.x = -100f
            }
        }
    }

    override fun onTouch(x: Float, y: Float) {
        if (!isOceanRevealed) {
            cloudAlpha = (cloudAlpha - 0.15f).coerceAtLeast(0f)
            if (cloudAlpha <= 0f) {
                isOceanRevealed = true
                message = "Oa! Đại dương hiện ra rồi. Chạm vào các bạn cá nào!"
                onSpeech(message)
            }
        } else {
            fishList.forEach { fish ->
                if (dist(x, y, fish.x, fish.y) < 100f) {
                    onSpeech("Bạn " + fish.emoji + " chào con!")
                }
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0277BD), Color(0xFF01579B))
                )
            )

            if (isOceanRevealed) {
                fishList.forEach { fish ->
                    val layout = textMeasurer.measure(fish.emoji, TextStyle(fontSize = fish.size.sp))
                    drawText(textLayoutResult = layout, topLeft = Offset(fish.x - layout.size.width/2, fish.y - layout.size.height/2))
                }
            }

            if (cloudAlpha > 0) {
                drawRect(
                    color = Color.White.copy(alpha = cloudAlpha),
                    size = size
                )
                val cloudIcon = textMeasurer.measure("☁️☁️☁️\n☁️☁️☁️", TextStyle(fontSize = 100.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center))
                drawText(textLayoutResult = cloudIcon, topLeft = Offset(size.width/2 - cloudIcon.size.width/2, size.height/2 - cloudIcon.size.height/2), alpha = cloudAlpha)
            }

            val msgLayout = textMeasurer.measure(
                text = message,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (isOceanRevealed) Color.White else Color.DarkGray)
            )
            drawText(textLayoutResult = msgLayout, topLeft = Offset(size.width/2 - msgLayout.size.width/2, size.height - 150f))
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = kotlin.math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)).toFloat()
    override fun release() {}
}
