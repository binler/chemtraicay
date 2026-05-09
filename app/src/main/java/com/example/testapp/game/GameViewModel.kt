package com.example.testapp.game

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.game.core.AgeGroup
import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import com.example.testapp.game.impl.*
import com.example.testapp.ml.PoseProcessor
import com.example.testapp.services.PoseDetectorService
import com.example.testapp.services.SpeechService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    var activeGame by mutableStateOf<GameLogic?>(null)
    private var poseProcessor: PoseProcessor? = null
    private var speechService: SpeechService? = null
    private var gameJob: Job? = null

    // Quản lý trạng thái theo Spec
    var selectedAge by mutableStateOf(AgeGroup.TODDLER)
    var selectedCategory by mutableStateOf(CategoryType.LEARNING)

    private val player1 = Player(id = "p1", name = "Bé", color = Color.Cyan)
    private val player2 = Player(id = "p2", name = "Ba/Mẹ", color = Color.Magenta)
    
    // Lưu kích thước màn hình để scale tọa độ
    private var screenWidth = 0f
    private var screenHeight = 0f

    val availableGames = listOf(
        FruitNinjaImpl(), SoccerGoalieImpl(), TugOfWarImpl(),
        EnglishFlashcardsImpl(), BubblePopImpl(),
        WaterCycleImpl(), SolarSystemImpl(), PlantLifecycleImpl()
    )

    fun initServices(context: android.content.Context) {
        if (speechService == null) speechService = SpeechService(context)
        if (poseProcessor == null) {
            poseProcessor = PoseProcessor(context)
            // Theo dõi StateFlow dữ liệu Pose liên tục
            viewModelScope.launch {
                poseProcessor?.poseData?.collectLatest { poses ->
                    updateMultiPlayerPoses(poses.getOrNull(0), poses.getOrNull(1))
                }
            }
        }
    }

    private fun updateMultiPlayerPoses(p1: PoseDetectorService.PoseResult?, p2: PoseDetectorService.PoseResult?) {
        val update = { player: Player, res: PoseDetectorService.PoseResult? ->
            if (screenWidth > 0) {
                player.leftWrist = res?.leftWrist?.let { PoseDetectorService.Point(it.x * screenWidth, it.y * screenHeight) }
                player.rightWrist = res?.rightWrist?.let { PoseDetectorService.Point(it.x * screenWidth, it.y * screenHeight) }
                player.leftShoulder = res?.leftShoulder?.let { PoseDetectorService.Point(it.x * screenWidth, it.y * screenHeight) }
                player.rightShoulder = res?.rightShoulder?.let { PoseDetectorService.Point(it.x * screenWidth, it.y * screenHeight) }
            }
        }
        update(player1, p1)
        update(player2, p2)
    }

    fun selectGame(game: GameLogic, width: Int, height: Int) {
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        activeGame = game
        activeGame?.init(width, height, listOf(player1, player2), onSpeech = { speak(it) })
        
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive) {
                activeGame?.onPlayersUpdate(listOf(player1, player2))
                activeGame?.update(width, height)
                delay(16)
            }
        }
    }

    fun speak(text: String) = speechService?.speak(text)
    fun exitGame() { gameJob?.cancel(); activeGame?.release(); activeGame = null }
    fun getPoseAnalyzer() = poseProcessor

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
        poseProcessor?.stop()
        speechService?.shutdown()
    }
}
