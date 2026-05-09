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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.AgeGroup
import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player

class PlantLifecycleImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Vòng Đời Của Cây"
    override val category = CategoryType.SCIENCE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🌱"
    override val isAR = false // PURE TOUCH

    private var stage by mutableStateOf(0)
    private var message by mutableStateOf("Chạm vào đất để gieo hạt nào!")
    private var onSpeech: (String) -> Unit = {}
    private var lastInteractionTime = 0L

    private val stages = listOf(
        "Chạm vào đất để gieo hạt nào!",
        "Hạt đã nảy mầm rồi! Chạm để tưới nước nhé.",
        "Cây đã lớn hơn và có nhiều lá xanh rồi!",
        "Oa! Cây đã nở hoa thật đẹp. Chúc mừng con!"
    )

    private val emojis = listOf("🫘", "🌱", "🌿", "🌸")

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        stage = 0
        message = stages[0]
        onSpeech(message)
    }

    override fun update(width: Int, height: Int) {}
    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun onTouch(x: Float, y: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInteractionTime < 1000) return

        if (stage < stages.size - 1) {
            stage++
        } else {
            stage = 0
        }
        message = stages[stage]
        onSpeech(message)
        lastInteractionTime = currentTime
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            val center = Offset(size.width / 2, size.height / 2)

            // Bầu trời xanh Gradient nhẹ
            drawRect(Color(0xFFE1F5FE))
            // Mặt đất nâu bo tròn
            drawRect(Color(0xFF795548), topLeft = Offset(0f, size.height * 0.7f), size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.3f))

            // Mặt trời
            val sunLayout = textMeasurer.measure("☀️", TextStyle(fontSize = 100.sp))
            drawText(textLayoutResult = sunLayout, topLeft = Offset(size.width - 200f, 50f))

            // Cây
            val plantLayout = textMeasurer.measure(emojis[stage], TextStyle(fontSize = 200.sp))
            drawText(textLayoutResult = plantLayout, topLeft = Offset(center.x - plantLayout.size.width / 2, size.height * 0.7f - plantLayout.size.height + 50f))

            // Message
            val msgLayout = textMeasurer.measure(
                text = message,
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            )
            drawText(textLayoutResult = msgLayout, topLeft = Offset(center.x - msgLayout.size.width / 2, 150f))
            
            val hintLayout = textMeasurer.measure("Chạm để xem cây lớn lên ✨", TextStyle(fontSize = 18.sp, color = Color.Gray))
            drawText(textLayoutResult = hintLayout, topLeft = Offset(center.x - hintLayout.size.width/2, size.height - 100f))
        }
    }

    override fun release() {}
}
