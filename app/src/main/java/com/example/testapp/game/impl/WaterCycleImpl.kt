package com.example.testapp.game.impl

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

class WaterCycleImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Phù Thủy Thời Tiết"
    override val category = CategoryType.SCIENCE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🌦️"
    override val isAR = false

    // State cho hiệu ứng
    private var vaporParticles = mutableStateListOf<Vapor>()
    private var rainParticles = mutableStateListOf<RainDrop>()
    private var cloudSize by mutableStateOf(0f)
    private var cloudColor by mutableStateOf(Color.White)
    private var message by mutableStateOf("Vuốt từ biển lên trời để tạo mây nào!")
    private var onSpeech: (String) -> Unit = {}

    data class Vapor(var x: Float, var y: Float, val speed: Float, var alpha: Float = 1f)
    data class RainDrop(var x: Float, var y: Float, val speed: Float)

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        vaporParticles.clear()
        rainParticles.clear()
        cloudSize = 0f
        cloudColor = Color.White
        onSpeech(message)
    }

    override fun update(width: Int, height: Int) {
        // Cập nhật hơi nước bay lên
        val vIterator = vaporParticles.iterator()
        while (vIterator.hasNext()) {
            val v = vIterator.next()
            v.y -= v.speed
            v.alpha -= 0.01f
            if (v.alpha <= 0) vIterator.remove()
        }

        // Cập nhật mưa rơi
        val rIterator = rainParticles.iterator()
        while (rIterator.hasNext()) {
            val r = rIterator.next()
            r.y += r.speed
            if (r.y > height) rIterator.remove()
        }

        // Đám mây đổi màu khi nặng
        if (cloudSize > 300f) {
            cloudColor = Color.Gray
            if (message != "Mây nặng rồi! Chạm vào mây để làm mưa nhé.") {
                message = "Mây nặng rồi! Chạm vào mây để làm mưa nhé."
                onSpeech(message)
            }
        }
    }

    override fun onTouch(x: Float, y: Float) {
        // Nếu chạm vùng trời (trên cao) và mây đã to -> Tạo mưa
        if (y < 400f && cloudSize > 300f) {
            repeat(20) {
                rainParticles.add(RainDrop(x + Random.nextFloat() * 400f - 200f, y, 10f + Random.nextFloat() * 10f))
            }
            message = "Rào rào! Mưa rơi xuống cứu cây cối rồi!"
            onSpeech("Mưa rơi rồi! Giỏi quá!")
        } 
        // Nếu vuốt/chạm vùng biển (dưới thấp) -> Tạo hơi nước
        else if (y > 600f) {
            repeat(5) {
                vaporParticles.add(Vapor(x + Random.nextFloat() * 100f - 50f, y, 2f + Random.nextFloat() * 3f))
            }
            cloudSize = (cloudSize + 2f).coerceAtMost(500f)
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            // Nền trời Gradient cực đẹp
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF81D4FA), Color(0xFFE1F5FE))
                )
            )

            // Vẽ biển ở dưới
            drawRect(
                color = Color(0xFF0288D1),
                topLeft = Offset(0f, size.height * 0.8f),
                size = Size(size.width, size.height * 0.2f)
            )

            // Vẽ hơi nước bay lên
            vaporParticles.forEach { v ->
                drawCircle(Color.White.copy(alpha = v.alpha), 10f, Offset(v.x, v.y))
            }

            // Vẽ đám mây (Tự vẽ bằng các vòng tròn ghép lại)
            if (cloudSize > 50f) {
                val cx = size.width / 2
                val cy = 200f
                drawCircle(cloudColor, cloudSize / 2, Offset(cx, cy))
                drawCircle(cloudColor, cloudSize / 2.5f, Offset(cx - cloudSize / 3, cy + 20f))
                drawCircle(cloudColor, cloudSize / 2.5f, Offset(cx + cloudSize / 3, cy + 20f))
            }

            // Vẽ mưa
            rainParticles.forEach { r ->
                drawLine(Color(0xFFB3E5FC), Offset(r.x, r.y), Offset(r.x, r.y + 20f), strokeWidth = 3f)
            }

            // Vẽ Message hướng dẫn nghệ thuật
            val msgLayout = textMeasurer.measure(
                text = message,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
            )
            drawText(textLayoutResult = msgLayout, topLeft = Offset(size.width/2 - msgLayout.size.width/2, size.height * 0.7f))
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}
    override fun release() {}
}
