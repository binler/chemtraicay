package com.example.testapp.game.impl

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.*
import kotlin.math.sin
import kotlin.random.Random

class BubblePopImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Bong Bóng Màu Sắc"
    override val category = CategoryType.LEARNING
    override val targetAge = AgeGroup.TODDLER
    override val icon = "🫧"
    override val isAR = false

    private val bubbles = mutableStateListOf<Bubble>()
    private val particles = mutableStateListOf<Particle>()
    private var lastSpawnTime = 0L

    data class Bubble(var x: Float, var y: Float, val radius: Float, val color: Color, val speed: Float, val phase: Float)
    data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, val color: Color, var alpha: Float = 1f)

    private val colorNames = mapOf(
        Color(0xFF85D7FF) to "Màu xanh dương",
        Color(0xFFFF6B6B) to "Màu hồng",
        Color(0xFFFFD93D) to "Màu vàng",
        Color(0xFF6BCB77) to "Màu xanh lá"
    )

    private var onSpeech: (String) -> Unit = {}

    override fun init(context: android.content.Context, width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        players.addAll(initialPlayers)
        bubbles.clear()
        particles.clear()
        onSpeech("Cùng thổi bong bóng màu sắc nào!")
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime > 1500) {
            bubbles.add(Bubble(
                (0.1f + Random.nextFloat() * 0.8f) * width, 
                height.toFloat() + 50f, 
                60f, 
                listOf(Color(0xFF85D7FF), Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77)).random(), 
                2f + Random.nextFloat() * 3f,
                Random.nextFloat() * 2f * kotlin.math.PI.toFloat()
            ))
            lastSpawnTime = currentTime
        }
        
        bubbles.forEach { it.y -= it.speed }
        bubbles.removeAll { it.y < -100f }

        particles.forEach { 
            it.x += it.vx
            it.y += it.vy
            it.vy += 0.1f
            it.alpha -= 0.03f
        }
        particles.removeAll { it.alpha <= 0 }
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun onTouch(x: Float, y: Float) {
        val iterator = bubbles.iterator()
        while (iterator.hasNext()) {
            val b = iterator.next()
            if (kotlin.math.sqrt(((x - b.x) * (x - b.x) + (y - b.y) * (y - b.y)).toDouble()) < b.radius + 30f) {
                players.getOrNull(0)?.score = (players.getOrNull(0)?.score ?: 0) + 10
                
                // Đọc tên màu sắc (Nhận diện màu cho bé 2 tuổi)
                colorNames[b.color]?.let { onSpeech(it) }

                // Hiệu ứng nổ tung
                repeat(12) {
                    particles.add(Particle(
                        b.x, b.y, 
                        (Random.nextFloat() - 0.5f) * 15f, 
                        (Random.nextFloat() - 0.5f) * 15f, 
                        b.color
                    ))
                }
                
                iterator.remove()
                return
            }
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            // Nền đã được vẽ ở MainActivity, ở đây vẽ hiệu ứng
            
            // Vẽ các hạt nổ
            particles.forEach { p ->
                drawCircle(p.color.copy(alpha = p.alpha), radius = 6f * p.alpha, center = Offset(p.x, p.y))
            }

            val time = System.currentTimeMillis()
            bubbles.forEach { b ->
                // Hiệu ứng phập phồng (Wobble)
                val wobble = sin(time * 0.005f + b.phase) * 5f
                val currentRadius = b.radius + wobble
                
                drawCircle(b.color.copy(alpha = 0.5f), currentRadius, Offset(b.x, b.y))
                // Ánh sáng bong bóng
                drawCircle(Color.White.copy(alpha = 0.3f), currentRadius * 0.3f, Offset(b.x - currentRadius * 0.3f, b.y - currentRadius * 0.3f))
            }
        }
    }

    override fun release() {}
}
