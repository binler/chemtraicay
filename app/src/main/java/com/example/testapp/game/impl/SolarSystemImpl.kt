package com.example.testapp.game.impl

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.AgeGroup
import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import kotlin.math.cos
import kotlin.math.sin

class SolarSystemImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Khám Phá Vũ Trụ"
    override val category = CategoryType.SCIENCE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🪐"
    override val isAR = false // PURE TOUCH

    private data class Planet(
        val name: String,
        val emoji: String,
        val fact: String,
        val distance: Float,
        val speed: Float,
        var angle: Float = 0f
    )

    private data class BackgroundStar(
        val x: Float,
        val y: Float,
        val size: Float,
        val speed: Float,
        val phase: Float
    )

    private val stars = mutableStateListOf<BackgroundStar>()
    private val planets = mutableStateListOf<Planet>()
    private var selectedPlanet by mutableStateOf<Planet?>(null)
    private var message by mutableStateOf("Chạm vào các hành tinh để khám phá nhé!")
    private var onSpeech: (String) -> Unit = {}
    private var lastInteractionTime = 0L

    // Lưu kích thước màn hình để dùng trong onTouch
    private var screenWidth = 0f
    private var screenHeight = 0f

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        this.screenWidth = width.toFloat()
        this.screenHeight = height.toFloat()
        players.clear()
        
        stars.clear()
        repeat(80) {
            stars.add(BackgroundStar(
                x = kotlin.random.Random.nextFloat() * width,
                y = kotlin.random.Random.nextFloat() * height,
                size = 1f + kotlin.random.Random.nextFloat() * 3f,
                speed = 0.001f + kotlin.random.Random.nextFloat() * 0.003f,
                phase = kotlin.random.Random.nextFloat() * 2f * kotlin.math.PI.toFloat()
            ))
        }

        planets.clear()
        planets.add(Planet("Mặt Trời", "☀️", "Là ngôi sao ở trung tâm, tỏa ánh sáng cho cả hệ mặt trời.", 0f, 0f))
        planets.add(Planet("Sao Thủy", "☿️", "Là hành tinh gần Mặt Trời nhất, rất là nóng đấy!", 120f, 0.02f))
        planets.add(Planet("Sao Kim", "♀️", "Hành tinh sáng nhất trên bầu trời đêm của chúng ta.", 180f, 0.015f))
        planets.add(Planet("Trái Đất", "🌍", "Ngôi nhà xanh của chúng mình, có không khí và nước.", 250f, 0.01f))
        planets.add(Planet("Sao Hỏa", "♂️", "Được gọi là hành tinh Đỏ vì bề mặt có rất nhiều sắt.", 320f, 0.008f))
        planets.add(Planet("Sao Mộc", "♃️", "Là hành tinh lớn nhất, to gấp nghìn lần Trái Đất!", 400f, 0.006f))
        planets.add(Planet("Sao Thổ", "♄️", "Nổi tiếng với chiếc vòng nhẫn tuyệt đẹp bao quanh.", 480f, 0.004f))

        message = "Chào mừng con đến với Vũ trụ! Hãy chạm vào các hành tinh nào."
        onSpeech(message)
    }

    override fun update(width: Int, height: Int) {
        planets.forEach { planet ->
            if (planet.distance > 0) {
                planet.angle += planet.speed
            }
        }
    }

    override fun onTouch(x: Float, y: Float) {
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        
        planets.forEach { planet ->
            val px = centerX + cos(planet.angle) * planet.distance
            val py = centerY + sin(planet.angle) * planet.distance
            
            if (dist(x, y, px, py) < 80f) {
                selectedPlanet = planet
                message = "${planet.name}: ${planet.fact}"
                onSpeech(message)
                lastInteractionTime = System.currentTimeMillis()
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Vẽ nền đen sâu thẳm
            drawRect(Color(0xFF000011))

            // Vẽ sao lấp lánh
            val time = System.currentTimeMillis()
            stars.forEach { star ->
                val alpha = ((sin(time * star.speed + star.phase) + 1f) / 2f) * 0.8f + 0.2f
                drawCircle(color = Color.White.copy(alpha = alpha), radius = star.size, center = Offset(star.x, star.y))
            }

            // Vẽ quỹ đạo
            planets.forEach { planet ->
                if (planet.distance > 0) {
                    drawCircle(color = Color.White.copy(alpha = 0.15f), radius = planet.distance, center = Offset(centerX, centerY), style = Stroke(width = 1.5f))
                }
            }

            // Vẽ hành tinh
            planets.forEach { planet ->
                val px = centerX + cos(planet.angle) * planet.distance
                val py = centerY + sin(planet.angle) * planet.distance
                
                val emojiSize = if (selectedPlanet == planet) 110.sp else 70.sp
                val layout = textMeasurer.measure(planet.emoji, TextStyle(fontSize = emojiSize))
                drawText(textLayoutResult = layout, topLeft = Offset(px - layout.size.width / 2, py - layout.size.height / 2))
            }

            // Vẽ Message hướng dẫn nghệ thuật
            val msgLayout = textMeasurer.measure(
                text = message,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
            )
            drawText(textLayoutResult = msgLayout, topLeft = Offset(size.width / 2 - msgLayout.size.width / 2, size.height - 150f))
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = kotlin.math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)).toFloat()
    override fun release() {}
}
