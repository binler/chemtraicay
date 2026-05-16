package com.example.testapp.game.impl

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.*
import com.example.testapp.services.PoseDetectorService
import kotlin.random.Random

class FruitNinjaImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Chém Trái Cây"
    override val category = CategoryType.EXERCISE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🍎"
    override val isAR = true

    private val fruits = mutableStateListOf<Fruit>()
    private val slicedFruits = mutableStateListOf<SlicedHalf>()
    private val particles = mutableStateListOf<Particle>()
    private var lastSpawnTime = 0L

    data class Fruit(var id: Long, var x: Float, var y: Float, var vx: Float, var vy: Float, var isSliced: Boolean, val emoji: String, val isBomb: Boolean)
    data class SlicedHalf(var x: Float, var y: Float, var vx: Float, var vy: Float, var rotation: Float, var emoji: String, var isLeft: Boolean, var alpha: Float)
    data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, val color: Color, var alpha: Float = 1f)

    override fun init(context: android.content.Context, width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        players.clear()
        players.addAll(initialPlayers)
        players.forEach { it.score = 0 }
        fruits.clear()
        slicedFruits.clear()
        particles.clear()
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime > 1200L) {
            val x = (0.2f + Random.nextFloat() * 0.6f) * width
            val isBomb = Random.nextFloat() < 0.15f
            fruits.add(Fruit(System.currentTimeMillis(), x, height.toFloat() + 50f, (Random.nextFloat() - 0.5f) * 10f, -15f - Random.nextFloat() * 10f, false, if (isBomb) "💣" else listOf("🍎", "🍊", "🍉", "🍓", "🍍").random(), isBomb))
            lastSpawnTime = currentTime
        }
        fruits.forEach { it.x += it.vx; it.y += it.vy; it.vy += 0.35f }
        fruits.removeAll { it.y > height + 100 || it.isSliced }
        slicedFruits.forEach { it.x += it.vx; it.y += it.vy; it.vy += 0.35f; it.rotation += 5f; it.alpha -= 0.02f }
        slicedFruits.removeAll { it.alpha <= 0 || it.y > height + 100 }
        
        particles.forEach { it.x += it.vx; it.y += it.vy; it.vy += 0.1f; it.alpha -= 0.02f }
        particles.removeAll { it.alpha <= 0 }
        
        checkCollisions()
    }

    private fun checkCollisions() {
        players.forEach { player ->
            val wrists = listOfNotNull(player.leftWrist, player.rightWrist)
            wrists.forEach { wrist ->
                fruits.forEach { fruit ->
                    if (!wrist.x.isNaN() && !fruit.isSliced && dist(wrist.x, wrist.y, fruit.x, fruit.y) < 100) {
                        if (fruit.isBomb) player.score = (player.score - 50).coerceAtLeast(0) else player.score += 10
                        fruit.isSliced = true
                        
                        if (!fruit.isBomb) {
                            slicedFruits.add(SlicedHalf(fruit.x, fruit.y, fruit.vx - 5, fruit.vy, 0f, fruit.emoji, true, 1f))
                            slicedFruits.add(SlicedHalf(fruit.x, fruit.y, fruit.vx + 5, fruit.vy, 0f, fruit.emoji, false, 1f))
                            
                            // Thêm hạt màu sắc rực rỡ
                            repeat(10) {
                                particles.add(Particle(fruit.x, fruit.y, (Random.nextFloat() - 0.5f) * 15f, (Random.nextFloat() - 0.5f) * 15f, 
                                    listOf(Color.Yellow, Color.White, Color.Cyan, Color.Magenta).random()))
                            }
                        } else {
                            // Thêm hạt màu xám khi trúng bom
                            repeat(15) {
                                particles.add(Particle(fruit.x, fruit.y, (Random.nextFloat() - 0.5f) * 20f, (Random.nextFloat() - 0.5f) * 20f, Color.Gray))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}
    override fun onTouch(x: Float, y: Float) {}

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            // Vẽ các hạt lấp lánh trước
            particles.forEach { p ->
                drawCircle(p.color.copy(alpha = p.alpha), radius = 8f * p.alpha, center = Offset(p.x, p.y))
            }
            fruits.forEach {
                val layout = textMeasurer.measure(it.emoji, TextStyle(fontSize = 80.sp))
                drawText(textLayoutResult = layout, topLeft = Offset(it.x - layout.size.width/2, it.y - layout.size.height/2))
            }
            slicedFruits.forEach { half ->
                val layout = textMeasurer.measure(half.emoji, TextStyle(fontSize = 80.sp))
                withTransform({ translate(half.x, half.y); rotate(half.rotation)
                    if (half.isLeft) clipRect(left=-100f, top=-100f, right=0f, bottom=100f) else clipRect(left=0f, top=-100f, right=100f, bottom=100f)
                }) { drawText(textLayoutResult = layout, topLeft = Offset(-layout.size.width/2f, -layout.size.height/2f), alpha = half.alpha) }
            }
            players.forEach { p ->
                p.leftWrist?.let { drawCircle(p.color, 15f, Offset(it.x, it.y)); drawCircle(p.color.copy(0.3f), 50f, Offset(it.x, it.y)) }
                p.rightWrist?.let { drawCircle(p.color, 15f, Offset(it.x, it.y)); drawCircle(p.color.copy(0.3f), 50f, Offset(it.x, it.y)) }
            }
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = kotlin.math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)).toFloat()
    override fun release() {}
}
