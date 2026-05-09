package com.example.testapp.game.impl

import androidx.compose.runtime.mutableStateListOf
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
import kotlin.math.sqrt

class BodyXRayImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Khám Phá Cơ Thể"
    override val category = CategoryType.SCIENCE
    override val targetAge = AgeGroup.TODDLER
    override val icon = "🦴"
    override val isAR = true

    private var onSpeech: (String) -> Unit = {}
    private var lastSpeechTime = 0L

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        players.addAll(initialPlayers)
        onSpeech("Chào con! Hãy chỉ vào các bộ phận trên cơ thể để khám phá nhé.")
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpeechTime < 2500) return

        players.forEach { player ->
            val lw = player.leftWrist ?: return@forEach
            val rw = player.rightWrist ?: return@forEach
            val ls = player.leftShoulder ?: return@forEach
            val rs = player.rightShoulder ?: return@forEach
            val lh = player.leftHip ?: return@forEach

            // Logic đơn giản: Nếu tay đưa đến gần bộ phận nào đó
            // Kiểm tra tay chạm đầu (Giả định đầu ở trên vai)
            if (lw.y < ls.y - 100f || rw.y < rs.y - 100f) {
                onSpeech("Đây là Cái Đầu. Head!")
                lastSpeechTime = currentTime
            } else if (dist(lw.x, lw.y, rs.x, rs.y) < 100f || dist(rw.x, rw.y, ls.x, ls.y) < 100f) {
                onSpeech("Đây là Cái Vai. Shoulder!")
                lastSpeechTime = currentTime
            } else if (lw.y > lh.y - 50f && lw.y < lh.y + 150f) {
                onSpeech("Đây là Cái Bụng. Tummy!")
                lastSpeechTime = currentTime
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            players.forEach { p ->
                val color = p.color
                // Vẽ nhãn bộ phận cơ thể nếu AI nhận diện được
                p.leftShoulder?.let { 
                    val layout = textMeasurer.measure("VAI", TextStyle(fontSize = 18.sp, color = color, fontWeight = FontWeight.Bold))
                    drawText(textLayoutResult = layout, topLeft = Offset(it.x, it.y - 40f))
                }
                
                // Vẽ xương đơn giản để bé dễ hình dung
                fun drawBone(p1: com.example.testapp.services.PoseDetectorService.Point?, p2: com.example.testapp.services.PoseDetectorService.Point?) {
                    if (p1 != null && p2 != null) drawLine(color.copy(alpha = 0.5f), Offset(p1.x, p1.y), Offset(p2.x, p2.y), strokeWidth = 8f)
                }
                drawBone(p.leftShoulder, p.rightShoulder)
                drawBone(p.leftShoulder, p.leftWrist)
                drawBone(p.rightShoulder, p.rightWrist)
                drawBone(p.leftShoulder, p.leftHip)
                drawBone(p.rightShoulder, p.rightHip)
                drawBone(p.leftHip, p.rightHip)
            }
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt(((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)).toDouble()).toFloat()
    override fun onTouch(x: Float, y: Float) {}
    override fun release() {}
}
