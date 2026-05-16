package com.example.testapp.game.impl

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.*
import com.example.testapp.ui.theme.*
import kotlin.math.sqrt

class BodyXRayImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Khám Phá Cơ Thể"
    override val category = CategoryType.SCIENCE
    override val targetAge = AgeGroup.TODDLER
    override val icon = "🦴"
    override val isAR = false // CẢM ỨNG CHO BÉ 2 TUỔI

    private data class BodyPart(val name: String, val x: Float, val y: Float, val radius: Float)
    private val parts = mutableStateListOf<BodyPart>()
    private var onSpeech: (String) -> Unit = {}
    private var lastInteractionTime = 0L

    override fun init(context: android.content.Context, width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        parts.clear()
        
        val cx = width / 2f
        val cy = height / 2f
        
        // Thiết lập tọa độ tương tác trên hình nhân
        parts.add(BodyPart("Cái Đầu", cx, cy - 250f, 80f))
        parts.add(BodyPart("Cái Vai", cx - 120f, cy - 120f, 50f))
        parts.add(BodyPart("Cái Vai", cx + 120f, cy - 120f, 50f))
        parts.add(BodyPart("Cái Bụng", cx, cy + 50f, 100f))
        parts.add(BodyPart("Bàn Tay", cx - 220f, cy + 50f, 60f))
        parts.add(BodyPart("Bàn Tay", cx + 220f, cy + 50f, 60f))
        
        onSpeech("Chào con! Hãy chạm vào các bộ phận trên cơ thể bạn nhỏ này nhé.")
    }

    override fun update(width: Int, height: Int) {}
    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun onTouch(x: Float, y: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInteractionTime < 1000) return

        parts.forEach { part ->
            val dx = x - part.x
            val dy = y - part.y
            if (sqrt((dx * dx + dy * dy).toDouble()) < part.radius) {
                onSpeech("Đây là " + part.name + "!")
                lastInteractionTime = currentTime
                return@forEach
            }
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            // Nền trắng kem
            drawRect(CreamWhite)

            val cx = size.width / 2f
            val cy = size.height / 2f

            // Vẽ hình nhân đơn giản bằng các khối hình Pastel
            drawCircle(Color.LightGray.copy(alpha = 0.5f), 100f, Offset(cx, cy - 250f)) // Đầu
            drawRoundRect(
                color = Color.LightGray.copy(alpha = 0.5f),
                topLeft = Offset(cx - 100f, cy - 150f),
                size = Size(200f, 350f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f, 40f)
            ) // Thân
            
            // Vẽ các điểm tương tác rực rỡ
            parts.forEach { part ->
                drawCircle(
                    color = SoftPink.copy(alpha = 0.4f),
                    radius = part.radius,
                    center = Offset(part.x, part.y)
                )
                drawCircle(
                    color = SoftPink,
                    radius = 10f,
                    center = Offset(part.x, part.y)
                )
            }

            val hintLayout = textMeasurer.measure(
                "Chạm vào các vòng tròn hồng để học tên nhé! ✨", 
                TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
            )
            drawText(
                textLayoutResult = hintLayout, 
                topLeft = Offset(size.width/2 - hintLayout.size.width/2, size.height - 120f)
            )
        }
    }

    override fun release() {}
}
