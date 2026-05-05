package com.example.testapp.game.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import com.example.testapp.services.PoseDetectorService
import kotlin.random.Random

class FruitNinjaImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Fruit Ninja Multiplayer"
    
    private val fruits = mutableStateListOf<Fruit>()
    private val slicedFruits = mutableStateListOf<SlicedHalf>()
    private val particles = mutableStateListOf<Particle>()
    
    // Lưu trữ Trail cho từng Player theo ID
    private val playerTrails = mutableMapOf<String, MutableList<PoseDetectorService.Point>>()
    
    private var lastSpawnTime = 0L
    private val gravity = 0.35f

    data class Fruit(var id: Long, var x: Float, var y: Float, var vx: Float, var vy: Float, 
                     var isSliced: Boolean, val emoji: String, val isBomb: Boolean)
    data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, 
                        var color: Int, var alpha: Float, var size: Float)
    data class SlicedHalf(var x: Float, var y: Float, var vx: Float, var vy: Float, 
                          var rotation: Float, var emoji: String, var isLeft: Boolean, var alpha: Float)

    override fun init(width: Int, height: Int, initialPlayers: List<Player>) {
        players.clear()
        players.addAll(initialPlayers)
        playerTrails.clear()
        initialPlayers.forEach { playerTrails[it.id] = mutableListOf() }
        fruits.clear()
        particles.clear()
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        
        // Spawning based on average score or total score
        if (currentTime - lastSpawnTime > 1200L) {
            spawnFruit(width, height)
            lastSpawnTime = currentTime
        }

        updatePhysics(height)
        checkCollisions()
        updateTrails()
    }

    private fun spawnFruit(width: Int, height: Int) {
        val x = (0.15f + Random.nextFloat() * 0.7f) * width
        val vx = (Random.nextFloat() - 0.5f) * 12f
        val vy = -18f - Random.nextFloat() * 12f
        val isBomb = Random.nextFloat() < 0.1f
        val emoji = if (isBomb) "💣" else listOf("🍎", "🍊", "🍉", "🍓", "🍍").random()
        fruits.add(Fruit(System.currentTimeMillis(), x, height.toFloat() + 50f, vx, vy, false, emoji, isBomb))
    }

    private fun updatePhysics(height: Int) {
        fruits.removeAll { fruit ->
            fruit.x += fruit.vx; fruit.y += fruit.vy; fruit.vy += gravity
            fruit.y > height + 100 || fruit.isSliced
        }
        slicedFruits.removeAll { half ->
            half.x += half.vx; half.y += half.vy; half.vy += gravity
            half.rotation += 5f; half.alpha -= 0.02f
            half.alpha <= 0 || half.y > height + 100
        }
        particles.removeAll { p ->
            p.x += p.vx; p.y += p.vy; p.vy += gravity * 0.5f; p.alpha -= 0.03f
            p.alpha <= 0
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {
        // Cập nhật tọa độ wrist từ server hoặc local camera
        updatedPlayers.forEach { updated ->
            players.find { it.id == updated.id }?.let { 
                it.leftWrist = updated.leftWrist
                it.rightWrist = updated.rightWrist
            }
        }
    }

    private fun updateTrails() {
        players.forEach { player ->
            val trail = playerTrails[player.id] ?: return@forEach
            player.leftWrist?.let { 
                trail.add(it)
                if (trail.size > 10) trail.removeAt(0)
            } ?: trail.clear()
        }
    }

    private fun checkCollisions() {
        players.forEach { player ->
            val wrists = listOfNotNull(player.leftWrist, player.rightWrist)
            wrists.forEach { wrist ->
                fruits.forEach { fruit ->
                    if (!fruit.isSliced && dist(wrist.x, wrist.y, fruit.x, fruit.y) < 130) {
                        if (fruit.isBomb) {
                            player.score = (player.score - 50).coerceAtLeast(0)
                            fruit.isSliced = true
                        } else {
                            player.score += 10
                            sliceFruit(fruit, player.color)
                        }
                    }
                }
            }
        }
    }

    private fun sliceFruit(fruit: Fruit, playerColor: Color) {
        fruit.isSliced = true
        slicedFruits.add(SlicedHalf(fruit.x, fruit.y, fruit.vx - 5f, fruit.vy - 2f, 0f, fruit.emoji, true, 1f))
        slicedFruits.add(SlicedHalf(fruit.x, fruit.y, fruit.vx + 5f, fruit.vy + 2f, 0f, fruit.emoji, false, 1f))
        
        repeat(6) {
            particles.add(Particle(fruit.x, fruit.y, (Random.nextFloat()-0.5f)*15f, (Random.nextFloat()-0.5f)*15f, playerColor.toArgb(), 1f, 15f))
        }
    }

    private fun Color.toArgb(): Int = (alpha * 255).toInt() shl 24 or ((red * 255).toInt() shl 16) or ((green * 255).toInt() shl 8) or (blue * 255).toInt()

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            // Draw Particles
            particles.forEach { p ->
                drawCircle(Color(p.color).copy(alpha = p.alpha), p.size, Offset(p.x, p.y))
            }

            // Draw Fruits
            fruits.forEach { fruit ->
                val layout = textMeasurer.measure(fruit.emoji, TextStyle(fontSize = 100.sp))
                drawText(textLayoutResult = layout, topLeft = Offset(fruit.x - layout.size.width/2, fruit.y - layout.size.height/2))
            }

            // Draw Sliced
            slicedFruits.forEach { half ->
                val layout = textMeasurer.measure(half.emoji, TextStyle(fontSize = 100.sp))
                withTransform({
                    translate(half.x, half.y)
                    rotate(half.rotation)
                    if (half.isLeft) clipRect(left=-100f, top=-100f, right=0f, bottom=100f)
                    else clipRect(left=0f, top=-100f, right=100f, bottom=100f)
                }) {
                    drawText(textLayoutResult = layout, topLeft = Offset(-layout.size.width/2f, -layout.size.height/2f), alpha = half.alpha)
                }
            }

            // Draw Players' Wrists & Trails
            players.forEach { player ->
                val trail = playerTrails[player.id] ?: return@forEach
                drawTrail(trail, player.color)
                
                player.leftWrist?.let { 
                    drawCircle(Color.White, 15f, Offset(it.x, it.y))
                    drawCircle(player.color.copy(0.3f), 60f, Offset(it.x, it.y)) 
                }
                player.rightWrist?.let { 
                    drawCircle(Color.White, 15f, Offset(it.x, it.y))
                    drawCircle(player.color.copy(0.3f), 60f, Offset(it.x, it.y)) 
                }
            }
        }
    }

    private fun DrawScope.drawTrail(trail: List<PoseDetectorService.Point>, color: Color) {
        for (i in 0 until trail.size - 1) {
            drawLine(color.copy(alpha = (i.toFloat()/trail.size)*0.8f), 
                Offset(trail[i].x, trail[i].y), Offset(trail[i+1].x, trail[i+1].y), 
                strokeWidth = (i.toFloat()/trail.size)*30f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = kotlin.math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2))

    override fun release() {}
}
