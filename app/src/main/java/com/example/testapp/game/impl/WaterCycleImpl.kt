package com.example.testapp.game.impl

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.*
import com.example.testapp.ui.theme.*
import kotlin.math.sin
import kotlin.random.Random

class WaterCycleImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Phù Thủy Thời Tiết"
    override val category = CategoryType.SCIENCE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🌦️"
    override val isAR = false

    private var cloudSize by mutableStateOf(0f)
    private var isRaining by mutableStateOf(false)
    private val rainDrops = mutableStateListOf<Offset>()
    private var message by mutableStateOf("Vuốt từ biển lên trời để tạo mây nhé!")
    private var onSpeech: (String) -> Unit = {}
    
    override fun init(context: android.content.Context, width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        cloudSize = 0f
        isRaining = false
        rainDrops.clear()
        onSpeech(message)
    }

    override fun update(width: Int, height: Int) {
        if (isRaining) {
            repeat(3) { rainDrops.add(Offset(Random.nextFloat() * width, 150f)) }
            rainDrops.removeAll { it.y > height }
        }
    }

    override fun onTouch(x: Float, y: Float) {
        if (y > 600f) {
            cloudSize = (cloudSize + 10f).coerceAtMost(500f)
            if (cloudSize > 400f && !isRaining) {
                message = "Mây đã nặng rồi! Chạm vào mây để làm mưa nào!"
                onSpeech(message)
            }
        } else if (y < 450f && cloudSize > 400f && !isRaining) {
            isRaining = true
            onSpeech("Rào rào! Mưa rơi rồi!")
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        val size = drawScope.size

        // Trời & Biển
        drawScope.drawRect(Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFFE1F5FE))))
        drawScope.drawRect(Color(0xFF0288D1), topLeft = Offset(0f, size.height * 0.85f))

        // Mây
        if (cloudSize > 50f) {
            val color = if (isRaining) Color.Gray else Color.White
            drawScope.drawCircle(color, cloudSize/2, Offset(size.width/2, 200f))
        }

        // Mưa
        rainDrops.forEach { drop ->
            drawScope.drawLine(Color.White, drop, Offset(drop.x, drop.y + 30f), strokeWidth = 2f)
        }

        // Chữ
        val layout = textMeasurer.measure(message, TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark))
        drawScope.drawText(textLayoutResult = layout, topLeft = Offset(size.width/2 - layout.size.width/2, size.height - 200f))
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}
    override fun release() {}
}
