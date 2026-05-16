package com.example.testapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.ui.theme.*

/**
 * KidButton: Nút bấm bản to (height 72dp+). Có hiệu ứng đổ bóng 3D dày ở đáy.
 * Sử dụng spring animation tạo hiệu ứng nảy (Jelly) khi bé nhấn vào.
 */
@Composable
fun KidButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SunnyYellow,
    shadowColor: Color = Color(0xFFD4A017),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    // Hiệu ứng nảy (Jelly)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val verticalOffset by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
    )

    Box(
        modifier = modifier
            .height(84.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Lớp bóng đổ 3D
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
                .background(shadowColor, RoundedCornerShape(32.dp))
        )

        // Lớp bề mặt nút
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp)
                .offset(y = verticalOffset)
                .background(containerColor, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )
            )
        }
    }
}

/**
 * KidCard: Thẻ nội dung trắng kem, bo góc cực đại, có bóng đổ mềm.
 */
@Composable
fun KidCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(40.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(containerColor = CreamWhite),
        shape = RoundedCornerShape(40.dp),
        content = content
    )
}

/**
 * KidTopBar: Thanh tiêu đề bo tròn, hiển thị tên phân khu bằng Tiếng Việt.
 */
@Composable
fun KidTopBar(
    title: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SkyBlue
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(containerColor)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = TextDark,
            fontWeight = FontWeight.Black
        )
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
fun KidComponentsPreview() {
    RyRoTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KidTopBar(title = "HÀNH TINH GAME")

            KidCard {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Chào bé yêu! Cùng khám phá thế giới khoa học nhé.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            KidButton(text = "Bắt đầu thôi!") {
                // Do nothing
            }
            
            KidButton(text = "Học tập", containerColor = MintGreen, shadowColor = Color(0xFF4A9B53)) {
                // Do nothing
            }
        }
    }
}
