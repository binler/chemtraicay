package com.example.testapp.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.services.PoseDetectorService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Fruit(
    var id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var isSliced: Boolean = false,
    val emoji: String,
    var sliceAngle: Float = 0f,
    var alpha: Float = 1f,
    val isBomb: Boolean = false
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Int, // Color as Int
    var alpha: Float = 1f,
    var size: Float
)

data class SlicedHalf(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var emoji: String,
    var isLeft: Boolean,
    var alpha: Float = 1f
)

// Old FruitNinja implementation, superseded by GameLogic/FruitNinjaImpl
// Keep data classes if needed, or remove entire file if unused.

