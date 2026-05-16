package com.example.testapp.game.impl

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.game.core.*
import com.example.testapp.ui.theme.*
import kotlin.math.*

class SolarSystemImpl : GameLogic {
    override var players = mutableStateListOf<Player>()
    override val name = "Vũ Trụ Kỳ Diệu"
    override val category = CategoryType.SCIENCE
    override val targetAge = AgeGroup.PRESCHOOL
    override val icon = "🪐"
    override val isAR = false

    private enum class PlanetState { NORMAL, SELECTED }

    // Đóng gói thực thể hành tinh chuẩn hóa dữ liệu
    private class PlanetEntity(
        val name: String,
        val emoji: String,
        val baseColor: Color,
        val fact: String,
        val a: Float,       // Bán trục lớn elip (Quỹ đạo X)
        val b: Float,       // Bán trục nhỏ elip (Quỹ đạo Y)
        val size: Float,    // Kích thước hiển thị
        val baseSpeed: Float,
        val hasRings: Boolean = false,
        val hasMoon: Boolean = false
    ) {
        var angle by mutableStateOf(0f)
        var state by mutableStateOf(PlanetState.NORMAL)
        var selfRotation by mutableStateOf(0f)
    }

    private val planets = mutableStateListOf<PlanetEntity>()
    private var onSpeech: (String) -> Unit = {}
    private var resources: android.content.res.Resources? = null

    // UI State đồng bộ hệ thống
    private var rotationSpeedFactor by mutableStateOf(1f)
    private var isResearchMode by mutableStateOf(false)
    private var researchProgress by mutableStateOf(45)
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var lastTime = System.currentTimeMillis()

    override fun init(context: Context, width: Int, height: Int, initialPlayers: List<Player>, onSpeech: (String) -> Unit) {
        this.onSpeech = onSpeech
        this.resources = context.resources
        this.screenWidth = width.toFloat()
        this.screenHeight = height.toFloat()

        // Tỷ lệ scale mượt mà theo kích thước màn hình thực tế của Tab S9
        val s = minOf(width, height) / 900f
        planets.clear()

        // Khởi tạo các hành tinh xếp lớp chuẩn tỉ lệ lượng giác, không chồng chéo
        planets.add(PlanetEntity("Sao Thủy", "🌑", Color(0xFF9E9E9E), getString(R.string.solar_fact_mercury), 140f * s, 90f * s, 18f * s, 0.035f))
        planets.add(PlanetEntity("Sao Kim", "🔸", Color(0xFFF57C00), getString(R.string.solar_fact_venus), 210f * s, 135f * s, 26f * s, 0.025f))
        planets.add(PlanetEntity("Trái Đất", "🌍", Color(0xFF1976D2), getString(R.string.solar_fact_earth), 290f * s, 185f * s, 32f * s, 0.018f, hasMoon = true))
        planets.add(PlanetEntity("Sao Hỏa", "🔴", Color(0xFFD32F2F), getString(R.string.solar_fact_mars), 370f * s, 235f * s, 24f * s, 0.012f))
        planets.add(PlanetEntity("Sao Mộc", "🟤", Color(0xFFFFA000), getString(R.string.solar_fact_jupiter), 480f * s, 310f * s, 48f * s, 0.008f))
        planets.add(PlanetEntity("Sao Thổ", "🪐", Color(0xFFE0E0E0), getString(R.string.solar_fact_saturn), 600f * s, 390f * s, 42f * s, 0.005f, hasRings = true))

        onSpeech(getString(R.string.solar_system_title))
        lastTime = System.currentTimeMillis()
    }

    private fun getString(id: Int): String = resources?.getString(id) ?: ""

    override fun update(width: Int, height: Int) {
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastTime) / 16.67f // Chuẩn hóa mượt mà cho màn hình 120Hz
        lastTime = currentTime

