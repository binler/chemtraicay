package com.example.testapp.game.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.testapp.services.PoseDetectorService

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Player(
    val id: String,
    val name: String,
    val color: Color,
    initialScore: Int = 0
) {
    var score by mutableStateOf(initialScore)
    var leftWrist by mutableStateOf<PoseDetectorService.Point?>(null)
    var rightWrist by mutableStateOf<PoseDetectorService.Point?>(null)
}

interface GameLogic {
    val players: List<Player>
    val name: String
    
    // Khởi tạo game
    fun init(width: Int, height: Int, initialPlayers: List<Player>)
    
    // Cập nhật logic
    fun update(width: Int, height: Int)
    
    // Nhận dữ liệu Pose cho từng Player cụ thể
    fun onPlayersUpdate(updatedPlayers: List<Player>)
    
    // Vẽ giao diện
    fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer)
    
    fun release()
}
