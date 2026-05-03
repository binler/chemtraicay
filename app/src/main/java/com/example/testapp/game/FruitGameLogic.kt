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

class GameViewModel : ViewModel() {
    val fruits = mutableStateListOf<Fruit>()
    val slicedFruits = mutableStateListOf<SlicedHalf>()
    val particles = mutableStateListOf<Particle>()
    val leftTrail = mutableStateListOf<PoseDetectorService.Point>()
    val rightTrail = mutableStateListOf<PoseDetectorService.Point>()
    
    var score by mutableStateOf(0)
    var comboCount by mutableStateOf(0)
    var lastSliceTime = 0L
    
    var leftWrist by mutableStateOf<PoseDetectorService.Point?>(null)
    var rightWrist by mutableStateOf<PoseDetectorService.Point?>(null)
    
    private val fruitEmojis = listOf("🍎", "🍊", "🍉", "🍓", "🍍", "🍐", "🍋", "🍌")
    private val bombEmoji = "💣"

    private var gameJob: Job? = null
    private val gravity = 0.35f

    fun startGame(screenWidth: Int, screenHeight: Int) {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            var lastSpawnTime = 0L
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                
                val spawnInterval = when {
                    score > 500 -> 600L
                    score > 200 -> 1000L
                    else -> 1500L
                }
                
                if (currentTime - lastSpawnTime > spawnInterval) {
                    spawnFruit(screenWidth, screenHeight)
                    lastSpawnTime = currentTime
                }

                // Reset combo if no slice for 500ms
                if (currentTime - lastSliceTime > 500 && comboCount > 0) {
                    comboCount = 0
                }

                updateFruits(screenHeight)
                updateTrails()
                updateParticles(screenHeight)
                checkCollisions()
                
                delay(16)
            }
        }
    }

    private fun spawnFruit(width: Int, height: Int) {
        val x = (0.15f + Random.nextFloat() * 0.7f) * width 
        val vx = (Random.nextFloat() - 0.5f) * 12f
        val vy = -18f - Random.nextFloat() * 12f
        
        val isBomb = Random.nextFloat() < 0.2f // 20% cơ hội ra bom
        val emoji = if (isBomb) bombEmoji else fruitEmojis.random()
        
        fruits.add(Fruit(System.currentTimeMillis(), x, height.toFloat() + 50f, vx, vy, false, emoji, isBomb = isBomb))
    }

    private fun updateFruits(height: Int) {
        // Update active fruits
        val fruitIterator = fruits.iterator()
        while (fruitIterator.hasNext()) {
            val fruit = fruitIterator.next()
            fruit.x += fruit.vx
            fruit.y += fruit.vy
            fruit.vy += gravity

            if (fruit.y > height + 100) fruitIterator.remove()
        }

        // Update sliced halves
        val slicedIterator = slicedFruits.iterator()
        while (slicedIterator.hasNext()) {
            val half = slicedIterator.next()
            half.x += half.vx
            half.y += half.vy
            half.vy += gravity
            half.rotation += 5f
            half.alpha -= 0.02f

            if (half.y > height + 100 || half.alpha <= 0) slicedIterator.remove()
        }
    }

    private fun updateParticles(height: Int) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.vy += gravity * 0.5f
            p.alpha -= 0.03f
            if (p.alpha <= 0 || p.y > height) iterator.remove()
        }
    }

    private fun updateTrails() {
        leftWrist?.let { 
            leftTrail.add(it)
            if (leftTrail.size > 10) leftTrail.removeAt(0)
        } ?: leftTrail.clear()

        rightWrist?.let { 
            rightTrail.add(it)
            if (rightTrail.size > 10) rightTrail.removeAt(0)
        } ?: rightTrail.clear()
    }

    private fun checkCollisions() {
        val wristRadius = 60f
        val fruitRadius = 70f

        val wrists = listOfNotNull(leftWrist, rightWrist)
        
        wrists.forEach { wrist ->
            fruits.forEach { fruit ->
                if (!fruit.isSliced) {
                    val dx = wrist.x - fruit.x
                    val dy = wrist.y - fruit.y
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                    
                    if (distance < wristRadius + fruitRadius) {
                        if (fruit.isBomb) {
                            hitBomb(fruit)
                        } else {
                            sliceFruit(fruit)
                        }
                    }
                }
            }
        }
    }

    private fun sliceFruit(fruit: Fruit) {
        fruit.isSliced = true
        val currentTime = System.currentTimeMillis()
        
        lastSliceTime = currentTime
        comboCount++
        
        val comboBonus = if (comboCount >= 3) comboCount * 5 else 0
        score += 10 + comboBonus
        
        // Create two halves
        slicedFruits.add(SlicedHalf(fruit.x, fruit.y, fruit.vx - 5f, fruit.vy - 2f, 0f, fruit.emoji, true))
        slicedFruits.add(SlicedHalf(fruit.x, fruit.y, fruit.vx + 5f, fruit.vy + 2f, 0f, fruit.emoji, false))
        
        // Create Splatter particles (Màu sắc dựa theo trái cây đơn giản)
        val particleColor = when(fruit.emoji) {
            "🍎", "🍓", "🍉" -> 0xFFFF0000.toInt()
            "🍊" -> 0xFFFFA500.toInt()
            "🍋", "🍌", "🍍" -> 0xFFFFFF00.toInt()
            else -> 0xFF00FF00.toInt()
        }
        
        repeat(8) {
            particles.add(Particle(
                fruit.x, fruit.y, 
                (Random.nextFloat() - 0.5f) * 15f, 
                (Random.nextFloat() - 0.5f) * 15f,
                particleColor,
                size = 10f + Random.nextFloat() * 15f
            ))
        }
    }

    private fun hitBomb(fruit: Fruit) {
        fruit.isSliced = true // Biến mất
        score = (score - 50).coerceAtLeast(0) // Trừ 50 điểm
        comboCount = 0
        
        // Hiệu ứng nổ đen
        repeat(15) {
            particles.add(Particle(
                fruit.x, fruit.y, 
                (Random.nextFloat() - 0.5f) * 25f, 
                (Random.nextFloat() - 0.5f) * 25f,
                0xFF333333.toInt(),
                size = 15f + Random.nextFloat() * 20f
            ))
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
    }
}
