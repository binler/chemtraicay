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
import androidx.compose.ui.geometry.CornerRadius

class EnglishFlashcardsImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Học Tiếng Anh"
    override val category = CategoryType.LEARNING
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🇺🇸"
    override val isAR = false

    private data class Flashcard(val emoji: String, val english: String, val vietnamese: String)

    private val cards = listOf(
        Flashcard("🍎", "Apple", "Quả táo"),
        Flashcard("🍌", "Banana", "Quả chuối"),
        Flashcard("🍊", "Orange", "Quả cam"),
        Flashcard("🐘", "Elephant", "Con voi"),
        Flashcard("🦒", "Giraffe", "Con hươu cao cổ"),
        Flashcard("🦁", "Lion", "Con sư tử"),
        Flashcard("🔴", "Red", "Màu đỏ"),
        Flashcard("🔵", "Blue", "Màu xanh dương"),
        Flashcard("🟢", "Green", "Màu xanh lá"),
        Flashcard("🟡", "Yellow", "Màu vàng"),
        Flashcard("⭕", "Circle", "Hình tròn"),
        Flashcard("⬜", "Square", "Hình vuông"),
        Flashcard("🔺", "Triangle", "Hình tam giác"),
        Flashcard("⭐", "Star", "Hình ngôi sao"),
        Flashcard("🚗", "Car", "Xe ô tô"),
        Flashcard("🚌", "Bus", "Xe buýt"),
        Flashcard("🚲", "Bicycle", "Xe đạp"),
        Flashcard("✈️", "Airplane", "Máy bay"),
        Flashcard("🚢", "Ship", "Tàu thủy"),
        Flashcard("👨", "Father", "Bố"),
        Flashcard("👩", "Mother", "Mẹ")
    )

    private var currentIndex by mutableStateOf(0)
    private var isFlipped by mutableStateOf(false)
    private var onSpeech: (String) -> Unit = {}
    private var lastInteractionTime = 0L

    override fun init(context: android.content.Context, width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        players.clear()
        players.addAll(initialPlayers)
        currentIndex = 0
        isFlipped = false
        speakCurrentCard()
    }

    private fun speakCurrentCard() {
        val card = cards[currentIndex]
        if (!isFlipped) {
            onSpeech(card.english)
        } else {
            onSpeech(card.vietnamese)
        }
    }

    override fun update(width: Int, height: Int) {}
    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}

    override fun onTouch(x: Float, y: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastInteractionTime < 500) return

        if (!isFlipped) {
            isFlipped = true
            speakCurrentCard()
        } else {
            isFlipped = false
            currentIndex = (currentIndex + 1) % cards.size
            speakCurrentCard()
        }
        lastInteractionTime = currentTime
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        with(drawScope) {
            val card = cards[currentIndex]
            val center = Offset(size.width / 2, size.height / 2)

            // Vẽ nền thẻ bài
            drawRoundRect(
                color = Color(0xFFF5F5F5),
                topLeft = Offset(center.x - 250f, center.y - 350f),
                size = androidx.compose.ui.geometry.Size(500f, 700f),
                cornerRadius = CornerRadius(40f, 40f)
            )

            val emojiLayout = textMeasurer.measure(card.emoji, TextStyle(fontSize = 150.sp))
            drawText(textLayoutResult = emojiLayout, topLeft = Offset(center.x - emojiLayout.size.width / 2, center.y - 300f))

            val textToDisplay = if (isFlipped) card.vietnamese else card.english
            val textColor = if (isFlipped) Color(0xFF4CAF50) else Color(0xFF2196F3)
            
            val textLayout = textMeasurer.measure(
                text = textToDisplay,
                style = TextStyle(fontSize = 60.sp, fontWeight = FontWeight.Black, color = textColor)
            )
            drawText(textLayoutResult = textLayout, topLeft = Offset(center.x - textLayout.size.width / 2, center.y + 100f))

            val hint = if (isFlipped) "Chạm để học từ tiếp theo" else "Chạm để xem nghĩa tiếng Việt"
            val hintLayout = textMeasurer.measure(hint, TextStyle(fontSize = 20.sp, color = Color.Gray))
            drawText(textLayoutResult = hintLayout, topLeft = Offset(center.x - hintLayout.size.width / 2, center.y + 250f))
        }
    }

    override fun release() {}
}
