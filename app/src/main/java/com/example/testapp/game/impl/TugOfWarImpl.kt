package com.example.testapp.game.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import com.example.testapp.services.PoseDetectorService

import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.AgeGroup

class TugOfWarImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Kéo Co Thần Kỳ"
    override val category = CategoryType.EXERCISE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🤝"
    override val isAR = true
    
    private var ropePosition by mutableStateOf(0f) // -300 to 300
    private val winThreshold = 400f

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        players.clear()
        players.addAll(initialPlayers)
        players.forEach { it.score = 0 }
        ropePosition = 0f
    }

    override fun update(width: Int, height: Int) {
        if (kotlin.math.abs(ropePosition) >= winThreshold) return

        val p1 = players.getOrNull(0)
        val p2 = players.getOrNull(1)

        // Tính lực kéo cho P1 (Cha) - Dựa vào vận động tay
        val p1Power = calculatePower(p1)
        // Tính lực kéo cho P2 (Con)
        val p2Power = calculatePower(p2)

        ropePosition += (p2Power - p1Power) * 5f
    }

    private fun calculatePower(player: Player?): Float {
        val lw = player?.leftWrist ?: return 0f
        val rw = player?.rightWrist ?: return 0f
        val ls = player?.leftShoulder ?: return 0f
        // Lực tính bằng vận tốc di chuyển tay hoặc khoảng cách co duỗi
        return kotlin.math.abs(lw.y - ls.y) / 200f + kotlin.math.abs(rw.y - ls.y) / 200f
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun onTouch(x: Float, y: Float) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            val center = Offset(size.width / 2, size.height / 2)
            val currentRopeX = center.x + ropePosition

            // Vẽ dây thừng
            drawLine(Color(0xFF8B4513), Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 15f)
            
            // Vẽ nút thắt trung tâm
            drawCircle(Color.Red, 30f, Offset(currentRopeX, center.y))
            drawCircle(Color.White, 35f, Offset(currentRopeX, center.y), style = Stroke(5f))

            // Vẽ vạch đích
            drawLine(Color.Yellow, Offset(center.x - winThreshold, center.y - 100), Offset(center.x - winThreshold, center.y + 100), 10f)
            drawLine(Color.Yellow, Offset(center.x + winThreshold, center.y - 100), Offset(center.x + winThreshold, center.y + 100), 10f)

            // Thông báo thắng cuộc
            if (kotlin.math.abs(ropePosition) >= winThreshold) {
                val winner = if (ropePosition > 0) players.getOrNull(1)?.name else players.getOrNull(0)?.name
                val layout = textMeasurer.measure("$winner THẮNG!", TextStyle(fontSize = 50.sp, fontWeight = FontWeight.Black, color = Color.Green))
                drawText(textLayoutResult = layout, topLeft = Offset(center.x - layout.size.width/2, center.y - 200f))
            }
        }
    }

    override fun release() {}
}