        planets.forEach { p ->
            // Luôn duy trì quay quỹ đạo mượt mà độc lập với frame rate
            p.angle += p.baseSpeed * rotationSpeedFactor * deltaTime
            if (p.angle > 2 * PI) {
                p.angle -= (2 * PI).toFloat()
            }
            p.selfRotation += 0.8f * deltaTime
        }
    }

    override fun onTouch(x: Float, y: Float) {
        val cx = screenWidth / 2f
        val cy = screenHeight / 2f

        var hitAny = false

        // Fix triệt để logic Tap vùng chọn hành tinh bằng thuật toán lượng giác đồng bộ 100% với hàm vẽ
        planets.forEach { p ->
            val px = cx + p.a * cos(p.angle)
            val py = cy + p.b * sin(p.angle)
            val dx = x - px
            val dy = y - py
            val distance = sqrt((dx * dx + dy * dy).toDouble())

            // Mở rộng bán kính vùng chạm (p.size * 1.8f) giúp bé 2 tuổi dễ thao tác chạm trúng
            if (distance < p.size * 1.8f) {
                planets.forEach { it.state = PlanetState.NORMAL }
                p.state = PlanetState.SELECTED
                onSpeech(p.fact)
                hitAny = true
                return
            }
        }

        // Tương tác khi không chạm trúng hành tinh nào
        if (!hitAny) {
            planets.forEach { it.state = PlanetState.NORMAL }
        }
    }

    override fun draw(drawScope: DrawScope, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
        val size = drawScope.size
        val cx = size.width / 2f
        val cy = size.height / 2f
        val time = System.currentTimeMillis()

        // LAYER 0: AMOLED BLACK BACKGROUND (Chuẩn màn hình Galaxy Tab S9)
        drawScope.drawRect(Color(0xFF0A0E17))

        // Vẽ hiệu ứng ngàn sao lung linh nền trời ẩn hiện nhẹ nhàng
        repeat(60) { i ->
            val rx = (i * 31415.9f) % size.width
            val ry = (i * 27182.8f) % size.height
            val alpha = 0.2f + 0.6f * abs(sin(time * 0.0012f + i))
            drawScope.drawCircle(Color(0xFFE2F1FF), radius = 2f, center = Offset(rx, ry), alpha = alpha)
        }

        // LAYER 1: VẼ QUỸ ĐẠO ELIP ĐỒNG TÂM CỐ ĐỊNH (Không méo, không sai lệch)
        planets.forEach { p ->
            drawScope.drawOval(
                color = Color(0xFF2C3D4E).copy(alpha = 0.5f),
                topLeft = Offset(cx - p.a, cy - p.b),
                size = Size(p.a * 2, p.b * 2),
                style = Stroke(width = 1.5.dp.toPx(drawScope))
            )
        }

        // ÔNG MẶT TRỜI HOẠT HÌNH (Chính giữa trung tâm)
        drawScope.withTransform({ translate(cx, cy) }) {
            // Hiệu ứng hào quang phát sáng mềm mại tỏa ra xung quanh
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFFFFE082), Color.Transparent), radius = 110f),
                radius = 110f, center = Offset.Zero
            )
            // Thân mặt trời màu vàng ấm áp
            drawCircle(Color(0xFFFFD93D), radius = 65f, center = Offset.Zero)

            // Vẽ biểu cảm mặt cười ngộ nghĩnh (Khan Academy Style) cho bé vui vẻ
            drawCircle(Color(0xFF333333), radius = 6f, center = Offset(-20f, -12f))
            drawCircle(Color(0xFF333333), radius = 6f, center = Offset(20f, -12f))
            drawArc(
                color = Color(0xFF333333),
                startAngle = 0f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(-20f, 0f), size = Size(40f, 30f),
                style = Stroke(5f, cap = StrokeCap.Round)
            )
        }

        // LAYER 2: ĐỒ HỌA DI CHUYỂN CỦA CÁC HÀNH TINH
        planets.forEach { p ->
            // Sử dụng chính xác công thức lượng giác elip đồng bộ với onTouch
            val px = cx + p.a * cos(p.angle)
            val py = cy + p.b * sin(p.angle)

            val isSelected = p.state == PlanetState.SELECTED
            val scale = if (isSelected) 1.5f else 1.0f
            val displaySize = p.size * scale

            // KHÓA TRỤC TRANSFORMATION: Chỉ dịch chuyển tọa độ tâm, KHÔNG xoay ma trận chung để tránh đảo lộn asset
            drawScope.withTransform({ translate(px, py) }) {
                if (isSelected) {
                    // Quầng sáng Pastel rực rỡ bọc ngoài hành tinh đang chọn
                    drawCircle(
                        brush = Brush.radialGradient(listOf(p.baseColor.copy(0.4f), Color.Transparent), radius = displaySize * 2.2f),
                        radius = displaySize * 2.2f, center = Offset.Zero
                    )
                }

                // Kết cấu thực thể hành tinh kết hợp Shading tự nhiên + Icon vẽ tay sắc nét
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(0.3f), p.baseColor, Color.Black.copy(0.4f)),
                        start = Offset(-displaySize, -displaySize),
                        end = Offset(displaySize, displaySize)
                    ),
                    radius = displaySize, center = Offset.Zero
                )

                // Render Graphic chữ/biểu tượng hành tinh mượt mà, đứng thẳng trục
                val textStyle = TextStyle(fontSize = (displaySize * 1.1f).sp)
                val planetLayout = textMeasurer.measure(p.emoji, textStyle)

                // Sử dụng thêm biến ma trận xoay riêng cho lõi nội dung nếu muốn, ở đây giữ đứng cho bé dễ nhận biết
                drawText(
                    planetLayout,
                    topLeft = Offset(-planetLayout.size.width / 2f, -planetLayout.size.height / 2f)
                )

                // Vòng nhẫn bao quanh Sao Thổ thiết kế nghiêng mềm mại
                if (p.hasRings) {
                    drawOval(
                        color = Color(0xFFFFF59D).copy(alpha = 0.6f),
                        topLeft = Offset(-displaySize * 1.8f, -displaySize * 0.25f),
                        size = Size(displaySize * 3.6f, displaySize * 0.5f),
                        style = Stroke(5f)
                    )
                }

                // Vệ tinh Mặt Trăng xoay quanh quỹ đạo cục bộ Trái Đất
                if (p.hasMoon) {
                    val moonAngle = time * 0.0025f
                    val mx = cos(moonAngle) * (displaySize + 18f)
                    val my = sin(moonAngle) * (displaySize + 18f)
                    drawCircle(Color(0xFFECEFF1), radius = 5f, center = Offset(mx, my))
                }
            }
        }

        // LAYER 3: HỆ THỐNG UI OVERLAY

        // 1. Thanh Banner thông báo/Thuyết minh dưới đáy màn hình (Màu trắng kem/vàng sữa)
        val bannerText = if (planets.any { it.state == PlanetState.SELECTED }) {
            planets.find { it.state == PlanetState.SELECTED }?.fact ?: ""
        } else {
            "Hệ Mặt Trời của Chúng Ta - Chạm vào hành tinh để tìm hiểu thêm!"
        }

        val bannerW = size.width * 0.85f
        val bannerH = 85f
        val bannerX = (size.width - bannerW) / 2f
        val bannerY = size.height - bannerH - 20f

        drawScope.drawRoundRect(
            color = Color(0xFFFFF9F0), // Tone Trắng Kem Pastel mượt mắt cho trẻ nhỏ
            topLeft = Offset(bannerX, bannerY),
            size = Size(bannerW, bannerH),
            cornerRadius = CornerRadius(24f, 24f)
        )
        val bannerLayout = textMeasurer.measure(
            bannerText,
            TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C3E50))
        )
        drawScope.drawText(
            bannerLayout,
            topLeft = Offset(size.width / 2f - bannerLayout.size.width / 2f, bannerY + bannerH / 2f - bannerLayout.size.height / 2f)
        )
    }

    private fun androidx.compose.ui.unit.Dp.toPx(density: androidx.compose.ui.unit.Density): Float = with(density) { this@toPx.toPx() }

    override fun onPlayersUpdate(updatedPlayers: List<Player>) {}
    override fun release() {}
}
