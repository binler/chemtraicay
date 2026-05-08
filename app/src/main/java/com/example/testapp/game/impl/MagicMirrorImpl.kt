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
import kotlin.random.Random

class MagicMirrorImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Gương Thần Chiếu Yêu"
    
    private var targetPoseName by mutableStateOf("DANG CÁNH")
    private var matchPercentage by mutableStateOf(0f)
    private var isSuccess by mutableStateOf(false)
    private var lastSuccessTime = 0L

    override fun init(width: Int, height: Int, initialPlayers: List<Player>) {
        players.clear()
        players.addAll(initialPlayers)
        players.forEach { it.score = 0 }
        isSuccess = false
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (isSuccess && currentTime - lastSuccessTime > 2000) {
            isSuccess = false
            targetPoseName = listOf("DANG CÁNH", "GIƠ TAY CAO", "CHẠM VAI").random()
        }

        if (!isSuccess) {
            checkPoseMatch()
        }
    }

    private fun checkPoseMatch() {
        // Kiểm tra tư thế cho cả 2 người chơi
        val isAllMatch = players.all { p ->
            val lw = p.leftWrist ?: return@all false
            val rw = p.rightWrist ?: return@all false
            val ls = p.leftShoulder ?: return@all false
            val rs = p.rightShoulder ?: return@all false

            when (targetPoseName) {
                "DANG CÁNH" -> lw.y > ls.y - 50 && lw.y < ls.y + 50 && rw.y > rs.y - 50 && rw.y < rs.y + 50
                "GIƠ TAY CAO" -> lw.y < ls.y - 100 && rw.y < rs.y - 100
                "CHẠM VAI" -> dist(lw.x, lw.y, ls.x, ls.y) < 100 && dist(rw.x, rw.y, rs.x, rs.y) < 100
                else -> false
            }
        }

        if (isAllMatch) {
            isSuccess = true
            lastSuccessTime = System.currentTimeMillis()
            players.forEach { it.score += 100 }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Vẽ hướng dẫn
            val hintLayout = textMeasurer.measure(
                text = if (isSuccess) "BẠN GIỎI QUÁ! ✨" else "HÃY CÙNG: $targetPoseName",
                style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, color = if (isSuccess) Color.Green else Color.White)
            )
            drawText(textLayoutResult = hintLayout, topLeft = Offset(center.x - hintLayout.size.width/2, 200f))

            // Vẽ khung người chơi để hướng dẫn tạo dáng
            players.forEach { p ->
                val color = if (isSuccess) Color.Green else p.color
                // Vẽ xương đơn giản
                val drawLineBetween = { p1: PoseDetectorService.Point?, p2: PoseDetectorService.Point? ->
                    if (p1 != null && p2 != null) drawLine(color, Offset(p1.x, p1.y), Offset(p2.x, p2.y), strokeWidth = 10f)
                }
                drawLineBetween(p.leftShoulder, p.rightShoulder)
                drawLineBetween(p.leftShoulder, p.leftWrist)
                drawLineBetween(p.rightShoulder, p.rightWrist)
                
                p.leftWrist?.let { drawCircle(color, 20f, Offset(it.x, it.y)) }
                p.rightWrist?.let { drawCircle(color, 20f, Offset(it.x, it.y)) }
            }
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = kotlin.math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2))
    override fun release() {}
}
