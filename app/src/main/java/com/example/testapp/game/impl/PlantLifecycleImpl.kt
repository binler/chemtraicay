package com.example.testapp.game.impl

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.*
import com.example.testapp.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

class PlantLifecycleImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Vòng Đời Của Cây"
    override val category = CategoryType.SCIENCE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🌱"
    override val isAR = false

    private var stage by mutableStateOf(0) 
    private var growthProgress by mutableStateOf(0f)
    private var message by mutableStateOf("Tưới nước phép thuật để hạt giống nảy mầm nhé!")
    private var onSpeech: (String) -> Unit = {}
    private var lastInteractionTime = 0L

    override fun init(context: android.content.Context, width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        stage = 0
        growthProgress = 0f
        onSpeech(message)
    }

    override fun update(width: Int, height: Int) {
        if (growthProgress < 1f) growthProgress += 0.02f
    }

    override fun onTouch(x: Float, y: Float) {
        val now = System.currentTimeMillis()
        if (now - lastInteractionTime < 500) return
        
        if (growthProgress >= 0.9f) {
            stage = (stage + 1) % 5
            growthProgress = 0f
            message = when(stage) {
                1 -> "Hạt mầm nhỏ bé đã nhú lên!"
                2 -> "Cây đang lớn dần đón nắng mai."
                3 -> "Nhiều lá xanh thật là khỏe mạnh!"
                4 -> "Tuyệt vời! Bông hoa rực rỡ đã nở rồi!"
                else -> "Gieo một hạt giống mới nào con!"
            }
            onSpeech(message)
            lastInteractionTime = now
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        val size = drawScope.size
        val cx = size.width / 2
        val groundY = size.height * 0.8f

        // Nền
        drawScope.drawRect(Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFFE1F5FE))))
        drawScope.drawCircle(SunnyYellow, 60f, Offset(150f, 150f))
        drawScope.drawRect(Color(0xFF795548), topLeft = Offset(0f, groundY), size = Size(size.width, size.height * 0.2f))

        // Vẽ Cây (Đúng tọa độ)
        if (stage >= 1) {
            val stemHeight = (stage * 80f) * growthProgress
            val top = Offset(cx, groundY - stemHeight)
            
            // Thân cây
            drawScope.drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(cx, groundY),
                end = top,
                strokeWidth = 15f + stage * 2,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Lá cây (Căn chỉnh chính xác)
            if (stage >= 3) {
                drawScope.drawOval(Color(0xFF81C784), topLeft = Offset(cx - 80f, groundY - stemHeight/2), size = Size(80f, 40f))
                drawScope.drawOval(Color(0xFF81C784), topLeft = Offset(cx, groundY - stemHeight/3), size = Size(80f, 40f))
            }

            // Hoa (Ở trên đỉnh)
            if (stage == 4 && growthProgress > 0.5f) {
                drawScope.drawCircle(SunnyYellow, 45f, top)
                repeat(6) { i ->
                    val angle = (i * 60f) * (Math.PI / 180f).toFloat()
                    drawScope.drawCircle(SoftPink, 35f, Offset(top.x + cos(angle)*55f, top.y + sin(angle)*55f))
                }
            }
        } else {
            // Hạt giống
            drawScope.drawCircle(Color(0xFF5D4037), 15f, Offset(cx, groundY - 10f))
        }

        // Chữ
        val layout = textMeasurer.measure(message, TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark))
        drawScope.drawText(textLayoutResult = layout, topLeft = Offset(cx - layout.size.width/2, 250f))
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}
    override fun release() {}
}
