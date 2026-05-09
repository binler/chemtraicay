package com.example.testapp.game.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.AgeGroup
import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.GameLogic
import com.example.testapp.game.core.Player
import kotlin.random.Random

class MathNinjaImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Toán Học Vận Động"
    override val category = CategoryType.LEARNING
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "➕"
    override val isAR = true

    private var question by mutableStateOf("")
    private var correctAnswer by mutableStateOf(0)
    private val options = mutableStateListOf<NumberOption>()
    private var onSpeech: (String) -> Unit = {}
    private var lastSpawnTime = 0L

    data class NumberOption(val value: Int, var x: Float, var y: Float, val vx: Float, val vy: Float)

    override fun init(width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        players.addAll(initialPlayers)
        generateQuestion()
    }

    private fun generateQuestion() {
        val isAddition = Random.nextBoolean()
        if (isAddition) {
            val a = Random.nextInt(1, 6)
            val b = Random.nextInt(1, 6)
            correctAnswer = a + b
            question = "$a + $b = ?"
            onSpeech("Mấy cộng mấy đây con? $a cộng $b bằng bao nhiêu nhỉ?")
        } else {
            // Đảm bảo kết quả phép trừ không âm cho bé
            val a = Random.nextInt(3, 11)
            val b = Random.nextInt(1, a)
            correctAnswer = a - b
            question = "$a - $b = ?"
            onSpeech("$a trừ $b bằng mấy nhỉ? Con tìm kết quả đi nào!")
        }
        options.clear()
    }

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime > 2000 && options.size < 3) {
            val isCorrect = Random.nextFloat() < 0.4f || options.none { it.value == correctAnswer }
            val value = if (isCorrect) correctAnswer else Random.nextInt(1, 11)
            options.add(NumberOption(
                value = value,
                x = (0.2f + Random.nextFloat() * 0.6f) * width,
                y = height.toFloat() + 50f,
                vx = (Random.nextFloat() - 0.5f) * 8f,
                vy = -15f - Random.nextFloat() * 5f
            ))
            lastSpawnTime = currentTime
        }

        options.removeAll { opt ->
            opt.x += opt.vx; opt.y += opt.vy
            opt.y > height + 100
        }
        checkCollisions()
    }

    private fun checkCollisions() {
        players.forEach { player ->
            val wrists = listOfNotNull(player.leftWrist, player.rightWrist)
            wrists.forEach { wrist ->
                val iterator = options.iterator()
                while (iterator.hasNext()) {
                    val opt = iterator.next()
                    if (dist(wrist.x, wrist.y, opt.x, opt.y) < 100f) {
                        if (opt.value == correctAnswer) {
                            player.score += 50
                            onSpeech("Chính xác! Bằng $correctAnswer. Con giỏi quá!")
                            generateQuestion()
                        } else {
                            onSpeech("Ồ ô, không phải rồi, thử lại nhé!")
                            iterator.remove()
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
            // Vẽ câu hỏi
            val qLayout = textMeasurer.measure(question, TextStyle(fontSize = 80.sp, fontWeight = FontWeight.Bold, color = Color.White))
            drawText(textLayoutResult = qLayout, topLeft = Offset(size.width/2 - qLayout.size.width/2, 100f))

            // Vẽ các bong bóng số
            options.forEach { opt ->
                drawCircle(Color.Cyan.copy(alpha = 0.6f), 70f, Offset(opt.x, opt.y))
                val nLayout = textMeasurer.measure(opt.value.toString(), TextStyle(fontSize = 50.sp, color = Color.White))
                drawText(textLayoutResult = nLayout, topLeft = Offset(opt.x - nLayout.size.width/2, opt.y - nLayout.size.height/2))
            }

            players.forEach { p ->
                p.leftWrist?.let { drawCircle(p.color, 25f, Offset(it.x, it.y)) }
                p.rightWrist?.let { drawCircle(p.color, 25f, Offset(it.x, it.y)) }
            }
        }
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) = kotlin.math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2))
    override fun release() {}
}
