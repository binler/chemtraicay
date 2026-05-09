package com.example.testapp.game.impl

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.testapp.game.core.AgeGroup
import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import com.example.testapp.services.PoseDetectorService

class AirPainterImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Họa Sĩ Không Trung"
    override val category = CategoryType.LEARNING
    override val targetAge = AgeGroup.TODDLER // Thích hợp cho cả 2 nhưng ưu tiên bé nhỏ
    override val icon = "🎨"
    override val isAR = true // Air Painter uses camera for tracking

    private val playerPaths = mutableMapOf<String, MutableList<Offset>>()
    private var onSpeech: (String) -> Unit = {}

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        players.addAll(initialPlayers)
        playerPaths.clear()
        initialPlayers.forEach { playerPaths[it.id] = mutableListOf() }
        onSpeech("Con hãy vẽ một bức tranh thật đẹp bằng tay mình nhé!")
    }

    override fun update(width: Int, height: Int) {
        players.forEach { player ->
            val wrists = listOfNotNull(player.leftWrist, player.rightWrist)
            wrists.forEach { wrist ->
                playerPaths[player.id]?.let { path ->
                    path.add(Offset(wrist.x, wrist.y))
                    if (path.size > 200) path.removeAt(0) // Giới hạn nét vẽ để không lag
                }
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun onTouch(x: Float, y: Float) {
        // Clear screen on touch? Maybe just leave it for now
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            drawRect(Color(0xFF121212)) // Nền tối để nổi bật nét vẽ dạ quang
            
            players.forEach { player ->
                val path = playerPaths[player.id] ?: return@forEach
                if (path.size < 2) return@forEach
                
                for (i in 0 until path.size - 1) {
                    val alpha = (i.toFloat() / path.size)
                    drawLine(
                        color = player.color.copy(alpha = alpha),
                        start = path[i],
                        end = path[i + 1],
                        strokeWidth = 15f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Vẽ "bút vẽ" là đôi tay
            players.forEach { p ->
                p.leftWrist?.let { drawCircle(Color.White, 10f, Offset(it.x, it.y)); drawCircle(p.color.copy(0.5f), 40f, Offset(it.x, it.y)) }
                p.rightWrist?.let { drawCircle(Color.White, 10f, Offset(it.x, it.y)); drawCircle(p.color.copy(0.5f), 40f, Offset(it.x, it.y)) }
            }
        }
    }

    override fun release() {}
}
