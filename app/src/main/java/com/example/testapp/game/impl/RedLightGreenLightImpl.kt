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
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import kotlin.random.Random

class RedLightGreenLightImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Đèn Xanh Đèn Đỏ"
    
    private var isRedLight by mutableStateOf(false)
    private var lastSwitchTime = 0L
    private var nextSwitchDuration = 3000L
    
    // Lưu trữ tiến trình của mỗi người chơi (0.0 to 1.0)
    private val progresses = mutableMapOf<String, Float>()

    override fun init(width: Int, height: Int, initialPlayers: List<Player>) {
        players.clear()
        players.addAll(initialPlayers)
        players.forEach { 
            it.score = 0
            progresses[it.id] = 0f
        }
        isRedLight = false
        lastSwitchTime = System.currentTimeMillis()
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        
        // Chuyển đổi đèn
        if (currentTime - lastSwitchTime > nextSwitchDuration) {
            isRedLight = !isRedLight
            lastSwitchTime = currentTime
            nextSwitchDuration = if (isRedLight) 2000L else (2000L + Random.nextLong(3000L))
        }

        players.forEach { player ->
            val currentProgress = progresses[player.id] ?: 0f
            if (currentProgress >= 1f) return@forEach

            // Tính toán chuyển động (vận tốc cổ tay)
            val movement = calculateMovement(player)
            
            if (isRedLight) {
                if (movement > 15f) { // Nếu di chuyển quá mạnh khi đèn đỏ
                    progresses[player.id] = 0f // Bị phạt quay lại vạch xuất phát
                }
            } else {
                progresses[player.id] = (currentProgress + movement / 1000f).coerceAtMost(1f)
                if (progresses[player.id] == 1f) player.score += 100
            }
        }
    }

    private fun calculateMovement(player: Player): Float {
        val lw = player.leftWrist ?: return 0f
        val rw = player.rightWrist ?: return 0f
        // Giả lập vận tốc bằng cách đo sự thay đổi tọa độ (đơn giản hóa cho demo)
        return Random.nextFloat() * 20f // Trong thực tế sẽ so sánh với frame trước
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Vẽ đèn
            drawCircle(if (isRedLight) Color.Red else Color.DarkGray, 50f, Offset(center.x, 100f))
            drawCircle(if (!isRedLight) Color.Green else Color.DarkGray, 50f, Offset(center.x, 220f))

            // Vẽ đường chạy và người chơi
            players.forEachIndexed { index, player ->
                val prog = progresses[player.id] ?: 0f
                val yPos = 400f + index * 150f
                
                // Đường chạy
                drawLine(Color.Gray, Offset(100f, yPos), Offset(size.width - 100f, yPos), 5f)
                // Đích
                drawLine(Color.Yellow, Offset(size.width - 100f, yPos - 50), Offset(size.width - 100f, yPos + 50), 10f)
                
                // Vị trí người chơi
                val xPos = 100f + prog * (size.width - 200f)
                drawCircle(player.color, 40f, Offset(xPos, yPos))
                
                val nameLayout = textMeasurer.measure(player.name, TextStyle(fontSize = 20.sp, color = Color.White))
                drawText(textLayoutResult = nameLayout, topLeft = Offset(xPos - nameLayout.size.width/2, yPos + 50f))
            }

            if (isRedLight) {
                val alertLayout = textMeasurer.measure("ĐỨNG IM!!!", TextStyle(fontSize = 60.sp, fontWeight = FontWeight.Bold, color = Color.Red))
                drawText(textLayoutResult = alertLayout, topLeft = Offset(center.x - alertLayout.size.width/2, center.y))
            }
        }
    }

    override fun release() {}
}
