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
    var leftShoulder by mutableStateOf<PoseDetectorService.Point?>(null)
    var rightShoulder by mutableStateOf<PoseDetectorService.Point?>(null)
    var leftHip by mutableStateOf<PoseDetectorService.Point?>(null)
    var rightHip by mutableStateOf<PoseDetectorService.Point?>(null)
}

enum class CategoryType { EXERCISE, LEARNING, SCIENCE }
enum class AgeGroup { TODDLER, PRESCHOOL } // TODDLER: 2yo, PRESCHOOL: 5yo

interface GameLogic {
    val players: List<Player>
    val name: String
    val category: CategoryType
    val targetAge: AgeGroup
    val icon: String
    val isAR: Boolean // true: Dùng Camera, false: Dùng Cảm ứng
    
    fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit = {})
    fun update(width: Int, height: Int)
    fun onPlayersUpdate(updatedPlayers: List<Player>)
    fun onTouch(x: Float, y: Float) // Xử lý cảm ứng
    fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer)
    fun release()
}
