package com.example.testapp.game.impl

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testapp.game.core.*
import com.example.testapp.ui.theme.*
import kotlin.math.sin
import kotlin.random.Random

class MathGardenImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Vườn Toán Học"
    override val category = CategoryType.LEARNING
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🧮"
    override val isAR = false

    private var num1 by mutableStateOf(0)
    private var num2 by mutableStateOf(0)
    private var operator by mutableStateOf("+")
    private var correctAnswer by mutableStateOf(0)
    private val options = mutableStateListOf<Int>()
    
    private var onSpeech: (String) -> Unit = {}
    private var lastInteractionTime = 0L
    private var screenWidth = 0f
    private var screenHeight = 0f
    
    // Trạng thái chờ chuyển câu hỏi
    private var nextQuestionTime by mutableStateOf(0L)
    private var lastCorrectValue by mutableStateOf(-1)

    override fun init(context: android.content.Context, width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        this.screenWidth = width.toFloat()
        this.screenHeight = height.toFloat()
        players.clear()
        players.addAll(initialPlayers)
        nextQuestionTime = 0L
        lastCorrectValue = -1
        generateNewQuestion()
    }

    private fun generateNewQuestion() {
        val isAddition = Random.nextBoolean()
        val speechOperator: String
        
        if (isAddition) {
            // Cộng trong phạm vi 20
            num1 = Random.nextInt(1, 15) // Số hạng thứ nhất
            num2 = Random.nextInt(1, 21 - num1) // Đảm bảo tổng không quá 20
            operator = "+"
            speechOperator = "cộng"
            correctAnswer = num1 + num2
        } else {
            // Trừ trong phạm vi 20
            num1 = Random.nextInt(5, 21) // Số bị trừ lên đến 20
            num2 = Random.nextInt(1, num1) // Đảm bảo hiệu dương
            operator = "-"
            speechOperator = "trừ"
            correctAnswer = num1 - num2
        }
        
        options.clear()
        options.add(correctAnswer)
        while (options.size < 3) {
            val wrong = Random.nextInt(1, 21)
            if (!options.contains(wrong)) options.add(wrong)
        }
        options.shuffle()
        
        // Đọc rõ ràng bằng chữ để TTS không bỏ sót
        onSpeech("Con hãy cho biết $num1 $speechOperator $num2 bằng mấy nhé?")
    }

    override fun update(width: Int, height: Int) {
        val now = System.currentTimeMillis()
        // Nếu đang trong trạng thái chờ và đã hết thời gian delay -> Đổi câu hỏi
        if (nextQuestionTime > 0 && now > nextQuestionTime) {
            generateNewQuestion()
            nextQuestionTime = 0L
            lastCorrectValue = -1
        }
    }

    override fun onTouch(x: Float, y: Float) {
        val now = System.currentTimeMillis()
        // Nếu đang hiện kết quả đúng hoặc chưa hết thời gian chờ thì không nhận chạm mới
        if (nextQuestionTime > 0 || now - lastInteractionTime < 1000 || screenWidth == 0f) return

        val itemW = screenWidth / 4
        val cy = screenHeight * 0.6f
        
        var selectedValue: Int? = null
        
        for (i in 0 until options.size) {
            val value = options[i]
            val cx = (i + 1) * itemW
            val d = kotlin.math.sqrt(((x - cx)*(x - cx) + (y - cy)*(y - cy)).toDouble())
            if (d < 120f) {
                selectedValue = value
                break
            }
        }

        selectedValue?.let { value ->
            if (value == correctAnswer) {
                val praises = listOf(
                    "Chính xác! Con giỏi quá đi.",
                    "Đúng rồi! Bé thông minh tuyệt vời.",
                    "Oa! Kết quả hoàn toàn chính xác.",
                    "Con làm tốt lắm! Tiếp tục nào."
                )
                onSpeech(praises.random())
                players.firstOrNull()?.let { it.score += 10 }
                
                // Thiết lập thời gian chuyển câu hỏi sau 2.5 giây
                lastCorrectValue = value
                nextQuestionTime = now + 2500
            } else {
                onSpeech("Ồ ô, hãy thử lại nhé con.")
            }
            lastInteractionTime = now
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        val size = drawScope.size
        val time = System.currentTimeMillis()

        // 1. Nền vườn Pastel
        drawScope.drawRect(Brush.verticalGradient(listOf(Color(0xFFE1F5FE), Color(0xFFF1F8E9))))

        // 2. Vẽ Câu hỏi (Số lớn, rực rỡ)
        val qText = "$num1 $operator $num2 = ?"
        val qLayout = textMeasurer.measure(qText, TextStyle(fontSize = 80.sp, fontWeight = FontWeight.Black, color = TextDark))
        drawScope.drawText(textLayoutResult = qLayout, topLeft = Offset(size.width/2 - qLayout.size.width/2, 150f))

        // 3. Vẽ 3 Quả cầu đáp án
        val itemW = size.width / 4
        options.forEachIndexed { index, value ->
            val cx = (index + 1) * itemW
            val cy = size.height * 0.6f
            
            // Hiệu ứng phập phồng (Wobble)
            val wobble = sin(time * 0.003f + index) * 8f
            
            // Nếu đây là quả cầu vừa chọn đúng, nó sẽ nở to ra rực rỡ
            val isCorrectSelection = (value == lastCorrectValue && nextQuestionTime > 0)
            val isOtherSelection = (value != lastCorrectValue && nextQuestionTime > 0)
            
            val radius = if (isCorrectSelection) 150f else if (isOtherSelection) 60f else 100f + wobble
            val alpha = if (isOtherSelection) 0.3f else 1f
            
            val sphereColor = when(index) {
                0 -> SkyBlue
                1 -> SoftPink
                else -> MintGreen
            }

            // Vẽ khối cầu bóng bẩy
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.7f * alpha), sphereColor.copy(alpha = alpha), sphereColor.copy(alpha = 0.8f * alpha)),
                    center = Offset(cx - radius * 0.3f, cy - radius * 0.3f),
                    radius = radius * 1.5f
                ),
                radius = radius,
                center = Offset(cx, cy)
            )

            // Vẽ số bên trong quả cầu
            val nLayout = textMeasurer.measure(value.toString(), TextStyle(fontSize = if(isCorrectSelection) 64.sp else 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = alpha)))
            drawScope.drawText(textLayoutResult = nLayout, topLeft = Offset(cx - nLayout.size.width/2, cy - nLayout.size.height/2))
            
            // Hiệu ứng hào quang cho quả cầu đúng
            if (isCorrectSelection) {
                drawScope.drawCircle(
                    color = Color.Yellow.copy(alpha = 0.3f),
                    radius = radius + 30f + sin(time * 0.01f) * 10f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 10f)
                )
            }
        }

        // 4. Bảng hướng dẫn phía dưới
        val hintText = "Chạm vào quả cầu mang đáp án đúng nhé!"
        val hintLayout = textMeasurer.measure(hintText, TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TextGray))
        drawScope.drawText(textLayoutResult = hintLayout, topLeft = Offset(size.width/2 - hintLayout.size.width/2, size.height - 150f))
    }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}
    override fun release() {}
}
