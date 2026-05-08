package com.example.testapp.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import com.example.testapp.game.impl.FruitNinjaImpl
import com.example.testapp.game.impl.PunchPointsImpl
import com.example.testapp.services.PoseDetectorService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.testapp.game.impl.FruitNinjaImpl
import com.example.testapp.game.impl.PunchPointsImpl
import com.example.testapp.game.impl.BubblePopImpl
import com.example.testapp.game.impl.CatchStarsImpl
import com.example.testapp.game.impl.MagicMirrorImpl
import com.example.testapp.game.impl.TugOfWarImpl
import com.example.testapp.game.impl.RedLightGreenLightImpl
import com.example.testapp.game.impl.SoccerGoalieImpl

class GameViewModel : ViewModel() {
    // Trạng thái game hiện tại
    var activeGame by mutableStateOf<GameLogic?>(null)
    
    // Danh sách game khả dụng cho GameListScreen
    val availableGames = listOf(
        FruitNinjaImpl(), 
        PunchPointsImpl(),
        BubblePopImpl(),
        CatchStarsImpl(),
        MagicMirrorImpl(),
        TugOfWarImpl(),
        RedLightGreenLightImpl(),
        SoccerGoalieImpl()
    )
    
    // Danh sách Players (Local & Online)
    private val player1 = Player(id = "local_p1", name = "You", color = Color.Cyan)
    private val player2 = Player(id = "remote_p2", name = "Opponent", color = Color.Magenta)
    
    var leftWrist by mutableStateOf<PoseDetectorService.Point?>(null)
    var rightWrist by mutableStateOf<PoseDetectorService.Point?>(null)
    
    private var gameJob: Job? = null

    fun selectGame(game: GameLogic, width: Int, height: Int) {
        activeGame = game
        // Khởi tạo game với 2 người chơi thật
        activeGame?.init(width, height, listOf(player1, player2))
        startGameLoop(width, height)
    }

    fun updateMultiPlayerPoses(
        p1Pose: PoseDetectorService.PoseResult?,
        p2Pose: PoseDetectorService.PoseResult?,
        width: Float,
        height: Float
    ) {
        val updatePlayer = { player: Player, pose: PoseDetectorService.PoseResult? ->
            player.leftWrist = pose?.leftWrist?.let { PoseDetectorService.Point(it.x * width, it.y * height) }
            player.rightWrist = pose?.rightWrist?.let { PoseDetectorService.Point(it.x * width, it.y * height) }
            player.leftShoulder = pose?.leftShoulder?.let { PoseDetectorService.Point(it.x * width, it.y * height) }
            player.rightShoulder = pose?.rightShoulder?.let { PoseDetectorService.Point(it.x * width, it.y * height) }
            player.leftHip = pose?.leftHip?.let { PoseDetectorService.Point(it.x * width, it.y * height) }
            player.rightHip = pose?.rightHip?.let { PoseDetectorService.Point(it.x * width, it.y * height) }
        }

        updatePlayer(player1, p1Pose)
        updatePlayer(player2, p2Pose)
    }

    private fun startGameLoop(width: Int, height: Int) {
        val game = activeGame ?: return
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive) {
                // Không cần simulateRemotePlayer nữa vì đã có 2 người thật
                game.onPlayersUpdate(listOf(player1, player2))
                game.update(width, height)
                delay(16)
            }
        }
    }

    fun exitGame() {
        gameJob?.cancel()
        activeGame?.release()
        activeGame = null
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
        activeGame?.release()
    }
}
